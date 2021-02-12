package eu.monniot.subpleaseapp.clients.subsplease

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.time.ZoneId

class ClientTest {

    @Test
    fun fetchingSchedule() {
        val tz = ZoneId.of("America/Los_Angeles")

        runBlocking {
            val result = client.schedule(tz)

            val shows = mapOf(
                "Monday" to listOf(Shows.tatoeba),
                "Tuesday" to listOf(Shows.tensei),
                "Wednesday" to listOf(Shows.horizon),
                "Thursday" to listOf(Shows.yuru, Shows.tenchi),
                "Friday" to listOf(Shows.jaku),
                "Saturday" to listOf(Shows.horimiya),
                "Sunday" to listOf(Shows.nonNon),
            )

            assertEquals(ScheduleResponse(shows, "America/Los_Angeles"), result)
        }
    }

    @Test
    fun fetchingDownloads() {
        val tz = ZoneId.of("America/Los_Angeles")

        runBlocking {
            val result = client.downloads(tz, 104)

            val episode = mapOf(
                "Horimiya - 04" to DownloadItem(
                    "01/30/21",
                    "Horimiya",
                    "04",
                    listOf(
                        DownloadLink("540", "magnet:?xt=urn:btih:5PEFUTGDMQ4CBZOYEDS64OLTX7MUYLJJ&dn=%5BSubsPlease%5D%20Horimiya%20-%2004%20%28540p%29%20%5B3E4CEBA9%5D.mkv&xl=349066870&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2F9.rarbg.to%3A2710%2Fannounce&tr=udp%3A%2F%2F9.rarbg.me%3A2710%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.cyberia.is%3A6969%2Fannounce&tr=udp%3A%2F%2Fexodus.desync.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=udp%3A%2F%2Ftracker.tiny-vps.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fretracker.lanta-net.ru%3A2710%2Fannounce&tr=http%3A%2F%2Fopen.acgnxtracker.com%3A80%2Fannounce&tr=wss%3A%2F%2Ftracker.openwebtorrent.com"),
                        DownloadLink("720", "magnet:?xt=urn:btih:EWEZLXCAJUWCCPSXLCTQE54374QOO23S&dn=%5BSubsPlease%5D%20Horimiya%20-%2004%20%28720p%29%20%5BF0E0B024%5D.mkv&xl=613951322&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2F9.rarbg.to%3A2710%2Fannounce&tr=udp%3A%2F%2F9.rarbg.me%3A2710%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.cyberia.is%3A6969%2Fannounce&tr=udp%3A%2F%2Fexodus.desync.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=udp%3A%2F%2Ftracker.tiny-vps.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fretracker.lanta-net.ru%3A2710%2Fannounce&tr=http%3A%2F%2Fopen.acgnxtracker.com%3A80%2Fannounce&tr=wss%3A%2F%2Ftracker.openwebtorrent.com"),
                        DownloadLink("1080", "magnet:?xt=urn:btih:IUKNTIIBKM2DI5V2P5GGKBNNMRGQFL77&dn=%5BSubsPlease%5D%20Horimiya%20-%2004%20%281080p%29%20%5B53F80174%5D.mkv&xl=1383989732&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2F9.rarbg.to%3A2710%2Fannounce&tr=udp%3A%2F%2F9.rarbg.me%3A2710%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.cyberia.is%3A6969%2Fannounce&tr=udp%3A%2F%2Fexodus.desync.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=udp%3A%2F%2Ftracker.tiny-vps.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fretracker.lanta-net.ru%3A2710%2Fannounce&tr=http%3A%2F%2Fopen.acgnxtracker.com%3A80%2Fannounce&tr=wss%3A%2F%2Ftracker.openwebtorrent.com"),
                    )
                ),
                "Horimiya - 03" to DownloadItem(
                    "01/23/21",
                    "Horimiya",
                    "03",
                    emptyList()
                ),
                "Horimiya - 02" to DownloadItem(
                    "01/16/21",
                    "Horimiya",
                    "02",
                    emptyList()
                ),
                "Horimiya - 01" to DownloadItem(
                    "01/09/21",
                    "Horimiya",
                    "01",
                    emptyList()
                ),
            )

            assertEquals(DownloadsResponse(emptyMap(), episode), result)
        }
    }

