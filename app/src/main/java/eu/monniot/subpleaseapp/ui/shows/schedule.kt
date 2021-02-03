package eu.monniot.subpleaseapp.ui.shows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.monniot.subpleaseapp.data.Show
import eu.monniot.subpleaseapp.data.ShowsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class ScheduleViewModel(private val showsStore: ShowsStore): ViewModel() {

    val schedule: Flow<Map<String, List<Show>>>
        get() = showsStore.schedule()

    fun toggleShowSubscription(show: Show) {
        viewModelScope.launch {
            if(show.subscribed) {
                showsStore.unsubscribeToShow(show.page)
            } else {
                showsStore.subscribeToShow(show.page)
            }
        }
    }
}

/**
 * Stateful component managing the schedule screen
 *
 * @param navigateShowDetail request navigation to show's page
 * @param viewModel data source for shows
 */
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    navigateShowDetail: (String) -> Unit,
) {
    val schedule by viewModel.schedule.collectAsState(initial = emptyMap())

    ShowsScreen(
        navigateShowDetail = navigateShowDetail,
        schedule = schedule,
        toggleShowSubscription = viewModel::toggleShowSubscription
    )
}
