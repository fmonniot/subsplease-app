package eu.monniot.subpleaseapp.data

import eu.monniot.subpleaseapp.clients.subsplease.DownloadItem
import eu.monniot.subpleaseapp.clients.subsplease.SubsPleaseApi
import eu.monniot.subpleaseapp.clients.subsplease.fetchDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields


class ShowsStore(
    private val showDao: ShowDao,
    private val api: SubsPleaseApi,
    private val okHttpClient: OkHttpClient
) {

    private var refreshingJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    suspend fun getShow(page: String): Show {
        val show = showDao.findShowByPage(page)

        if (show == null) {
            throw TODO("What to do here ?")
        } else {
            return show
        }
    }

    suspend fun updateDetails(page: String): Pair<List<String>, Int> {
        // TODO Perhaps make that an interface/function to ease testing ?
        val details = okHttpClient.fetchDetails(page)

        // Join manually because room will infer something else for lists
        showDao.updateShowSynopsis(page, StringListConverters.join(details.synopsis), details.sid)

        return Pair(details.synopsis, details.sid)
    }

    suspend fun listDownloads(sid: Int): List<DownloadItem> {
        val timezone = ZoneId.systemDefault()
        val response = api.downloads(timezone, sid)

        return response.episode.values.toList()
    }

    suspend fun subscribeToShow(page: String) {
        showDao.updateShowSubscription(page, true)
    }

    suspend fun unsubscribeToShow(page: String) {
        showDao.updateShowSubscription(page, false)
    }

    suspend fun subscriptions(): Map<String, List<Show>> {
        return showDao
            .findSubscriptionsBySeason(currentSeason())
            .groupBy(Show::releaseDay)
    }

    fun schedule(forceRefresh: Boolean = false): Flow<Map<String, List<Show>>> {

        val season = currentSeason()
        val timezone = ZoneId.systemDefault()

        return flow {

            // We need access to what is in the db for the update logic.
            // Emit it while we are at it
            val init = showDao.findAllBySeason(season)
            emit(init)

            // go through if no refresh in progress and we don't have any data (or we force)
            if (refreshingJob?.isActive != true && (init.isEmpty() || forceRefresh)) {

                refreshingJob = scope.launch {

                    val newShows = api
                        .schedule(timezone)
                        .schedule
                        .flatMap { (releaseDay, shows) ->
                            shows.map { show ->
                                Show(
                                    page = show.page,
                                    time = show.time,
                                    title = show.title,
                                    imageUrl = show.imageUrl,
                                    releaseDay = releaseDay,
                                    season = season
                                )
                            }
                        }

                    showDao.insertShows(newShows)
                    // done, the outer flow should reflect the changes
                }
            }

            // Do emit further changes
            emitAll(showDao.subscribeToAllBySeason(season))

        }.map { it.groupBy(Show::releaseDay) }
    }

    companion object {
        fun currentSeason(): String {
            val zdt = ZonedDateTime.now()
            val quarter = zdt.get(IsoFields.QUARTER_OF_YEAR)
            val year = zdt.get(ChronoField.YEAR)

            return "${year}Q$quarter"
        }
    }
}
