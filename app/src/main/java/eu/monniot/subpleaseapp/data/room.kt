package eu.monniot.subpleaseapp.data


import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    val synopsis: String? = null,
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
