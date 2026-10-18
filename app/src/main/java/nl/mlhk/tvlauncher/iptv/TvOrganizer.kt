package nl.mlhk.tvlauncher.iptv

import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale

data class OrganizedChannel(val channel: Channel, val name: String, val country: String, val category: String, val quality: Int)
data class ChannelFamily(val name: String, val primary: Channel, val variants: List<Channel>, val country: String, val category: String)

object TvOrganizer {
    val categories = listOf(
        "Nederlandse zenders", "Nederlandse sport", "Sportzenders", "Films en series",
        "Nieuws", "Kinderen", "Documentaires", "Muziek", "Vlaamse zenders", "Internationaal", "Overige"
    )

    private fun plain(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "").lowercase(Locale.ROOT)

    private val quality = Regex("(?i)\\b(?:4k|8k|uhd|fhd|full\\s*hd|hd|sd|hevc|h\\.?265|h\\.?264|1080[pi]?|720p?|2160p?|50fps|60fps|backup|bkup)\\b")
    private val countryTag = Regex("(?i)^(?:\\[?(?:nl|nld|nederland|netherlands|dutch|be|bel|belgie|belgium|uk|gb|us|usa|de|ger|fr|tr|es|it)\\]?\\s*[:|·_-]\\s*)+")
    private val sport = Regex("\\b(?:sport\\w*|espn|ziggo\\s*sport|eurosport|viaplay|dazn|bein|f1|formula\\s*1|formule\\s*1|motors\\s*tv|racing|golf|tennis|ufc|fight|premier\\s*league|eredivisie)\\b")
    private val knownDutch = Regex("\\b(?:npo|rtl\\s*[4578z]|sbs\\s*(?:6|9)|net\\s*5|veronica|ziggo\\s*sport|omroep|24kitchen|rtl\\s*lounge|rtl\\s*crime|rtl\\s*telekids|nos|nederland\\s*[123])\\b")
    private val countryRules = listOf(
        "NL" to "(?:nl|nld|nederland|netherlands|dutch)", "BE" to "(?:be|bel|belgie|belgium|vlaams|vlaanderen|flemish)",
        "UK" to "(?:uk|gb|british|united kingdom)", "US" to "(?:us|usa|united states)",
        "DE" to "(?:de|ger|germany|duitsland|german)", "FR" to "(?:fr|fra|france|french)",
        "TR" to "(?:tr|tur|turkey|turkish)", "ES" to "(?:es|esp|spain|spanish)", "IT" to "(?:it|ita|italy|italian)"
    ).map { it.first to Regex("(?:^|[^a-z])${it.second}(?:$|[^a-z])") }

    private val categoryRules = listOf(
        "Volwassenen" to Regex("\\b(?:xxx|adult|18\\+|erotic|hustler|penthouse|playboy)\\b"),
        "Sportzenders" to sport,
        "Kinderen" to Regex("\\b(?:kids|kinderen|junior|disney|nickelodeon|nick\\s*jr|cartoon|boomerang|baby\\s*tv|telekids|ketnet|cbeebies)\\b"),
        "Nieuws" to Regex("\\b(?:news|nieuws|cnn|bbc\\s*world|euronews|bloomberg|al\\s*jazeera|nos)\\b"),
        "Documentaires" to Regex("\\b(?:documentar\\w*|discovery|national\\s*geographic|nat\\s*geo|history|animal\\s*planet|love\\s*nature)\\b"),
        "Muziek" to Regex("\\b(?:music|muziek|mtv|vh1|stingray|538|192\\s*tv|xite|mezzo)\\b"),
        "Films en series" to Regex("\\b(?:film\\w*|movie\\w*|cinema|series|hbo|paramount|film1|cinemax|amc)\\b")
    )
    private val qualityRules = listOf(4 to Regex("\\b(?:8k|4k|uhd|2160p?)\\b"), 3 to Regex("\\b(?:fhd|full\\s*hd|1080[pi]?)\\b"), 2 to Regex("\\b(?:hd|720p?)\\b"), 1 to Regex("\\bsd\\b"))
    private val knownBelgian = Regex("\\b(?:vrt|een|canvas|ketnet|vtm|play\\s*[4567])\\b")

