package eu.monniot.subpleaseapp.data


import androidx.compose.runtime.Immutable
import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Entity
@Immutable
data class Show(
    @PrimaryKey val page: String,
    val time: String,
    val title: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "release_day") val releaseDay: String,
    val season: String, // 21Q1 for winter 2021; 22Q3 for autumn 2022
    val synopsis: List<String>? = null,
    val sid: Int? = null,
    val subscribed: Boolean = false,
)

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

    // Can't use a List<String> here because room will not call the type converter but will
    // try to inject the values individually…
    @Query("UPDATE show SET synopsis = :synopsis, sid = :sid WHERE page = :page")
    suspend fun updateShowSynopsis(page: String, synopsis: String, sid: Int)
}

/**
 * Store List<String> as a String in our database. We are using the <br> delimiter
 * to know when a String start and end. We don't expect to store HTML values so it
 * _should_ be a safe value.
 */
class StringListConverters {
    @TypeConverter
    fun fromList(values : List<String>?) = if (values != null) join(values) else null

    @TypeConverter
    fun toList(value: String?) = value?.split("<br>")

    companion object {
        fun join(values: List<String>) = values.joinToString("<br>")
    }
}
