package eu.monniot.subpleaseapp.ui.shows

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.monniot.subpleaseapp.data.Show
import eu.monniot.subpleaseapp.data.ShowsStore
import kotlinx.coroutines.launch


class SubscriptionsViewModel(private val showsStore: ShowsStore): ViewModel() {

    private val _state: MutableState<Map<String, List<Show>>> = mutableStateOf(emptyMap())

    val state: State<Map<String, List<Show>>>
        get() = _state

    init {
        viewModelScope.launch {
            _state.value = showsStore.subscriptions()
        }
    }

    fun toggleShowSubscription(show: Show) {
        viewModelScope.launch {
            if(show.subscribed) {
                showsStore.unsubscribeToShow(show.page)
            } else {
                showsStore.subscribeToShow(show.page)
            }

            // Update the state
            updateShowLocalState(show.copy(subscribed = !show.subscribed))
        }
    }

    private fun updateShowLocalState(show: Show) {
        val list = _state.value[show.releaseDay]
        val newList = list?.map { if(it.page == show.page) show else it} ?: emptyList()
        val newMap = _state.value + Pair(show.releaseDay, newList)


        _state.value = newMap
    }
}

/**
 * Stateful component managing the subscriptions screen.
 *
 * @param navigateShowDetail request navigation to show's page
 * @param viewModel data source for shows
 */
@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionsViewModel,
    navigateShowDetail: (String) -> Unit,
) {
    val schedule by remember { viewModel.state }

    ShowsScreen(
        navigateShowDetail = navigateShowDetail,
        schedule = schedule,
        toggleShowSubscription = viewModel::toggleShowSubscription
    )
}
