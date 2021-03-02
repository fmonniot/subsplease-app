package eu.monniot.subpleaseapp.ui.shows

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.monniot.subpleaseapp.data.Show
import eu.monniot.subpleaseapp.data.ShowsStore
import eu.monniot.subpleaseapp.scheduling.AlertScheduling
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*


// TODO Find a way to have common code between ScheduleViewModel and this
// toggle subscription is the same, and with the AlarmManager being added
// on top of it, we will want some shared logic.
class SubscriptionsViewModel(
    private val showsStore: ShowsStore,
    private val scheduling: AlertScheduling
) : ViewModel() {

    // As opposed to schedule, we don't use a Flow because we want to keep unsubscribed
    // items in view for the user to correct a misclick.
    private val _state: MutableState<Map<String, List<Show>>> = mutableStateOf(emptyMap())

    val state: State<Map<String, List<Show>>>
        get() = _state

    val today: String by lazy {
        ZonedDateTime.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    init {
        viewModelScope.launch {
            _state.value = showsStore.subscriptions()
        }
    }

    fun toggleShowSubscription(show: Show) {
        viewModelScope.launch {
            if (show.subscribed) {
                showsStore.unsubscribeToShow(show.page)
            } else {
                showsStore.subscribeToShow(show.page)
            }

            // Update the state
            updateShowLocalState(show.copy(subscribed = !show.subscribed))

            scheduling.schedule()
        }
    }

    private fun updateShowLocalState(show: Show) {
        val list = _state.value[show.releaseDay]
        val newList = list?.map { if (it.page == show.page) show else it } ?: emptyList()
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
        today = viewModel.today,
        toggleShowSubscription = viewModel::toggleShowSubscription
    )
}