    @Test
    fun parsingShowDetails() {
        val txt = javaClass.classLoader?.getResource("subsplease/shows/hiromiya.html")?.readText()

        val synopsis = parseDetails(txt!!)

        assertEquals(
            listOf(
                "A secret life is the one thing they have in common. At school, Hori is a prim and perfect social butterfly, but the truth is she's a brash homebody. Meanwhile, under a gloomy facade, Miyamura hides a gentle heart, along with piercings and tattoos. In a chance meeting, they both reveal a side they've never shown. Could this blossom into something new?",
                "Spoilers: yes it does."
            ), synopsis
        )
    }


    companion object {

        object Shows {
            val tatoeba = Show(
                page = "tatoeba-last-dungeon-mae-no-mura-no-shounen-ga-joban-no-machi-de-kurasu-youna-monogatari",
                time = "06:30",
                title = "Tatoeba Last Dungeon Mae no Mura no Shounen ga Joban no Machi de Kurasu Youna Monogatari",
                imageUrl = "/wp-content/uploads/2021/01/106599.jpg"
            )

            val tensei = Show(
                page = "tensei-shitara-slime-datta-ken",
                time = "08:00",
                title = "Tensei Shitara Slime Datta Ken",
                imageUrl = "/wp-content/uploads/2021/01/93337.jpg"
            )

            val horizon = Show(
                page = "log-horizon-s3",
                time = "04:00",
                title = "Log Horizon S3",
                imageUrl = "/wp-content/uploads/2021/01/108026.jpg"
            )
            val yuru = Show(
                page = "yuru-camp-s2",
                time = "08:00",
                title = "Yuru Camp S2",
                imageUrl = "/wp-content/uploads/2021/01/110636.jpg"
            )
            val tenchi = Show(
                page = "tenchi-souzou-design-bu",
                time = "08:30",
                title = "Tenchi Souzou Design-bu",
                imageUrl = "/wp-content/uploads/2021/01/109865.jpg"
            )
            val jaku = Show(
                page = "jaku-chara-tomozaki-kun",
                time = "04:30",
                title = "Jaku-Chara Tomozaki-kun",
                imageUrl = "/wp-content/uploads/2021/01/109232.jpg"
            )
            val horimiya = Show(
                page = "horimiya",
                time = "09:00",
                title = "Horimiya",
                imageUrl = "/wp-content/uploads/2021/01/110336.jpg"
            )
            val nonNon = Show(
                page = "non-non-biyori-nonstop",
                time = "09:35",
                title = "Non Non Biyori Nonstop",
                imageUrl = "/wp-content/uploads/2021/01/107670.jpg"
            )

        }

        private lateinit var server: MockWebServer
        private lateinit var client: SubsPleaseApi

        @BeforeClass
        @JvmStatic
        fun setup() {
            server = MockWebServer()
            server.dispatcher = subsPleaseDispatcher

            val http = OkHttpClient.Builder()
                .addInterceptor(run {
                    val h = HttpLoggingInterceptor()
                    //h.level = HttpLoggingInterceptor.Level.BODY
                    h
                })
                .build()

            client = SubsPleaseApi.build(
                http,
                server.url("/").toString()
            )
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            server.close()
        }

        private val subsPleaseDispatcher = object : Dispatcher() {

            fun fromResource(path: String): MockResponse {
                val txt = javaClass.classLoader?.getResource(path)?.readText()

                return if (txt == null) {
                    MockResponse().setResponseCode(404).setBody("Resource $path not found")
                } else {
                    MockResponse().setResponseCode(200).setBody(txt)
                }
            }

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.requestUrl?.queryParameter("f")) {
                    "schedule" -> fromResource("subsplease/schedule.json")
                    "show" -> {
                        if (request.requestUrl?.queryParameter("sid")?.toInt() == 104) {
                            fromResource("subsplease/show.json")
                        } else {
                            MockResponse().setResponseCode(400).setBody("Invalid sid")
                        }
                    }
                    else -> MockResponse().setResponseCode(404).setBody("Unknown path")
                }
            }

        }
    }

}