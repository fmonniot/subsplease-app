package eu.monniot.subpleaseapp.clients.subsplease

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ParsersTest {

    @Test
    fun parseGetLatestPayload() {
        val content = javaClass.classLoader?.getResource("getlatest.xml")?.readText().orEmpty()

        val today = LocalDate.now(ZoneId.of("America/Los_Angeles"))
        val nov1 = LocalDate.of(today.year, 11, 1)

        val updates = parseLatestShows(content, today)

        assertEquals(
            listOf(
                ShowUpdate(
                    "/shows/houkago-saikoro-club#06",
                    today,
                    "Houkago Saikoro Club",
                    6.0
                ),
                ShowUpdate(
                    "/shows/ore-wo-suki-nano-wa-omae-dake-ka-yo#06",
                    today,
                    "Ore wo Suki nano wa Omae dake ka yo",
                    6.0
                ),
                ShowUpdate(
                    "/shows/shinchou-yuusha#05",
                    today,
                    "Shinchou Yuusha",
                    5.0
                ),
                ShowUpdate(
                    "/shows/urashimasakatasen-no-nichijou#06",
                    today.minusDays(1),
                    "Urashimasakatasen no Nichijou",
                    6.0
                ),
                ShowUpdate(
                    "/shows/kandagawa-jet-girls#04-5",
                    nov1,
                    "Kandagawa Jet Girls",
                    4.5
                )
            ),
            updates
        )
    }

    @Test
    fun parseShowDetails() {
        val content = javaClass.classLoader?.getResource("show-details.html")?.readText().orEmpty()

        val show = parseShowDetails(content)

        assertEquals(
            Show(
                name = "Ore wo Suki nano wa Omae dake ka yo",
                id = 1307,
                slug = "ore-wo-suki-nano-wa-omae-dake-ka-yo",
                imageUrl = "https://horriblesubs.info/wp-content/uploads/2019/10/oresuki.jpg",
                description = "Kisaragi Amatsuyu is invited out alone by the cool beauty upperclassman Cosmos and his childhood friend Himawari. Expecting to hear their confessions, he triumphantly goes to meet each of them in turn. But Cosmos and Himawari both instead confess to Amatsuyu that they like his friend. Amatsuyu fights this lonely battle, but there is another girl who is looking at him. She is a gloomy girl with glasses and braids. Amatsuyu finds that he hates her, because she's always turning her sharp tongue only on him and finding enjoyment in his troubles. But it turns out that she's the only one who actually does like him.",
                episodes = emptyList()
            ),
            show
        )
    }

    @Test
    fun parseShowEpisodes() {
        val content = javaClass.classLoader?.getResource("show-episodes.xml")?.readText().orEmpty()
        val today = LocalDate.now(ZoneId.of("America/Los_Angeles"))

        val episodes = parseShowEpisodes(content, today)

        assertEquals(
            listOf(
                ShowEpisode(
                    1,
                    LocalDate.of(2019, 10, 2)
                ),
                ShowEpisode(
                    2,
                    LocalDate.of(2019, 10, 9)
                ),
                ShowEpisode(
                    3,
                    LocalDate.of(2019, 10, 16)
                ),
                ShowEpisode(
                    4,
                    LocalDate.of(2019, 10, 23)
                ),
                ShowEpisode(
                    5,
                    LocalDate.of(2019, 10, 30)
                ),
                ShowEpisode(6, today.minusDays(1))
            ),
            episodes
        )
    }

    @Test
    fun parseSeasonalShows() {
        val content =
            javaClass.classLoader?.getResource("current-season.html")?.readText().orEmpty()

        val episodes = parseCurrentSeasonShows(content)

        assertEquals(
            listOf(
                SeasonalShow(
                    "Ace of Diamond Act II",
                    "ace-of-diamond-act-ii"
                ),
                SeasonalShow(
                    "Bokutachi wa Benkyou ga Dekinai S2",
                    "bokutachi-wa-benkyou-ga-dekinai-s2"
                ),
                SeasonalShow(
                    name = "Bonobono",
                    slug = "bonobono"
                ),
                SeasonalShow(
                    name = "Boruto – Naruto Next Generations",
                    slug = "boruto-naruto-next-generations"
                ),
                SeasonalShow(
                    name = "Cardfight!! Vanguard – Zoku Koukousei-hen",
                    slug = "cardfight-vanguard-zoku-koukousei-hen"
                ),
                SeasonalShow(
                    name = "Chihayafuru S3",
                    slug = "chihayafuru-s3"
                ),
                SeasonalShow(
                    name = "Choyoyu",
                    slug = "choyoyu"
                ),
                SeasonalShow(
                    name = "Chuubyou Gekihatsu Boy",
                    slug = "chuubyou-gekihatsu-boy"
                ),
                SeasonalShow(
                    name = "ZX – Code Reunion",
                    slug = "zx-code-reunion"
                )
            ),
            episodes
        )
    }

}