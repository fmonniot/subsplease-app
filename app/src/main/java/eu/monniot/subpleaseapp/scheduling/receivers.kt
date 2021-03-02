package eu.monniot.subpleaseapp.scheduling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.monniot.subpleaseapp.R

/**
 * BroadcastReceiver called by the alarm we set. It is in charge of scheduling
 * show downloads based on what the user is subscribed to.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

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
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        // Create the notification
        val nBuilder = Notification.Builder(context, channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("DL")
            .setContentText("Shows will go here")
            .setAutoCancel(true)

        // Generate an Id for each notification
        val id = System.currentTimeMillis() / 1000

        // Notify the user
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id.toInt(), nBuilder.build())

        // 1. Instantiate dependencies (prefs, db, http clients, coroutine scope, current day)
        // 2. Call downloadSubscribedShows
    }

    companion object {
        suspend fun downloadSubscribedShows() {
            // 1. Get subscribed show for the day
            // 2. Grab download list for each
            // 3. Filter each list by the current day
            // 4. Add dl through magnet links
            // 5. Future work: if enough space left on server, start items
        }
    }
}

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