    private fun explicitCountry(text: String): String {
        return countryRules.firstOrNull { it.second.containsMatchIn(text) }?.first.orEmpty()
    }

    fun classify(channel: Channel): OrganizedChannel {
        val name = plain(channel.name)
        val group = plain(channel.group)
        val country = explicitCountry(name).ifBlank { explicitCountry(group) }.ifBlank {
            when {
                channel.id.lowercase(Locale.ROOT).endsWith(".nl") || knownDutch.containsMatchIn(name) -> "NL"
                knownBelgian.containsMatchIn(name) -> "BE"
                else -> ""
            }
        }
        val all = "$name $group"
        val category = categoryRules.firstOrNull { it.second.containsMatchIn(all) }?.first ?: "Overige"
        val rank = qualityRules.firstOrNull { it.second.containsMatchIn(name) }?.first ?: 0
        val clean = channel.name.replace(countryTag, "").replace(quality, "").replace(Regex("[|\\[\\]()]"), " ").replace(Regex("\\s+"), " ").trim(' ', '-', '_')
        return OrganizedChannel(channel, clean.ifBlank { channel.name }, country, category, rank)
    }

    fun families(channels: List<Channel>, qualityPreference: String = "fhd", overrides: JSONObject? = null): List<ChannelFamily> {
        val preferred = if (qualityPreference == "uhd") 4 else 3
        val grouped = channels.map { channel ->
            val classified = classify(channel)
            val custom = overrides?.optJSONObject(channel.id)
            if (custom == null) classified else classified.copy(
                name = custom.optString("name", classified.name),
                country = custom.optString("country", classified.country),
                category = custom.optString("category", classified.category)
            )
        }.groupBy { "${it.country}:${it.category}:${plain(it.name)}" }

        val families = grouped.values.map { variants ->
            val ordered = variants.sortedWith(
                compareBy<OrganizedChannel> {
                    if (it.quality == preferred) 0 else if (it.quality == 2) 1 else if (it.quality == 4) 2 else if (it.quality == 1) 3 else 4
                }.thenBy { it.channel.name }
            )
            val first = ordered.first()
            ChannelFamily(first.name, first.channel, ordered.map { it.channel }, first.country, first.category)
        }
        val dutchOrder = listOf("npo 1", "npo 2", "npo 3", "rtl 4", "rtl 5", "sbs 6", "rtl 7", "net5", "net 5", "veronica", "rtl 8", "sbs 9")
        return families.sortedWith(
            compareBy<ChannelFamily> { when (it.country) { "NL" -> 0; "BE" -> 1; else -> 2 } }
                .thenBy { dutchOrder.indexOf(plain(it.name)).let { i -> if (i < 0) 100 else i } }
                .thenBy { plain(it.name).replace(Regex("[0-9]+")) { match -> match.value.padStart(5, '0') } }
        )
    }

    fun groups(families: List<ChannelFamily>, favorites: Set<String>): List<String> {
        val result = mutableListOf<String>()
        if (families.any { it.variants.any { v -> v.id in favorites } }) result.add("Favorieten")
        result.addAll(categories.filter { select(families, it, favorites).isNotEmpty() })
        if (families.any { it.category == "Volwassenen" }) result.add("Volwassenen")
        result.add("Alle zenders")
        return result
    }

    fun select(families: List<ChannelFamily>, group: String, favorites: Set<String>): List<ChannelFamily> = families.filter { family ->
        when (group) {
            "Favorieten" -> family.variants.any { it.id in favorites }
            "Nederlandse zenders" -> family.country == "NL" && family.category != "Volwassenen"
            "Nederlandse sport" -> family.country == "NL" && family.category == "Sportzenders"
            "Vlaamse zenders" -> family.country == "BE" && family.category != "Volwassenen"
            "Internationaal" -> family.country !in listOf("NL", "BE", "") && family.category != "Volwassenen"
            "Alle zenders" -> true
            else -> family.category == group
        }
    }
}
