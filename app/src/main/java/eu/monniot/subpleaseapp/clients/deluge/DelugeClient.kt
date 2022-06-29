package eu.monniot.subpleaseapp.clients.deluge

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import eu.monniot.subpleaseapp.clients.ApiCallback
import eu.monniot.subpleaseapp.clients.execAsSource
import eu.monniot.subpleaseapp.clients.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString.Companion.encodeUtf8
import ru.gildor.coroutines.okhttp.await

import java.util.concurrent.atomic.AtomicInteger

// TODO Use data class for authentication and make HTTP basic optional
class DelugeClient(
    private val base: String,
    private val username: String,
    private val password: String,
    private val okHttpClient: OkHttpClient
) {

    // Constants
    private val USER_AGENT = "SubPleaseApp/0.1.0"
    private val applicationJson = "application/json".toMediaType()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(PairAdapterFactory)
        .build()
    private val booleanResultAdapter = run {
        val pt = Types.newParameterizedTypeWithOwner(
            Companion::class.java,
            DelugeResponse::class.java,
            Boolean::class.javaObjectType
        )

        moshi.adapter<DelugeResponse<Boolean>>(pt)
    }
    private val torrentListAdapter = run {
        val pt = Types.newParameterizedTypeWithOwner(
            Companion::class.java,
            DelugeResponse::class.java,
            WebUi::class.java
        )

        moshi.adapter<DelugeResponse<WebUi>>(pt)
    }
    private val addTorrentAdapter: JsonAdapter<DelugeResponse<Unit>> = run {
        val pt = Types.newParameterizedTypeWithOwner(
            Companion::class.java,
            DelugeResponse::class.java,
            WebUi::class.java
        )

        moshi.adapter(pt)
    }

    // internal for testing
    internal val idCounter = AtomicInteger()
    internal var session: String? = null

    suspend fun listTorrents(): Result<List<Torrent>> {
        val body = """
                {
                    "id": "${idCounter.incrementAndGet()}",
                    "method": "web.update_ui",
                    "params": [
                        [
                            "queue",
                            "name",
                            "progress",
                            "state",
                            "eta",
                            "label"
                        ],
                        {}
                    ]
                }
            """.trimIndent().encodeUtf8().toRequestBody(applicationJson)

        val req = Request.Builder()
            .url(base)
            .post(body)
            .basicAuthorization(username, password)
            .addHeader("Cookie", "_session_id=$session")
            .addHeader("User-Agent", USER_AGENT)
            .build()

        // try catch ?
        val response = okHttpClient.newCall(req).await()
        val torrents = response.body?.source()?.use {
            // TODO Detect when the payload doesn't have a result but an error instead
            // eg.  {"id": "1", "result": null, "error": {"message": "Not authenticated", "code": 1}}
            val result = runCatching {
                torrentListAdapter.fromJson(it)
            }

            result.fold(
                { parsed ->
                    if (parsed == null) {
                        Result.ParsingError("Couldn't parse the JSON")
                    } else {
                        // TODO Also return the space available, it's going to be useful to know
                        // if we should start a download immediately or not.
                        Result.success(parsed.result.torrents.map {
                            Torrent.fromPartial(it.value, it.key)
                        })
                    }
                },
                { failure ->
                    Result.ParsingError(
                        failure.message ?: "Failure without error message: $failure"
                    )
                }
            )
        }

        return torrents ?: Result.ParsingError("No body to parse")
    }

    // TODO Maybe refactor with listTorrents ?
    // TODO suspend instead of ApiCallback
    fun addTorrentUrl(torrentUrl: String): ApiCallback<Unit> =
        { client, cb ->
            val method =
                if (torrentUrl.startsWith("magnet:")) "core.add_torrent_magnet"
                else "core.add_torrent_url"

            val params = "{}"

            val body = """
                {
                    "id": "${idCounter.incrementAndGet()}",
                    "params": ["$torrentUrl", $params],
                    "method": "$method"
                }
            """.trimIndent().encodeUtf8().toRequestBody(applicationJson)

            val req = Request.Builder()
                .url(base)
                .post(body)
                .basicAuthorization(username, password)
                .addHeader("Cookie", "_session_id=$session")
                .addHeader("User-Agent", USER_AGENT)
                .build()

            client.execAsSource(req) { result ->
                cb(result.flatMap { source ->
                    val parsed = source.use {
                        addTorrentAdapter.fromJson(source)
                    }

                    if (parsed == null) {
                        Result.ParsingError("Couldn't parse JSON response")
                    } else {
                        Result.success(parsed.result)
                    }
                })
            }
        }

    suspend fun login(): Result<Boolean> {
        val body = """
                {
                    "id": "-17000",
                    "method": "auth.login",
                    "params": [
                        "$password"
                    ]
                }
            """.trimIndent().encodeUtf8().toRequestBody(applicationJson)

        val req = Request.Builder()
            .url(base)
            .post(body)
            .basicAuthorization(username, password)
            .addHeader("User-Agent", USER_AGENT)
            .build()

        // try catch ?
        val response = okHttpClient.newCall(req).await()

        return if (response.code != 200) {
            Result.NonOkError(response.code, response.body!!.string())
        } else {
            val res = response.body!!.use {
                booleanResultAdapter.fromJson(it.source())
            }

            if (res == null) {
                Result.ParsingError("Couldn't parse the JSON")
            } else {
                if (res.result) {
                    // TODO Manage expiration of this cookie
                    session = parseSetCookieHeader(response.header("Set-Cookie"))
                }

                Result.success(res.result)
            }
        }

    }

    private fun Request.Builder.basicAuthorization(user: String, pass: String): Request.Builder {
        val input = "$user:$pass".toByteArray()
        val auth = java.util.Base64.getEncoder().encodeToString(input)
        return this.addHeader("Authorization", "Basic $auth")
    }


    companion object {

        internal fun parseSetCookieHeader(value: String?): String? =
            value?.let { cookie ->
                val sess = cookie.split(";")[0]

                if (sess.startsWith("_session_id=")) {
                    sess.removePrefix("_session_id=")
                } else null
            }


        data class Torrent(
            val id: String,
            val eta: Double,
            val label: String?,
            val name: String,
            val progress: Double,
            val queue: Int,
            val state: String  // TODO Need an enum on that one
        ) {

            companion object {
                fun fromPartial(partial: PartialTorrent, id: String): Torrent {
                    return Torrent(
                        id,
                        partial.eta,
                        partial.label,
                        partial.name,
                        partial.progress,
                        partial.queue,
                        partial.state
                    )
                }
            }
        }

        data class WebUi(
            val connected: Boolean,
            val filters: Filters,
            val stats: Stats,
            val torrents: Map<String, PartialTorrent>
        )

        data class Filters(val label: List<Pair<String, Int>>?, val state: List<Pair<String, Int>>)
        data class Stats(
            @Json(name = "free_space") val freeSpace: Long
        )

        // Use Adapter to go from a Map<String, PartialTorrent> to a List<Torrent>
        data class PartialTorrent(
            val eta: Double,
            val label: String?,
            val name: String,
            val progress: Double,
            val queue: Int,
            val state: String
        )

        data class DelugeResponse<T>(
            val id: String,
            val result: T,
            val error: String?
        )
    }
}