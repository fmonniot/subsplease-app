package eu.monniot.subpleaseapp.ui.details


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dev.chrisbanes.accompanist.coil.CoilImage
import eu.monniot.subpleaseapp.clients.subsplease.DownloadItem
import eu.monniot.subpleaseapp.data.Show
import eu.monniot.subpleaseapp.data.ShowsStore
import eu.monniot.subpleaseapp.ui.theme.SubPleaseAppTheme
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

data class DetailsState(
    val show: Show?,
    val loadingShow: Boolean,
    val loadingSynopsis: Boolean,
    val loadingDownloads: Boolean,
    val downloads: List<DownloadItem>,
) {

    companion object {
        fun default(): DetailsState =
            DetailsState(
                null,
                loadingShow = true,
                loadingSynopsis = true,
                loadingDownloads = true,
                downloads = emptyList()
            )
    }
}

class ShowViewModel(
    private val showsStore: ShowsStore,
    private val showPage: String
) : ViewModel() {

    suspend fun load(): Show =
        showsStore.getShow(showPage)

    suspend fun downloads(): List<DownloadItem> =
        showsStore.listDownloads(showPage)

    suspend fun fetchAndSaveSynopsis(): String =
        showsStore.updateSynopsis(showPage)

}

/**
 * Stateful component containing the logic to fetch a show details.
 */
@Composable
fun DetailsScreen(
    viewModel: ShowViewModel,
    backButtonPress: () -> Unit, // TODO Should it be included in the ViewModel ?
) {

    val state by produceState(initialValue = DetailsState.default(), viewModel) {

        val show = viewModel.load()
        val loadingSynopsis = show.synopsis == null
        value = value.copy(show = show, loadingShow = false, loadingSynopsis = loadingSynopsis)

        if (loadingSynopsis) {
            launch {
                val synopsis = viewModel.fetchAndSaveSynopsis()

                value = value.copy(
                    loadingSynopsis = false,
                    show = value.show?.copy(synopsis = synopsis)
                )
            }
        }

        launch {
            val list = viewModel.downloads()

            value = value.copy(loadingDownloads = false, downloads = list)

        }
    }

    DetailsScreen(state, backButtonPress)
}

private val TitleHeight = 128.dp
private val GradientScroll = 180.dp
private val ImageOverlap = 115.dp
private val MinTitleOffset = 56.dp
private val MinImageOffset = 12.dp
private val MaxTitleOffset = ImageOverlap + MinTitleOffset + GradientScroll
private val ExpandedImageSize = 300.dp
private val CollapsedImageSize = 150.dp
private val HzPadding = Modifier.padding(horizontal = 24.dp)

@Composable
fun DetailsScreen(
    state: DetailsState,
    backButtonPress: () -> Unit,
) {

    Box(Modifier.fillMaxSize()) {
        val scroll = rememberScrollState()

        Header()
        Body(state, scroll)
        Title(state, scroll.value)
        Image(state, scroll.value)
        Back(backButtonPress)

    }
}

// TODO Maybe get the color from the image itself ?
@Composable
fun Header() {
    Spacer(
        modifier = Modifier
            .preferredHeight(280.dp) // TODO Why not GradientScroll
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xff86f7fa),
                        Color(0xff9b86fa)
                    )
                )
            )
    )
}

// TODO Use material color for button background color
@Composable
fun Back(onClick: () -> Unit) {
    IconButton(
        onClick = onClick, modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .preferredSize(36.dp)
            .background(
                color = Color(0xff121212).copy(alpha = 0.32f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xffffffff)
        )
    }
}

