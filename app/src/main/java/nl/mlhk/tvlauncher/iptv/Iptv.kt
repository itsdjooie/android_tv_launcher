package nl.mlhk.tvlauncher.iptv

import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import javax.xml.parsers.SAXParserFactory

data class Channel(val id: String, val name: String, val url: String, val group: String, val logo: String = "")
data class Programme(val channel: String, val title: String, val start: Long, val end: Long)
data class TvCatalog(val channels: List<Channel>, val programmes: Map<String, List<Programme>>, val guideAvailable: Boolean = true) {
    fun now(channel: Channel, time: Long = System.currentTimeMillis()) = programmes[channel.id]?.firstOrNull { it.start <= time && it.end > time }
    fun next(channel: Channel, time: Long = System.currentTimeMillis()) = programmes[channel.id]?.firstOrNull { it.start > time }
}

object TvSession {
    @Volatile var catalog: TvCatalog? = null
    @Volatile var families: List<ChannelFamily> = emptyList()
    @Volatile var playbackChannels: List<Channel> = emptyList()
}

object IptvParsers {
    private val attribute = Regex("([\\w-]+)=\"([^\"]*)\"")

    fun m3u(text: String, allowHttp: Boolean = false): List<Channel> {
        require(text.trimStart('\uFEFF', ' ', '\r', '\n').startsWith("#EXTM3U")) { "Ongeldige M3U playlist" }
        val channels = mutableListOf<Channel>()
        var pending: Map<String, String>? = null
        var title = ""
        text.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#EXTINF:")) {
                val attributes = attribute.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
                pending = attributes
                var quoted = false
                var comma = -1
                for (i in line.indices) {
                    if (line[i] == '"') quoted = !quoted
                    if (line[i] == ',' && !quoted) { comma = i; break }
                }
                title = if (comma >= 0) line.substring(comma + 1).trim() else attributes["tvg-name"].orEmpty()
            } else if (line.isNotBlank() && !line.startsWith('#') && pending != null) {
                val attrs = pending!!
                if (channels.size < 15000 && IptvRepository.validUrl(line, allowHttp)) {
                    val name = title.ifBlank { attrs["tvg-name"].orEmpty() }.ifBlank { "Zender ${channels.size + 1}" }
                    channels.add(
                        Channel(
                            attrs["tvg-id"].orEmpty().ifBlank { name },
                            name,
                            line,
                            attrs["group-title"].orEmpty(),
                            attrs["tvg-logo"].orEmpty()
                        )
                    )
                }
                pending = null
            }
        }
        return channels.distinctBy { it.url }
    }

    fun xtream(
        array: JSONArray,
        base: String,
        username: String,
        password: String,
        categories: Map<String, String> = emptyMap()
    ): List<Channel> {
        val encode = { value: String -> URLEncoder.encode(value, "UTF-8").replace("+", "%20") }
        return (0 until minOf(array.length(), 15000)).mapNotNull { index ->
            val item = array.getJSONObject(index)
            val stream = item.optString("stream_id")
            if (!stream.matches(Regex("[0-9]+"))) null else {
                val epg = item.optString("epg_channel_id").let { if (it == "null") "" else it }
                Channel(
                    epg.ifBlank { stream },
                    item.optString("name", "Zender $stream"),
                    "${base.trimEnd('/')}/live/${encode(username)}/${encode(password)}/$stream.m3u8",
                    categories[item.optString("category_id")].orEmpty(),
                    item.optString("stream_icon")
                )
            }
        }
    }

    fun xmlTime(value: String): Long {
        val parts = value.trim().split(Regex("\\s+"))
        val digits = parts[0]
        require(digits.length == 12 || digits.length == 14)
        val date = LocalDateTime.parse(
            digits,
            DateTimeFormatter.ofPattern(if (digits.length == 14) "yyyyMMddHHmmss" else "yyyyMMddHHmm")
        )
        val zone = parts.getOrNull(1) ?: "+0000"
        val offset = if (zone == "Z") ZoneOffset.UTC else ZoneOffset.of(zone)
        return date.toInstant(offset).toEpochMilli()
    }

    fun xmltv(input: InputStream, channels: Set<String>, now: Long = System.currentTimeMillis()): Map<String, List<Programme>> {
        val output = mutableMapOf<String, MutableList<Programme>>()
        val factory = SAXParserFactory.newInstance()
        runCatching { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) }
        runCatching { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        runCatching { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        val handler = object : DefaultHandler() {
            var channel = ""
            var start = 0L
            var end = 0L
            var inTitle = false
            val title = StringBuilder()
            var count = 0
            override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                if (qName == "programme") {
                    channel = attributes.getValue("channel").orEmpty()
                    title.setLength(0)
                    start = runCatching { xmlTime(attributes.getValue("start")) }.getOrDefault(0)
                    end = runCatching { xmlTime(attributes.getValue("stop")) }.getOrDefault(0)
                }
                if (qName == "title" && title.isEmpty()) inTitle = true
            }
            override fun characters(chars: CharArray, begin: Int, length: Int) {
                if (inTitle && title.length < 512) title.append(chars, begin, minOf(length, 512 - title.length))
            }
            override fun endElement(uri: String?, localName: String?, qName: String) {
                if (qName == "title") inTitle = false
                if (qName == "programme" && channel in channels && start > 0 && end > start && end > now - 3_600_000 && start < now + 48 * 3_600_000L && count < 50000) {
                    output.getOrPut(channel) { mutableListOf() }.add(Programme(channel, title.toString(), start, end))
                    count++
                }
            }
        }
        val guarded = object : java.io.FilterInputStream(input) {
            val forbidden = "<!DOCTYPE"
            var matched = 0
            var bytesRead = 0
            private fun inspect(value: Int) {
                if (value < 0) return
                require(++bytesRead <= 64 * 1024 * 1024 && value != 0) { "XMLTV te groot of geen UTF-8" }
                val c = value.toChar()
                matched = if (c == forbidden[matched]) matched + 1 else if (c == '<') 1 else 0
                require(matched < forbidden.length) { "DTD is niet toegestaan" }
            }
            override fun read(): Int = super.read().also { inspect(it) }
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                val count = super.read(buffer, offset, length)
                for (i in 0 until maxOf(0, count)) inspect(buffer[offset + i].toInt() and 255)
                return count
            }
        }
        factory.newSAXParser().xmlReader.apply {
            contentHandler = handler
            entityResolver = org.xml.sax.EntityResolver { _, _ -> throw org.xml.sax.SAXException("Externe entities niet toegestaan") }
        }.parse(org.xml.sax.InputSource(guarded))
        return output.mapValues { it.value.sortedBy { p -> p.start } }
    }
}

