package eu.monniot.subpleaseapp.ui.details


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.ViewModel
import eu.monniot.subpleaseapp.clients.deluge.DownloadItem
import eu.monniot.subpleaseapp.data.Show
import eu.monniot.subpleaseapp.data.ShowsStore
import kotlinx.coroutines.launch

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

    suspend fun load(): Show = TODO()

    suspend fun downloads(): List<DownloadItem> = TODO()

    suspend fun fetchAndSaveSynopsis(): String = TODO()

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

@Composable
fun DetailsScreen(
    state: DetailsState,
    backButtonPress: () -> Unit,
) {

}