@Composable
fun Body(state: DetailsState, scroll: ScrollState) {
    Column {
        // Minimum height when the header is minimized
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .preferredHeight(MinTitleOffset)
        )

        Column(modifier = Modifier.verticalScroll(scroll)) {
            // Space taken by the Header() component (gradient)
            Spacer(modifier = Modifier.preferredHeight(GradientScroll))

            // We want to paint a surface on top of the gradient when we scroll up.
            // Because of how compose works, we then have to put the entire content within
            // this surface. Or if we could put exact dimension I guess we could Surface only
            // the first spacers ?
            Surface(Modifier.background(MaterialTheme.colors.surface)) {
                Column {
                    Spacer(Modifier.preferredHeight(ImageOverlap))
                    Spacer(Modifier.preferredHeight(TitleHeight))

                    // Actual content
                    Spacer(Modifier.preferredHeight(16.dp))
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.overline,
                        //color = JetsnackTheme.colors.textHelp,
                        modifier = HzPadding
                    )
                    Spacer(Modifier.preferredHeight(4.dp))
                    Text(
                        text = "This is the story of Ai, an introverted girl whose fate is forever changed when she acquires a mysterious “Wonder Egg” from a deserted arcade. That night, her dreams blend into reality, and as other girls obtain their own Wonder Eggs, Ai discovers new friends—and the magic within herself.",
                        style = MaterialTheme.typography.body1,
                        //color = JetsnackTheme.colors.textHelp,
                        modifier = HzPadding
                    )

                    Spacer(Modifier.preferredHeight(40.dp))
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.overline,
                        //color = JetsnackTheme.colors.textHelp,
                        modifier = HzPadding
                    )
                    Spacer(Modifier.preferredHeight(4.dp))
                    Box(Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    (1..5).forEach { episode ->
                        ListItem(
                            text = { Text("Episode 0$episode") },
                            secondaryText = {
                                Row {
                                    Text("Jan $episode, 2021")
                                    if (episode == 1) {
                                        Text(
                                            "New",
                                            style = MaterialTheme.typography.overline,
                                            color = Color.Green
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // TODO Remove once the bottom bar has been removed from this screen
                    Spacer(
                        modifier = Modifier
                            .padding(bottom = 56.dp)
                            .preferredHeight(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Title(state: DetailsState, scroll: Float) {
    // We clamp the offset between two position
    val maxOffset = with(AmbientDensity.current) { MaxTitleOffset.toPx() }
    val minOffset = with(AmbientDensity.current) { MinTitleOffset.toPx() }
    val offset = (maxOffset - scroll).coerceAtLeast(minOffset)

    // We also adjust the padding on the image side as it moves.
    // TODO Use a non-linear function as the image itself doesn't move horizontally linearly
    // with the scroll position.
    val collapseRange = with(AmbientDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFraction = (scroll / collapseRange).coerceIn(0f, 1f)
    val endPadding = (CollapsedImageSize + 5.dp) * collapseFraction

    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .preferredHeight(TitleHeight)
            .graphicsLayer(translationY = offset)
            .background(color = MaterialTheme.colors.background)
            .padding(end = endPadding)
            .border(1.dp, Color.Red)
    ) {
        Text(
            text = "Unravel by TK from from",
            style = MaterialTheme.typography.h4,
            //color = JetsnackTheme.colors.textSecondary,
            modifier = HzPadding
        )
    }
}

// TODO Adjust constant to work with a rectangle instead of a square image
// Of interest, the horizontal positioning is not ideal at the moment
@Composable
fun Image(state: DetailsState, scroll: Float) {
    val collapseRange = with(AmbientDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFraction = (scroll / collapseRange).coerceIn(0f, 1f)

    CollapsingImageLayout(
        collapseFraction = collapseFraction,
        modifier = HzPadding
    ) {
        CoilImage(
            data = "https://subsplease.org/wp-content/uploads/2021/01/110750.jpg",
            contentDescription = null,
            contentScale = ContentScale.FillHeight
        )
    }
}

@Composable
private fun CollapsingImageLayout(
    collapseFraction: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        check(measurables.size == 1)

        val imageMaxSize = min(ExpandedImageSize.toIntPx(), constraints.maxWidth)
        val imageMinSize = max(CollapsedImageSize.toIntPx(), constraints.minWidth)
        val imageWidth = lerp(imageMaxSize, imageMinSize, collapseFraction)
        val imagePlaceable = measurables[0].measure(Constraints.fixed(imageWidth, imageWidth))

        val imageY = lerp(MinTitleOffset, MinImageOffset, collapseFraction).toIntPx()
        val imageX = lerp(
            (constraints.maxWidth - imageWidth) / 2, // centered when expanded
            constraints.maxWidth - imageWidth, // right aligned when collapsed
            collapseFraction
        )

        // width=948 / height = 979

        layout(
            width = constraints.maxWidth,
            height = imageY + imageWidth
        ) {
            imagePlaceable.place(imageX, imageY)
        }
    }
}

//
// Previews
//

@Preview
@Composable
fun LoadingShow() {
    val state = DetailsState.default()
    SubPleaseAppTheme {
        Surface {
            DetailsScreen(state, backButtonPress = { })
        }
    }
}

// TODO fun LoadedShow (synopsis loading + downloads loading)
// TODO fun LoadedSynopsis (synopsis loaded + downloads loading)
// TODO fun FullyLoaded (synopsis loaded + downloads loaded)