class IptvRepository(
    private val iptvType: String,
    private val iptvServer: String,
    private val iptvUsername: String,
    private val iptvPassword: String,
    private val iptvM3u: String,
    private val iptvXmltv: String,
    private val allowHttp: Boolean = false
) {
    private fun <T> stream(url: String, max: Int, reader: (InputStream) -> T): T {
        require(validUrl(url, allowHttp)) { "Ongeldig provideradres: $url" }
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 12000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("User-Agent", "MLHK-TV/1.0")
        connection.setRequestProperty("Accept-Encoding", "gzip")
        try {
            require(connection.responseCode == 200) { "HTTP ${connection.responseCode} van IPTV provider" }
            val raw = connection.inputStream
            val input = if (connection.contentEncoding?.contains("gzip") == true || url.substringBefore('?').endsWith(".gz")) GZIPInputStream(raw) else raw
            val deadline = System.nanoTime() + 45_000_000_000L
            val limited = object : java.io.FilterInputStream(input) {
                var total = 0
                private fun check(count: Int) {
                    if (count > 0) total += count
                    require(total <= max && System.nanoTime() < deadline) { "Stream download overschrijdt limiet of time-out" }
                }
                override fun read(): Int = super.read().also { check(if (it >= 0) 1 else 0) }
                override fun read(bytes: ByteArray, offset: Int, length: Int): Int = super.read(bytes, offset, length).also { check(it) }
            }
            return limited.use(reader)
        } finally {
            connection.disconnect()
        }
    }

    private fun bytes(url: String, max: Int = 16 * 1024 * 1024) = stream(url, max) { it.readBytes() }

    fun load(onChannels: (TvCatalog) -> Unit = {}): TvCatalog {
        val channels: List<Channel>
        val guideUrl: String
        if (iptvType == "xtream") {
            val base = iptvServer.trimEnd('/')
            val user = iptvUsername
            val password = iptvPassword
            val encode = { value: String -> URLEncoder.encode(value, "UTF-8") }
            val api = "$base/player_api.php?username=${encode(user)}&password=${encode(password)}"
            val auth = JSONObject(String(bytes(api), Charsets.UTF_8)).getJSONObject("user_info")
            require(auth.optInt("auth") == 1) { "Provider weigert de inloggegevens" }
            val categories = runCatching {
                val array = JSONArray(String(bytes("$api&action=get_live_categories"), Charsets.UTF_8))
                (0 until array.length()).associate {
                    val item = array.getJSONObject(it)
                    item.optString("category_id") to item.optString("category_name")
                }
            }.getOrDefault(emptyMap())
            channels = IptvParsers.xtream(JSONArray(String(bytes("$api&action=get_live_streams"), Charsets.UTF_8)), base, user, password, categories)
            guideUrl = iptvXmltv.ifBlank { "$base/xmltv.php?username=${encode(user)}&password=${encode(password)}" }
        } else {
            require(iptvType == "m3u") { "Geen geldig IPTV type" }
            channels = IptvParsers.m3u(String(bytes(iptvM3u), Charsets.UTF_8), allowHttp)
            guideUrl = iptvXmltv
        }
        require(channels.isNotEmpty()) { "Geen zenders ontvangen van IPTV-provider" }
        onChannels(TvCatalog(channels, emptyMap(), false))
        val guide = runCatching {
            if (guideUrl.isBlank()) emptyMap() else stream(guideUrl, 64 * 1024 * 1024) { IptvParsers.xmltv(it, channels.map { c -> c.id }.toSet()) }
        }
        return TvCatalog(channels, guide.getOrDefault(emptyMap()), guideUrl.isNotBlank() && guide.isSuccess)
    }

    companion object {
        fun validUrl(value: String, allowHttp: Boolean): Boolean = runCatching {
            val uri = URI(value)
            uri.scheme in (if (allowHttp) listOf("https", "http") else listOf("https")) &&
                    !uri.host.isNullOrBlank() && uri.userInfo == null && uri.fragment == null
        }.getOrDefault(false)
    }
}
