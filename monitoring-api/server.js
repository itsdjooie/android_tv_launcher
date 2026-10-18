/**
 * MLHK Central Monitoring API
 * Lightweight service health monitoring for MLHK TV+ Launcher.
 * Built with pure Node.js (zero external dependencies).
 */

const http = require('http');
const https = require('https');
const net = require('net');
const url = require('url');
const fs = require('fs');
const path = require('path');

const CONFIG_PATH = path.join(__dirname, 'config.json');

// Load configuration
function loadConfig() {
    try {
        const raw = fs.readFileSync(CONFIG_PATH, 'utf8');
        return JSON.parse(raw);
    } catch (e) {
        console.error('Failed to load config.json, using defaults:', e.message);
        return {
            server: { port: 8080, api_key: 'mlhk-secure-key', check_interval_seconds: 30 },
            services: []
        };
    }
}

let config = loadConfig();

// In-memory status cache
let cachedStatus = {
    status: 'grey',
    message: 'Initialiseren...',
    timestamp: new Date().toISOString(),
    services: []
};

/**
 * Perform HTTP/HTTPS health check
 */
function checkHttpService(service) {
    return new Promise((resolve) => {
        const startTime = Date.now();
        const parsedUrl = new URL(service.url);
        const isHttps = parsedUrl.protocol === 'https:';
        const client = isHttps ? https : http;

        const options = {
            hostname: parsedUrl.hostname,
            port: parsedUrl.port || (isHttps ? 443 : 80),
            path: parsedUrl.pathname + parsedUrl.search,
            method: service.method || 'GET',
            timeout: service.timeout_ms || 3500,
            rejectUnauthorized: service.reject_unauthorized !== false,
            headers: {
                'User-Agent': 'MLHK-Monitor/1.0',
                ...(service.headers || {})
            }
        };

        const req = client.request(options, (res) => {
            const latency = Date.now() - startTime;
            const expectedCodes = service.expected_status || [200];
            const isExpected = expectedCodes.includes(res.statusCode);

            res.resume(); // discard body to free memory

            resolve({
                id: service.id,
                name: service.name,
                status: isExpected ? 'up' : (res.statusCode >= 500 ? 'down' : 'degraded'),
                latencyMs: latency,
                lastCheck: new Date().toISOString(),
                message: `HTTP ${res.statusCode}`
            });
        });

        req.on('timeout', () => {
            req.destroy();
            resolve({
                id: service.id,
                name: service.name,
                status: 'down',
                latencyMs: Date.now() - startTime,
                lastCheck: new Date().toISOString(),
                message: 'Timeout'
            });
        });

        req.on('error', (err) => {
            resolve({
                id: service.id,
                name: service.name,
                status: 'down',
                latencyMs: Date.now() - startTime,
                lastCheck: new Date().toISOString(),
                message: err.code || err.message
            });
        });

        req.end();
    });
}

/**
 * Perform TCP port health check
 */
function checkTcpService(service) {
    return new Promise((resolve) => {
        const startTime = Date.now();
        const socket = new net.Socket();
        const timeout = service.timeout_ms || 2500;

        socket.setTimeout(timeout);

        socket.connect(service.port, service.host, () => {
            const latency = Date.now() - startTime;
            socket.destroy();
            resolve({
                id: service.id,
                name: service.name,
                status: 'up',
                latencyMs: latency,
                lastCheck: new Date().toISOString(),
                message: 'TCP Open'
            });
        });

        socket.on('timeout', () => {
            socket.destroy();
            resolve({
                id: service.id,
                name: service.name,
                status: 'down',
                latencyMs: timeout,
                lastCheck: new Date().toISOString(),
                message: 'Connection Timeout'
            });
        });

        socket.on('error', (err) => {
            socket.destroy();
            resolve({
                id: service.id,
                name: service.name,
                status: 'down',
                latencyMs: Date.now() - startTime,
                lastCheck: new Date().toISOString(),
                message: err.code || 'TCP Connection Failed'
            });
        });
    });
}

/**
 * Execute all service checks and update cache
 */
async function runHealthChecks() {
    config = loadConfig();
    const services = config.services || [];
    const checkPromises = services.map((svc) => {
        if (svc.type === 'tcp') {
            return checkTcpService(svc);
        } else {
            return checkHttpService(svc);
        }
    });

    const results = await Promise.all(checkPromises);

    // Compute aggregated status
    let criticalDown = false;
    let anyIssues = false;

    for (const res of results) {
        const svcConfig = services.find((s) => s.id === res.id);
        const isCritical = svcConfig ? svcConfig.critical !== false : true;

        if (res.status === 'down') {
            anyIssues = true;
            if (isCritical) {
                criticalDown = true;
            }
        } else if (res.status === 'degraded') {
            anyIssues = true;
        }
    }

    let overallStatus = 'green';
    let overallMessage = 'Alles online';

    if (results.length === 0) {
        overallStatus = 'grey';
        overallMessage = 'Geen diensten geconfigureerd';
    } else if (criticalDown) {
        overallStatus = 'red';
        overallMessage = 'Kritieke dienst offline';
    } else if (anyIssues) {
        overallStatus = 'orange';
        overallMessage = 'Minstens één dienst heeft vertraging of storing';
    }

    cachedStatus = {
        status: overallStatus,
        message: overallMessage,
        timestamp: new Date().toISOString(),
        services: results
    };

    console.log(`[${cachedStatus.timestamp}] Health check: ${overallStatus.toUpperCase()} - ${overallMessage}`);
}

// Start background interval
const intervalMs = (config.server.check_interval_seconds || 30) * 1000;
setInterval(runHealthChecks, intervalMs);
runHealthChecks(); // initial run

// Create HTTP Server
const server = http.createServer((req, res) => {
    const parsed = url.parse(req.url, true);

    // CORS headers
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, X-API-Key, Authorization');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    // Endpoint: /api/status
    if (parsed.pathname === '/api/status' || parsed.pathname === '/status') {
        // Auth check
        const authHeader = req.headers['authorization'] || '';
        const apiKeyHeader = req.headers['x-api-key'] || '';
        const tokenQuery = parsed.query.token || '';

        const configuredKey = config.server.api_key;
        if (configuredKey) {
            const bearer = authHeader.replace(/^Bearer\s+/i, '');
            const authorized = bearer === configuredKey || apiKeyHeader === configuredKey || tokenQuery === configuredKey;
            if (!authorized) {
                res.writeHead(401, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: 'Unauthorized: Invalid API Key' }));
                return;
            }
        }

        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        });
        res.end(JSON.stringify(cachedStatus, null, 2));
        return;
    }

    // Endpoint: /api/check-now (forces immediate refresh)
    if (parsed.pathname === '/api/check-now') {
        runHealthChecks().then(() => {
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify(cachedStatus, null, 2));
        });
        return;
    }

    // Default 404
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Not Found' }));
});

const PORT = config.server.port || 8080;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`MLHK Central Monitoring API running on http://0.0.0.0:${PORT}`);
    console.log(`Endpoint: http://0.0.0.0:${PORT}/api/status`);
});
