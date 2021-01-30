package eu.monniot.subpleaseapp.clients.subsplease

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.*


private fun extractShowUpdate(a: Element, date: LocalDate): ShowUpdate? {
    return try {
        val link = a.attr("href")
        val name = a.textNodes()
            .filter { !it.isBlank }
            .joinToString()
            .trim()
            .removeSuffix(" -")
        val no = a.getElementsByTag("strong")[0].text().toDouble()

        val release =
            when (val txt = a.getElementsByClass("latest-releases-date")[0].text()) {
                "Today" -> date
                "Yesterday" -> date.minusDays(1)
                else ->
                    // Format MMM dst/nd/rd/th so we have to strip the suffix
                    MonthDay
                        .parse(
                            txt.dropLast(2),
                            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
                        )
                        .atYear(date.year)

            }

        ShowUpdate(link, release, name, no)
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

fun parseLatestShows(content: String, today: LocalDate): List<ShowUpdate> {
    return Jsoup.parse(content)
        .select("ul li a")
        .mapNotNull {
            extractShowUpdate(
                it,
                today
            )
        }
}

fun parseShowDetails(content: String): Show? {
    return try {

        val document = Jsoup.parse(content)
        val article = document.selectFirst("article.page")

        val title = article.select(".entry-title").text()
        val description = article.select(".series-desc p").text()

        val id =
            article
                .selectFirst("script")
                .childNode(0)
                .outerHtml()
                .removePrefix("var hs_showid = ")
                .removeSuffix(";")
                .toInt()

        val imageUrl = document
            .select("#secondary .series-image img")[0]
            .attr("src")

        val slug = document.select("link[rel=\"canonical\"]")[0]
            .attr("href")
            .removePrefix("https://horriblesubs.info/shows/")
            .removeSuffix("/")

        Show(
            name = title,
            id = id,
            slug = slug,
            imageUrl = imageUrl,
            description = description,
            episodes = emptyList()
        )
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}

private fun extractShowEpisode(element: Element, today: LocalDate): ShowEpisode? {

    return try {
        val release =
            when (val txt = element.selectFirst(".rls-date").text()) {
                "Today" -> today
                "Yesterday" -> today.minusDays(1)
                else ->
                    // Format MMM dst/nd/rd/th so we have to strip the suffix
                    LocalDate.parse(
                        txt,
                        DateTimeFormatter.ofPattern("MM/dd/yy", Locale.ENGLISH)
                    )

            }
        val no = element.selectFirst(".rls-label strong").text().toInt()

        ShowEpisode(no = no, releaseDate = release)
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}

fun parseShowEpisodes(content: String, today: LocalDate): List<ShowEpisode> {
    return Jsoup.parse(content)
        .select(".rls-info-container")
        .mapNotNull {
            extractShowEpisode(
                it,
                today
            )
        }
        .sortedBy { it.no }
}

fun parseCurrentSeasonShows(content: String): List<SeasonalShow> {
    return Jsoup.parse(content)
        .select(".shows-wrapper .ind-show a")
        .mapNotNull { a ->
            try {
                val name = a.text().trim()
                val slug = a.attr("href").removePrefix("/shows/")

                SeasonalShow(name, slug)
            } catch (t: Throwable) {
                null
            }
        }
        .sortedBy { it.name }
}
