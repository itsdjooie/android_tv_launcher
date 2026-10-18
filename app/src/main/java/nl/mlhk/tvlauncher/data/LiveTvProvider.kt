package nl.mlhk.tvlauncher.data

import nl.mlhk.tvlauncher.model.LiveTvChannel
import java.text.SimpleDateFormat
import java.util.*

object LiveTvProvider {

    fun getChannels(): List<LiveTvChannel> {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val isEvening = currentHour in 19..23
        val isLate = currentHour in 0..5

        return listOf(
            LiveTvChannel(
                channelNumber = 1,
                name = "NPO 1",
                currentProgram = if (isEvening) "NOS Journaal & Even Tot Hier" else "NOS Journaal / Tijd voor MAX",
                nextProgram = "Nieuwsuur",
                timeSlot = "20:00 - 21:15",
                progress = calculateProgress(currentMinute),
                category = "Nieuws & Actualiteiten",
                logoText = "NPO 1"
            ),
            LiveTvChannel(
                channelNumber = 2,
                name = "NPO 2",
                currentProgram = if (isEvening) "Nieuwsuur & 2Doc" else "BinnensteBuiten",
                nextProgram = "De Avondshow",
                timeSlot = "21:30 - 22:20",
                progress = 0.45f,
                category = "Documentaire",
                logoText = "NPO 2"
            ),
            LiveTvChannel(
                channelNumber = 3,
                name = "NPO 3",
                currentProgram = if (isEvening) "Keuringsdienst van Waarde" else "Zapp / First Dates",
                nextProgram = "Sluipschutters",
                timeSlot = "20:25 - 21:10",
                progress = 0.70f,
                category = "Amusement",
                logoText = "NPO 3"
            ),
            LiveTvChannel(
                channelNumber = 4,
                name = "RTL 4",
                currentProgram = if (isEvening) "RTL Nieuws & B&B Vol Liefde" else "Editie NL / RTL Boulevard",
                nextProgram = "Jinek / Renze",
                timeSlot = "20:30 - 21:55",
                progress = 0.60f,
                category = "Amusement",
                logoText = "RTL 4"
            ),
            LiveTvChannel(
                channelNumber = 5,
                name = "RTL 5",
                currentProgram = "112 Vandaag",
                nextProgram = "Team Spoedeisende Hulp",
                timeSlot = "20:30 - 21:30",
                progress = 0.35f,
                category = "Reality",
                logoText = "RTL 5"
            ),
            LiveTvChannel(
                channelNumber = 6,
                name = "SBS 6",
                currentProgram = if (isEvening) "Chateau Meiland & Hart van Nederland" else "Shownieuws Vroeg",
                nextProgram = "Hart van Nederland Laat",
                timeSlot = "20:30 - 21:40",
                progress = 0.50f,
                category = "Amusement",
                logoText = "SBS 6"
            ),
            LiveTvChannel(
                channelNumber = 7,
                name = "RTL 7",
                currentProgram = "Darts Premier League / Actiefilm",
                nextProgram = "RTL Transportwereld",
                timeSlot = "20:00 - 22:30",
                progress = 0.65f,
                category = "Sport & Film",
                logoText = "RTL 7"
            ),
            LiveTvChannel(
                channelNumber = 8,
                name = "Veronica",
                currentProgram = "Top Gear & UEFA Champions League",
                nextProgram = "Film: Inception",
                timeSlot = "20:30 - 22:45",
                progress = 0.40f,
                category = "Mannen & Actie",
                logoText = "VERONICA"
            ),
            LiveTvChannel(
                channelNumber = 9,
                name = "NET 5",
                currentProgram = "Grey's Anatomy",
                nextProgram = "Het Blok",
                timeSlot = "20:30 - 21:30",
                progress = 0.55f,
                category = "Drama",
                logoText = "NET 5"
            ),
            LiveTvChannel(
                channelNumber = 14,
                name = "ESPN",
                currentProgram = "Eredivisie Live: Samenvattingen & Analyses",
                nextProgram = "Voetbalpraat",
                timeSlot = "20:00 - 22:00",
                progress = 0.75f,
                category = "Live Sport",
                logoText = "ESPN"
            ),
            LiveTvChannel(
                channelNumber = 15,
                name = "Ziggo Sport",
                currentProgram = "Rondo & Samenvattingen La Liga",
                nextProgram = "Formule 1 Race Café",
                timeSlot = "21:00 - 22:15",
                progress = 0.30f,
                category = "Sport",
                logoText = "ZIGGO"
            )
        )
    }

    private fun calculateProgress(minute: Int): Float {
        return (minute / 60.0f).coerceIn(0.15f, 0.90f)
    }
}
