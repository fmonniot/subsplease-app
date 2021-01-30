package eu.monniot.subpleaseapp.clients.subsplease

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import eu.monniot.subpleaseapp.clients.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.LocalDate

// HorribleSubs APIs
object HorribleSubs {

    fun latestShowUpdates(today: LocalDate): ApiCallback<List<ShowUpdate>> {
        return { client, cb ->
            // TODO Missing part of the url
            client.execute("https://horriblesubs.info/api.php?method=getlatest") {
                cb(it.map { content ->
                    Parsers.parseLatestShows(
                        content,
                        today
                    )
                })
            }
        }
    }

    fun showDetails(slug: String): ApiCallback<Show> {
        return { client, cb ->
            client.execute("https://horriblesubs.info/shows/$slug/") { result ->
                cb(result.flatMap { content ->
                    Parsers.parseShowDetails(content)?.let {
                        Result.Success(
                            it
                        )
                    } ?: Result.ParsingError(
                        "Can't parse"
                    )
                })
            }
        }
    }

    fun showEpisodes(showId: Int, today: LocalDate): ApiCallback<List<ShowEpisode>> {
        return { client, cb ->
            client.execute("https://horriblesubs.info/api.php?method=getshows&type=show&showid=$showId") {
                cb(it.map { content ->
                    Parsers.parseShowEpisodes(
                        content,
                        today
                    )
                })
            }
        }
    }

    fun currentSeason(): ApiCallback<List<SeasonalShow>> {
        return { client, cb->
            client.execute("https://horriblesubs.info/current-season/") {
                cb(it.map { content ->
                    Parsers.parseCurrentSeasonShows(
                        content
                    )
                })
            }
        }
    }

    fun image(rawUrl: String): ApiCallback<Bitmap> {
        return { client, cb ->
            val url = when {
                rawUrl.startsWith("http:") -> rawUrl
                rawUrl.startsWith("https:") -> rawUrl
                else -> "http://horriblesubs.com$rawUrl"
            }

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cb(
                        Result.NetworkError(
                            e,
                            call
                        )
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.code != 200) return cb(
                            Result.NonOkError(
                                response.code,
                                response.body?.string().orEmpty()
                            )
                        )
                        else {
                            cb(
                                Result.Success(
                                    BitmapFactory.decodeStream(response.body!!.byteStream())
                                )
                            )
                        }
                    }
                }
            })
        }
    }

}