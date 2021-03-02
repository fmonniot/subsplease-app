package eu.monniot.subpleaseapp.ui.shows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.monniot.subpleaseapp.data.Show
import eu.monniot.subpleaseapp.data.ShowsStore
import eu.monniot.subpleaseapp.scheduling.AlertScheduling
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*


class ScheduleViewModel(
    private val showsStore: ShowsStore,
    private val scheduling: AlertScheduling
) : ViewModel() {

    val schedule: Flow<Map<String, List<Show>>>
        get() = showsStore.schedule()

    val today: String by lazy {
        ZonedDateTime.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    fun toggleShowSubscription(show: Show) {
        viewModelScope.launch {
            if (show.subscribed) {
                showsStore.unsubscribeToShow(show.page)
            } else {
                showsStore.subscribeToShow(show.page)
            }

            scheduling.schedule()
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
        today = viewModel.today,
        toggleShowSubscription = viewModel::toggleShowSubscription
    )
}
