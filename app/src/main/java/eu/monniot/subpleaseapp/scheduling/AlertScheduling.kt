package eu.monniot.subpleaseapp.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.monniot.subpleaseapp.data.ShowsStore
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*

/**
 * Needs an abstraction to install the Alarm (to easily mock it in tests).
 * Can be either a simple function or an interface (or a functional interface).
 *
 * In a first iteration, we can have a simple daily alarm at the latest time a show
 * is released. In the future, and if needed, we can have weekly alarms for each day
 * we have a subscribed to (so as to not trigger an alarm on a day we know nothing will
 * be acted on). Not entirely certain if it's worth the effort. As a data point, in 21Q1
 * We have two days off.
 *
 * After reading the AOSP clock application, it might make sense to instead schedule the
 * next alarm only. In that alarm, after we have scheduled the day dl, we can choose when
 * to trigger the next alarm/alert. This also let us deal with things like late show
 * availability. It does mean we will need a dedicated table to manage what show we have
 * dl and which one we haven't.
 */
interface AlertScheduling {

    /**
     * Called at system startup, on time/timezone change, and whenever
     * the user changes subscriptions. Loads all subscriptions and activate
     * the next alert.
     */
    suspend fun schedule()


    companion object {
        private val TAG = AlertScheduling::class.java.simpleName

        fun build(store: ShowsStore, context: Context): AlertScheduling {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            return object : AlertScheduling {
                override suspend fun schedule() {
                    val subs = store.dailyLatestSubscribedShow()

                    // If no subscriptions set, we have nothing to schedule
                    if (subs.isEmpty()) {
                        return
                    }

                    val today = ZonedDateTime.now()

                    val triggerAt = findNextAlarmTime(today, subs)
                    Log.d(TAG, "Next alert set for ${triggerAt.atZone(ZoneId.systemDefault())}")

                    val intent = Intent(context, AlarmReceiver::class.java)
                    val pending = PendingIntent.getBroadcast(
                        context, 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Given that the Intent and PendingIntent is always the same, it will cancel
                    // previous intent and set this one as reference
                    // TODO Verify AlarmManager behaviour
                    alarmManager.set(AlarmManager.RTC, triggerAt.toEpochMilli(), pending)
                }
            }
        }

        // TODO Change the subs parameter to use DayOfWeek as key
        // That will let us sort the Map correctly. For now we assume it is ordered
        // in the natural order for days (eg. Mon, Tue, Wed, Thu, Fri, Sat, Sun)
        fun findNextAlarmTime(now: ZonedDateTime, subs: Map<String, String>): Instant {
            require(subs.isNotEmpty()) { "Subscriptions list cannot be empty when finding next alarm time" }

            // Let's find the next day with some subscriptions.
            var time: String? = null // time is "04:30" format, in user locale
            var lookingAtDay = now.dayOfWeek
            var dayCounter = 0
            while (time == null) {
                dayCounter += 1   // Count the number of days we look into the future
                lookingAtDay += 1 // This will wrap from Sunday to Monday automatically
                time = subs[lookingAtDay.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()
                )]
            }

            val t = java.time.LocalTime.parse(time)

            // TODO Should we introduce a bit of latency here ?
            // It really depends if the shows are available at the minute or later on. Needs data.
            return now
                .plusDays(dayCounter.toLong())
                .withHour(t.hour)
                .withMinute(t.minute)
                .withSecond(0)
                .toInstant()
        }
    }
}
