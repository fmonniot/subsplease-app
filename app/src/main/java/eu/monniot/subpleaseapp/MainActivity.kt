package eu.monniot.subpleaseapp

import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import eu.monniot.subpleaseapp.ui.theme.SubPleaseAppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val items = listOf(
            Screen.Schedule,
            Screen.Subscriptions,
            Screen.Downloads,
            Screen.Settings
        )

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
                                        ?: "/schedule"
                                Log.d("currentRoute = ", currentRoute ?: "null")
                                items.forEach { screen ->
                                    BottomNavigationItem(
                                        icon = {
                                            val image = loadVectorResource(id = screen.iconId)
                                            //loadVectorResource will load the vector image asynchronous
                                            image.resource.resource?.let {
                                                Icon(it, contentDescription = screen.label)
                                            }
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

                        NavHost(navController, startDestination = Screen.Schedule.route) {
                            composable(Screen.Schedule.route) {
                            }

                            composable(Screen.Subscriptions.route) { Text("Active Subscriptions") }
                            composable(Screen.Downloads.route) { Text("Downloads") }
                            composable(Screen.Settings.route) {
                                Text("Settings")
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
    object Subscriptions :
        Screen("/subscriptions", "Subscriptions", R.drawable.ic_baseline_view_list_24)

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