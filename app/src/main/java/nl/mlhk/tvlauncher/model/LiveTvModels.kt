package nl.mlhk.tvlauncher.model

data class LiveTvChannel(
    val channelNumber: Int,
    val name: String,
    val currentProgram: String,
    val nextProgram: String,
    val timeSlot: String,
    val progress: Float, // 0.0 to 1.0
    val category: String, // "Algemeen", "Nieuws", "Sport", "Films"
    val logoText: String
)
