package eu.monniot.subpleaseapp.data

import androidx.compose.runtime.Immutable
import androidx.room.*


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

