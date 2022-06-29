package eu.monniot.subpleaseapp.ui.downloads

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import eu.monniot.subpleaseapp.R
import eu.monniot.subpleaseapp.clients.deluge.DelugeClient


private const val TAG = "DownloadsScreen"

/**
 * Stateful component to handle the download screen
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloadsScreen(client: DelugeClient) {
    val (torrents, setTorrents) = remember { mutableStateOf(listOf<DelugeClient.Companion.Torrent>()) }

    LaunchedEffect(key1 = Unit) {
        val login = client.login()
        Log.d(TAG, "login: $login")

        val result = client.listTorrents()
        Log.d(TAG, "list: $result")

        // Real error management, with visual
        val list = result.get()
        if (list != null) {
            setTorrents(list)
        }
    }


    LazyColumn(modifier = Modifier.padding(top = 16.dp, bottom = 56.dp)) {
        items(torrents) { torrent ->
            ListItem(
                icon = {
                    // TODO Change icon when at 100% (done)
                    DownloadIcon(percent = torrent.progress.toFloat())

                    /* Something like that for complete icon
                    Icon(
                        Icons.Outlined.Done,
                        contentDescription = "Torrent downloaded"
                    )
                    */
                },
                text = { Text(torrent.name) },
                secondaryText = {
                    // Change text based on done or not
                    // Change text based on paused or not

                    // TODO Clip Float to two digits
                    Text("${torrent.progress}% - ETA: ${torrent.eta} - Progress: ${torrent.progress}")
                }
            )
        }
    }
}
