package eu.monniot.subpleaseapp.ui.downloads

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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


/**
 * Stateful component to handle the download screen
 */
@Composable
fun DownloadsScreen(client: DelugeClient) {
    val (torrents, setTorrents) = remember { mutableStateOf(listOf<DelugeClient.Companion.Torrent>()) }

    LaunchedEffect(key1 = Unit) {
        val login = client.login()
        Log.d("subapp.downloads", "login: $login")

        val result = client.listTorrents()
        Log.d("subapp.downloads", "list: $result")

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
                    // TODO Understand how to fill ImageVector based on a percentage
                    // TODO Change icon when at 100% (done)
                    Icon(
                        vectorResource(R.drawable.ic_outline_get_app_24),
                        contentDescription = "${torrent.progress} downloaded"
                    )
                },
                text = { Text(torrent.name) },
                secondaryText = {
                    // Change text based on done or not
                    // Change text based on paused or not

                    Text("${torrent.progress}% - ETA: ${torrent.eta} - Progress: ${torrent.progress}")
                }
            )
        }
    }
}
