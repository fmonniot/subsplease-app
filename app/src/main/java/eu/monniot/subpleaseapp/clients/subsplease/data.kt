package eu.monniot.subpleaseapp.clients.subsplease

import java.time.LocalDate


data class Show(
    val name: String,
    val id: Int,
    val slug: String,
    val imageUrl: String,
    val description: String,
    val episodes: List<ShowEpisode>
)

data class ShowEpisode(val no: Int, val releaseDate: LocalDate)

data class ShowUpdate(
    val link: String,
    val releaseDate: LocalDate,
    val name: String,
    val no: Double
)

data class SeasonalShow(val name: String, val slug: String)

