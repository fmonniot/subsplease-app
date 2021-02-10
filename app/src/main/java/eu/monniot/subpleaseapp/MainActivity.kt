package eu.monniot.subpleaseapp

import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.room.Room
import eu.monniot.subpleaseapp.clients.deluge.DelugeClient
import eu.monniot.subpleaseapp.clients.subsplease.SubsPleaseApi
import eu.monniot.subpleaseapp.data.AppDatabase
import eu.monniot.subpleaseapp.data.ShowsStore
import eu.monniot.subpleaseapp.ui.details.DetailsScreen
import eu.monniot.subpleaseapp.ui.details.ShowViewModel
import eu.monniot.subpleaseapp.ui.downloads.DownloadsScreen
import eu.monniot.subpleaseapp.ui.settings.SettingsScreen
import eu.monniot.subpleaseapp.ui.settings.openSharedPrefs
import eu.monniot.subpleaseapp.ui.settings.string
import eu.monniot.subpleaseapp.ui.shows.ScheduleScreen
import eu.monniot.subpleaseapp.ui.shows.ScheduleViewModel
import eu.monniot.subpleaseapp.ui.shows.SubscriptionsScreen
import eu.monniot.subpleaseapp.ui.shows.SubscriptionsViewModel
import eu.monniot.subpleaseapp.ui.theme.SubPleaseAppTheme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val items = listOf(
            Screen.Schedule,
            Screen.Subscriptions,
            Screen.Downloads,
            Screen.Settings
        )

        val http = OkHttpClient.Builder()
            .addInterceptor(run {
                val h = HttpLoggingInterceptor()
                h.level = HttpLoggingInterceptor.Level.BODY
                h
            })
            .build()
        val api = SubsPleaseApi.build(http)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app-database"
        ).build()

        val store = ShowsStore(db.showDao(), api)
        var first = true

        setContent {
            SubPleaseAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = {
                            BottomNavigation {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                // TODO Need to understand what it means for this to be null. Is it
                                // safe to assume the first screen shown (for use /schedule) ?
                                val currentRoute =
                                    navBackStackEntry?.arguments?.getString(KEY_ROUTE)
                                        ?: Screen.Subscriptions.route
                                Log.d("currentRoute = ", currentRoute)
                                items.forEach { screen ->
                                    BottomNavigationItem(
                                        icon = {
                                            Icon(
                                                vectorResource(screen.iconId),
                                                contentDescription = screen.label
                                            )
                                        },
                                        label = { Text(screen.label) },
                                        selected = currentRoute == screen.route,
                                        alwaysShowLabels = false,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                // Pop up to the start destination of the graph to
                                                // avoid building up a large stack of destinations
                                                // on the back stack as users select items
                                                popUpTo = navController.graph.startDestination
                                                // Avoid multiple copies of the same destination when
                                                // reselecting the same item
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) {

                        NavHost(navController, startDestination = Screen.Subscriptions.route) {
                            composable(Screen.Schedule.route) {
                                val scheduleViewModel = ScheduleViewModel(store)

                                ScheduleScreen(scheduleViewModel) { page ->
                                    navController.navigate("/details/$page")
                                }
                            }

                            composable(Screen.Subscriptions.route) {
                                val subscriptionsViewModel = SubscriptionsViewModel(store)
                                if(first){
                                    navController.navigate("/details/wonder-egg-priority")
                                    first = false
                                }

                                SubscriptionsScreen(
                                    viewModel = subscriptionsViewModel,
                                    navigateShowDetail = { page -> navController.navigate("/details/$page") })
                            }

                            composable(Screen.Downloads.route) {
                                val context = AmbientContext.current
                                val preferences = openSharedPrefs(context)

                                // TODO Use values through remember and accessor
                                val host = preferences.string("deluge_host").value().value
                                val user = preferences.string("deluge_username").value().value
                                val pass = preferences.string("deluge_password").value().value

                                if (host != null && user != null && pass != null) {
                                    val client = DelugeClient(host, user, pass, http)

                                    DownloadsScreen(client = client)

                                } else {
                                    Text("Set up server configuration in the Settings please")
                                }

                            }

                            composable(Screen.Settings.route) {
                                SettingsScreen()
                            }

                            composable(
                                "/details/{page}",
                                arguments = listOf(navArgument("page") {
                                    type = NavType.StringType
                                })
                            ) {
                                // The null cast should be safe because arguments are required before hand
                                val page = it.arguments?.getString("page")!!
                                val vm = ShowViewModel(store, page)
                                DetailsScreen(viewModel = vm, backButtonPress = {
                                    navController.popBackStack()
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, @DrawableRes val iconId: Int) {
    object Schedule : Screen("/schedule", "Schedule", R.drawable.ic_baseline_today_24)
    object Subscriptions : Screen("/subscriptions", "My Subs", R.drawable.ic_baseline_view_list_24)
    object Downloads : Screen("/downloads", "Downloads", R.drawable.ic_baseline_get_app_24)
    object Settings : Screen("/settings", "Settings", R.drawable.ic_baseline_settings_24)
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SubPleaseAppTheme {
        Greeting("Android")
    }
}