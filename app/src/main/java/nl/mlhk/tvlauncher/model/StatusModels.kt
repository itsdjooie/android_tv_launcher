package nl.mlhk.tvlauncher.model

enum class IndicatorState {
    GREEN,
    ORANGE,
    RED,
    GREY
}

data class ServiceStatus(
    val id: String,
    val name: String,
    val status: String, // "up", "degraded", "down", "unknown"
    val latencyMs: Long = 0,
    val lastCheck: String = "",
    val message: String? = null
) {
    val isUp: Boolean get() = status.equals("up", ignoreCase = true)
    val isDown: Boolean get() = status.equals("down", ignoreCase = true)
    val isDegraded: Boolean get() = status.equals("degraded", ignoreCase = true)
}

data class SystemHealthResponse(
    val status: String = "grey", // "green", "orange", "red", "grey"
    val message: String = "Status onbekend",
    val timestamp: String = "",
    val services: List<ServiceStatus> = emptyList()
) {
    val indicatorState: IndicatorState
        get() = when (status.lowercase()) {
            "green" -> IndicatorState.GREEN
            "orange" -> IndicatorState.ORANGE
            "red" -> IndicatorState.RED
            else -> IndicatorState.GREY
        }
}
