package eu.monniot.subpleaseapp.data

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.room.*
import eu.monniot.subpleaseapp.clients.subsplease.DownloadItem
import eu.monniot.subpleaseapp.clients.subsplease.SubsPleaseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneId

@Entity
@Immutable
data class Episode(
    val page: String,
    val title: String,
    @ColumnInfo(name = "seven") val magnet720: String,
    @ColumnInfo(name = "ten") val magnet1080: String,
    val date: LocalDate,
    val state: State = State.PENDING,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
) {

    companion object {

        fun fromDownloadItem(page: String, item: DownloadItem): Episode {
            val m7 = item.downloads.find { it.res == "720" }?.magnet ?: ""
            val m10 = item.downloads.find { it.res == "1080" }?.magnet ?: ""

            // TODO Should use a parser instead. Format is mm/dd/yy
            val components = item.releaseDate.split("/")
            val date = LocalDate.of(
                2000 + components[2].toInt(),
                components[0].toInt(),
                components[1].toInt()
            )

            return Episode(
                page,
                title = "${item.show} - ${item.episode}",
                magnet720 = m7,
                magnet1080 = m10,
                date
            )
        }


    }
}

enum class State {
    PENDING, ALERT, DONE
}

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episode WHERE page = :page")
    fun episodesForShow(page: String): Flow<List<Episode>>

    @Query("SELECT * FROM episode WHERE page = :page")
    suspend fun findByShow(page: String): List<Episode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)
}

class EpisodeStore(
    private val episodeDao: EpisodeDao,
    private val showDao: ShowDao,
    private val api: SubsPleaseApi,
) {

    fun episodes(page: String, forceRefresh: Boolean = false): Flow<List<Episode>> {

        val today = LocalDate.now()
        val timezone = ZoneId.systemDefault()

        Log.d(TAG, "episodes.page = $page")

        return flow {
            val initial = episodeDao.findByShow(page)
            emit(initial)

            val lastEp = initial.maxByOrNull { it.date }

            Log.d(
                TAG,
                "####> episodeStore.episodes.refresh = ${
                    forceRefresh || lastEp?.date?.isBefore(
                        today.minusDays(7)
                    ) != false
                }"
            )
            // Nothing in db (lastEp is null) => downloads
            // last ep is older than a week   => downloads
            // TODO If the season completed => no downloads
            if (forceRefresh || lastEp?.date?.isBefore(today.minusDays(7)) != false) {
                val show = showDao.findShowByPage(page)

                if (show.sid != null) {
                    val episodes = api.downloads(timezone, show.sid)
                    episodeDao.insertEpisodes(episodes.episode.values.map {
                        Episode.fromDownloadItem(
                            page,
                            it
                        )
                    })
                } else {
                    Log.w(
                        TAG,
                        "Tried to update episode list for show $page but no show.sid available"
                    )
                }
            }

            emitAll(episodeDao.episodesForShow(page))
        }
    }


    companion object {
        private val TAG = EpisodeStore::class.simpleName
    }
}
