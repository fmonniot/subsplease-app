package eu.monniot.subpleaseapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@TypeConverters(StringListConverters::class, LocalDateConverter::class, EpisodeStateConverter::class)
@Database(entities = [Show::class, Episode::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao

    abstract fun episodeDao(): EpisodeDao

    companion object {

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE show ADD COLUMN sid INTEGER")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `Episode` (`page` TEXT NOT NULL, `title` TEXT NOT NULL, `seven` TEXT NOT NULL, `ten` TEXT NOT NULL, `date` TEXT NOT NULL, `state` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
            }
        }

        fun build(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "app-database"
            )
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build()
        }

    }
}

class LocalDateConverter {

    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yy")

    @TypeConverter
    fun fromDate(value: LocalDate?) = value?.format(formatter)

    @TypeConverter
    fun toDate(value: String?) = if(value != null) LocalDate.parse(value, formatter) else null

}

class EpisodeStateConverter {
    @TypeConverter
    fun fromState(state: State) = state.name

    @TypeConverter
    fun toState(value: String) = State.valueOf(value)
}
