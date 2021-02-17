package eu.monniot.subpleaseapp.clients.subsplease

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import ru.gildor.coroutines.okhttp.await
import java.lang.reflect.Type
import java.time.ZoneId
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi


data class Show(
    val page: String,
    val time: String,
    val title: String,
    @Json(name = "image_url") val imageUrl: String
)

data class ScheduleResponse(val schedule: Map<String, List<Show>>, val tz: String)

data class DownloadsResponse(
    val batch: Map<String, DownloadItem>,
    val episode: Map<String, DownloadItem>
)

data class DownloadItem(
    @Json(name = "release_date") val releaseDate: String,
    val show: String,
    val episode: String,
    val downloads: List<DownloadLink>
)

data class DownloadLink(val res: String, val magnet: String)

interface SubsPleaseApi {

    @GET("/api/?f=schedule")
    suspend fun schedule(@Query("tz") timeZone: ZoneId): ScheduleResponse

    @GET("/api/?f=show")
    suspend fun downloads(@Query("tz") timeZone: ZoneId, @Query("sid") sid: Int): DownloadsResponse

    companion object {
        fun build(client: OkHttpClient, baseUrl: String = "https://subsplease.org"): SubsPleaseApi {

            // Using reflection for now. If it is a concern, we can move to codegen
            val moshi = Moshi.Builder()
                .add(FACTORY)
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build()

            return retrofit.create(SubsPleaseApi::class.java)
        }

        private val FACTORY: JsonAdapter.Factory = object : JsonAdapter.Factory {
            override fun create(
                type: Type,
                annotations: Set<Annotation?>,
                moshi: Moshi
            ): JsonAdapter<*>? {
                if (!annotations.isEmpty()) return null
                if (Types.getRawType(type) !== java.util.Map::class.java) return null

                val objectJsonAdapter = moshi.nextAdapter<Map<*, *>>(this, type, annotations)
                return EmptyListAsMapAdapter(objectJsonAdapter)
            }
        }

        private class EmptyListAsMapAdapter(private val delegate: JsonAdapter<Map<*, *>>) :
            JsonAdapter<Map<*, *>>() {

            override fun fromJson(reader: JsonReader): Map<*, *>? {
                if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                    // Enter the array, and if non-empty, throw an error
                    reader.beginArray()

                    if (reader.peek() == JsonReader.Token.END_ARRAY) {
                        reader.endArray()
                        return emptyMap<Any, Any>()
                    } else {
                        throw JsonDataException(
                            "Expected END_ARRAY but was " + reader.peek()
                                    + " at path " + reader.path + " (empty array as map)"
                        )
                    }
                }

                return delegate.fromJson(reader);
            }

            override fun toJson(writer: JsonWriter, value: Map<*, *>?) {
                if (value == null) {
                    throw IllegalStateException("Wrap JsonAdapter with .nullSafe().");
                }
                delegate.toJson(writer, value)
            }

        }
    }
}

data class ShowDetails(val synopsis: List<String>, val sid: Int)

suspend fun OkHttpClient.fetchDetails(page: String): ShowDetails {

    val request = Request.Builder()
        .url("https://subsplease.org/shows/${page}")
        .build()
    val response = this.newCall(request).await()

    val text = response.body?.use(ResponseBody::string)

    return parseDetails(text ?: "")
}

internal fun parseDetails(html: String): ShowDetails {
    val doc = Jsoup.parse(html)
    val synopsis = doc.select(".series-syn p").toList().map { it.text() }
    val sid = doc.select("table#show-release-table").first().attr("sid").toInt()

    return ShowDetails(synopsis, sid)
}
