package eu.monniot.subpleaseapp.scheduling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.Html
import eu.monniot.subpleaseapp.R
import eu.monniot.subpleaseapp.clients.subsplease.SubsPleaseApi
import eu.monniot.subpleaseapp.data.AppDatabase
import eu.monniot.subpleaseapp.data.ShowsStore
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*

/**
 * BroadcastReceiver called by the alarm we set. It is in charge of scheduling
 * show downloads based on what the user is subscribed to.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        requireNotNull(context)

        // 1. Instantiate dependencies (prefs, db, http clients, coroutine scope, current day)
        val http = OkHttpClient.Builder()
            .addInterceptor(run {
                val h = HttpLoggingInterceptor()
                h.level = HttpLoggingInterceptor.Level.BODY
                h
            })
            .build()
        val api = SubsPleaseApi.build(http)
        val db = AppDatabase.build(context)

        val showsStore = ShowsStore(db.showDao(), api, http)
        val scheduling = AlertScheduling.build(showsStore, context)

        val today = ZonedDateTime.now()


        // TODO Verify that blocking a BroadcastReceiver thread is ok
        runBlocking {

            // TODO Pass dependencies to the function
            downloadSubscribedShows(context, showsStore, today)

            // Schedule the next alert
            // TODO Decide if this should be inlined within downloadSubscribedShows()
            scheduling.schedule()
        }
    }

    companion object {
        suspend fun downloadSubscribedShows(
            context: Context,
            showsStore: ShowsStore,
            today: ZonedDateTime
        ) {

            val shows = showsStore.subscriptions()[today.dayOfWeek.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            )] ?: emptyList()

            createNotification(context, shows.map { it.title })

            // TODO Real work
            // 1. Get subscribed show for the day
            // 2. Grab download list for each
            // 3. Filter each list by the current day
            // 4. Add dl through magnet links
            // 5. Future work: if enough space left on server, start items
        }

        // TODO See how we can test this. Maybe by extracting the NotificationManager part ?
        private fun createNotification(context: Context, showNames: List<String>) {
            // Create the notification channel (required to create notifications)
            // Create the NotificationChannel
            val channelID = "DlId"
            val mChannel = NotificationChannel(
                channelID,
                "Download scheduled",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = "Notification is shown when a show is added to the download list"
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)

            val nbShows = showNames.size

            // Create the notification
            val nBuilder = Notification.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Daily show${if (nbShows > 1) "s" else ""} scheduled")
                .setContentText("$nbShows shows")
                .setStyle(
                    Notification.BigTextStyle()
                        .setBigContentTitle("Daily show${if (nbShows > 1) "s" else ""} scheduled")
                        .bigText(
                            Html.fromHtml(
                                showNames.joinToString(
                                    "</li><li>",
                                    "<ul><li>",
                                    "</li></ul>"
                                ), Html.FROM_HTML_MODE_COMPACT
                            )
                        )
                )
                .setAutoCancel(true)

            // Generate an Id for each notification
            val id = System.currentTimeMillis() / 1000

            // Notify the user
            notificationManager.notify(id.toInt(), nBuilder.build())
        }
    }
}

// Too bad, I rebooted my phone yesterday and thus didn't test this…

/**
 * This receiver gets called everytime a user reboot its device. We need
 * to plan our alarms again when it happens.
 */
class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            TODO("Not yet implemented")
        }
    }
}

// Too bad, It was daylight change yesterday and thus didn't test this…

/**
 * This receiver gets called everytime a user change the system time, which
 * cancel all alarms set. We need to reschedule them with the new system time.
 */
class TimeChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_TIME_CHANGED) {
            TODO("Not yet implemented")
        }
    }
}
