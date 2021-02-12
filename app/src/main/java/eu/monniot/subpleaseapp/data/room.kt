package eu.monniot.subpleaseapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eu.monniot.subpleaseapp.clients.subsplease.DownloadItem
import eu.monniot.subpleaseapp.clients.subsplease.SubsPleaseApi
import eu.monniot.subpleaseapp.clients.subsplease.fetchDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
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

    @Query("SELECT * FROM show WHERE page = :page")
    suspend fun findShowByPage(page: String): Show?

    // replace because we assume new data has more chance to be true
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShows(users: List<Show>)

    @Query("UPDATE show SET subscribed = :subscribed WHERE page = :page")
    suspend fun updateShowSubscription(page: String, subscribed: Boolean)

    @Query("UPDATE show SET synopsis = :synopsis, sid = :sid WHERE page = :page")
    suspend fun updateShowSynopsis(page: String, synopsis: String, sid: Int)
}

@Database(entities = [Show::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao

    companion object {

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE show ADD COLUMN sid INTEGER")
            }
        }

        fun build(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "app-database"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }

    }
}

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

    suspend fun updateDetails(page: String): Pair<String, Int> {
        val details = okHttpClient.fetchDetails(page)

        // TODO Need to change the data model to handle list of strings (list of paragraphs)
        // Maybe using a TypeConverter to use <br> as an internal separator ?
        val synopsis: String = details.synopsis.joinToString("<br>")

        showDao.updateShowSynopsis(page, synopsis, details.sid)

        return Pair(synopsis, details.sid)
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
