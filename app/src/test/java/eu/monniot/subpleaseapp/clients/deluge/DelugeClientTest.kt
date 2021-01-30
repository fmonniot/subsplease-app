package eu.monniot.subpleaseapp.clients.deluge

import com.squareup.moshi.JsonReader
import eu.monniot.subpleaseapp.clients.Result
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.BufferedSource
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class DelugeClientTest {

    // TODO: Use a MockHttpServer under the hood
    private val http = OkHttpClient.Builder()
        .addInterceptor(run {
            val h = HttpLoggingInterceptor()
            h.level = HttpLoggingInterceptor.Level.BODY
            h
        })
        .build()

    @Test
    fun loginShouldSetTheSessionAndReturnTrue() {
        val server = MockWebServer()
        server.dispatcher = delugeLikeDispatcher
        val client = DelugeClient(
            server.url("deluge/json").toString(),
            "my-user-name",
            "my-secret"
        )
        val promise = CompletableFuture<Result<Boolean>>()

        client.login().invoke(http) {
            promise.complete(it)
        }

        val result = promise.get(5, TimeUnit.SECONDS)
        assertEquals(result, Result.success(true))
        assertNotNull("The session must be set", client.session)
    }

    @Test
    fun parseCookieWorksOk() {
        val header =
            "_session_id=c55fa72165ef013379a7b942274bf0a92147; Expires=Wed, 20 Nov 2019 22:11:54 GMT; Path=/deluge/json"
        val sessionId = DelugeClient.parseSetCookieHeader(header)

        assertEquals("c55fa72165ef013379a7b942274bf0a92147", sessionId)

        val incorrect =
            DelugeClient.parseSetCookieHeader("_forum_session=YWJUWjc5T2; path=/; secure; HttpOnly; SameSite=Lax")
        assertNull(incorrect)
    }

    @Test
    fun listTorrentsShouldReturnAListOfTorrents() {
        val server = MockWebServer()
        server.dispatcher = delugeLikeDispatcher
        val client = DelugeClient(
            server.url("deluge/json").toString(),
            "my-user-name",
            "my-secret"
        )

        val promise = CompletableFuture<Result<List<DelugeClient.Companion.Torrent>>>()
        client.listTorrents().invoke(http) { torrents ->
            promise.complete(torrents)
        }
        val actualResult = promise.get(5, TimeUnit.SECONDS)

        assertEquals(Result.success(
            listOf(
                DelugeClient.Companion.Torrent(
                    id = "f0938a29f938c9374ca93af0298d1a02983d947",
                    eta = 0.0,
                    label = "",
                    name = "my-awesome-file-name.mkv",
                    progress = 100.0,
                    queue = -1,
                    state = "Paused"
                )
            )
        ), actualResult)

        val recorded = server.takeRequest()
        assertEquals("POST /deluge/json HTTP/1.1", recorded.requestLine)
        assertEquals("Basic bXktdXNlci1uYW1lOm15LXNlY3JldA==", recorded.getHeader("Authorization"))
        assertEquals("_session_id=null", recorded.getHeader("Cookie"))
        assertNotNull(recorded.getHeader("User-Agent"))
    }

    companion object {
        val delugeLikeDispatcher = object : Dispatcher() {

            fun methodOf(src: BufferedSource): String? {
                var method: String? = null
                val reader = JsonReader.of(src)
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.selectName(JsonReader.Options.of("method"))) {
                        -1 -> {
                            reader.skipName()
                            reader.skipValue()
                        }
                        else -> {
                            method = reader.nextString()
                        }
                    }
                }
                reader.endObject()

                return method
            }

            fun fromResource(path: String): MockResponse {
                val txt = javaClass.classLoader?.getResource(path)?.readText()

                return if (txt == null) {
                    MockResponse().setResponseCode(404).setBody("Path $path not found")
                } else {
                    MockResponse().setResponseCode(200).setBody(txt)
                }
            }

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (val m = methodOf(request.body)) {
                    "web.update_ui" -> fromResource("deluge/web-update_ui.json")
                    "auth.login" ->
                        fromResource("deluge/auth-login-success.json")
                            .setHeader("Set-Cookie", "_session_id=a; Expires=Wed, 20 Nov 2019 22:11:54 GMT; Path=/deluge/json")
                    else -> MockResponse().setResponseCode(404).setBody("Unknown method $m")
                }
            }

        }
    }
}