package eu.monniot.subpleaseapp.ui.shows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.coil.CoilImage
import eu.monniot.subpleaseapp.data.Show
import eu.monniot.subpleaseapp.ui.theme.SubPleaseAppTheme


/**
 * Stateless screen displays the content of `schedule`
 */
@Composable
fun ShowsScreen(
    navigateShowDetail: (String) -> Unit,
    toggleShowSubscription: (Show) -> Unit,
    schedule: Map<String, List<Show>>,
) {

    LazyColumn(modifier = Modifier.padding(top = 16.dp, bottom = 56.dp)) {

        items(schedule.toList()) { (day, shows) ->
            Text(
                text = day,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.subtitle1
            )

            shows.forEach { show ->
                ShowItem(
                    showTitle = show.title,
                    imageUrl = show.imageUrl,
                    subscribed = show.subscribed,
                    onClick = { navigateShowDetail(show.page) },
                    onSubscribeToggle = { toggleShowSubscription(show) }
                )
                ShowDivider()
            }
        }
    }
}

// TODO Use ListItem and also add the time on second line. See how it behaves with very long title.
@Composable
private fun ShowItem(
    showTitle: String,
    imageUrl: String,
    subscribed: Boolean,
    onClick: () -> Unit,
    onSubscribeToggle: () -> Unit,
) {
    Row(modifier = Modifier
        .clickable(onClickLabel = "More details") { onClick() }
        .padding(horizontal = 16.dp)) {

        CoilImage(
            data = "https://subsplease.org${imageUrl}",
            contentDescription = "",
            fadeIn = true,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .preferredSize(56.dp, 78.dp) // 225w/317h | 56dp/78dp
                .clip(RoundedCornerShape(2.dp))
        )

        Text(
            showTitle,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(16.dp)
                .weight(1f),
            style = MaterialTheme.typography.body1
        )

        SelectTopicButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            selected = subscribed,
            onClick = onSubscribeToggle
        )
    }
}

@Composable
fun SelectTopicButton(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val icon = if (selected) Icons.Filled.Done else Icons.Filled.Add
    val backgroundColor = if (selected) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    }
    Surface(
        color = backgroundColor,
        shape = CircleShape,
        modifier = modifier
            .preferredSize(36.dp, 36.dp)
            .clickable { onClick() }
    ) {
        Icon(icon, contentDescription = null)
    }
}


/**
 * Full-width divider
 */
@Composable
private fun ShowDivider() {
    // Start is 56.dp for the image plus 16.dp for the left padding
    Divider(
        modifier = Modifier.padding(start = 72.dp, top = 8.dp, bottom = 8.dp),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
    )
}

@Preview
@Composable
fun ShowSubscribedPreview() {
    SubPleaseAppTheme {
        Surface {
            ShowItem(
                showTitle = "Horimiya",
                imageUrl = "/wp-content/uploads/2021/01/110336.jpg",
                subscribed = true,
                onClick = { },
                onSubscribeToggle = { })
        }
    }
}

@Preview
@Composable
fun ShowNotSubscribedPreview() {
    SubPleaseAppTheme {
        Surface {
            ShowItem(
                showTitle = "Horimiya",
                imageUrl = "/wp-content/uploads/2021/01/110336.jpg",
                subscribed = false,
                onClick = { },
                onSubscribeToggle = { })
        }
    }
}

@Preview
@Composable
fun ShowSubscribedLongNamePreview() {
    SubPleaseAppTheme {
        Surface {
            ShowItem(
                showTitle = "Tatoeba Last Dungeon Mae no Mura no Shounen ga Joban no Machi de Kurasu Youna Monogatari",
                imageUrl = "/wp-content/uploads/2021/01/106599.jpg",
                subscribed = true,
                onClick = { },
                onSubscribeToggle = { })
        }
    }
}

@Preview
@Composable
fun ShowsPreview() {
    val tatoeba = Show(
        page = "tatoeba-last-dungeon-mae-no-mura-no-shounen-ga-joban-no-machi-de-kurasu-youna-monogatari",
        time = "06:30",
        title = "Tatoeba Last Dungeon Mae no Mura no Shounen ga Joban no Machi de Kurasu Youna Monogatari",
        imageUrl = "/wp-content/uploads/2021/01/106599.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )

    val tensei = Show(
        page = "tensei-shitara-slime-datta-ken",
        time = "08:00",
        title = "Tensei Shitara Slime Datta Ken",
        imageUrl = "/wp-content/uploads/2021/01/93337.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )

    val horizon = Show(
        page = "log-horizon-s3",
        time = "04:00",
        title = "Log Horizon S3",
        imageUrl = "/wp-content/uploads/2021/01/108026.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )
    val yuru = Show(
        page = "yuru-camp-s2",
        time = "08:00",
        title = "Yuru Camp S2",
        imageUrl = "/wp-content/uploads/2021/01/110636.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )
    val tenchi = Show(
        page = "tenchi-souzou-design-bu",
        time = "08:30",
        title = "Tenchi Souzou Design-bu",
        imageUrl = "/wp-content/uploads/2021/01/109865.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )
    val jaku = Show(
        page = "jaku-chara-tomozaki-kun",
        time = "04:30",
        title = "Jaku-Chara Tomozaki-kun",
        imageUrl = "/wp-content/uploads/2021/01/109232.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )
    val horimiya = Show(
        page = "horimiya",
        time = "09:00",
        title = "Horimiya",
        imageUrl = "/wp-content/uploads/2021/01/110336.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )
    val nonNon = Show(
        page = "non-non-biyori-nonstop",
        time = "09:35",
        title = "Non Non Biyori Nonstop",
        imageUrl = "/wp-content/uploads/2021/01/107670.jpg",
        synopsis = null,
        releaseDay = "",
        season = ""
    )

    val schedule = mapOf(
        "Monday" to listOf(tatoeba),
        "Tuesday" to listOf(tensei),
        "Wednesday" to listOf(horizon),
        "Thursday" to listOf(yuru, tenchi),
        "Friday" to listOf(jaku),
        "Saturday" to listOf(horimiya),
        "Sunday" to listOf(nonNon),
    )

    SubPleaseAppTheme {
        Surface {
            ShowsScreen({}, {}, schedule)
        }
    }
}
