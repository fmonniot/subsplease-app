package eu.monniot.subpleaseapp.data

import androidx.room.*
import eu.monniot.subpleaseapp.clients.subsplease.SubsPleaseApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields


@Dao
interface ShowDao {

    @Query("SELECT * FROM show WHERE season = :season")
    fun subscribeToAllBySeason(season: String): Flow<List<Show>>

    @Query("SELECT * FROM show WHERE season = :season AND subscribed = true")
    suspend fun findSubscriptionsBySeason(season: String): List<Show>

    @Query("SELECT * FROM show WHERE season = :season")
    suspend fun findAllBySeason(season: String): List<Show>

    // replace because we assume new data has more chance to be true
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShows(users: List<Show>)

    @Query("UPDATE show SET subscribed = :subscribed WHERE page = :page")
    suspend fun updateShow(page: String, subscribed: Boolean)

}

@Database(entities = [Show::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
}

class ShowsStore(private val showDao: ShowDao, private val api: SubsPleaseApi) {

    private var refreshingJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    suspend fun subscribeToShow(page: String) {
        println("subscribe to $page")
        showDao.updateShow(page, true)
    }

    suspend fun unsubscribeToShow(page: String) {
        println("unsubscribe to $page")
        showDao.updateShow(page, false)
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

            println("ShowsStore#schedule")

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
