package eu.monniot.subpleaseapp.scheduling

import org.junit.Assert
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AlertSchedulingTest {

    // This is a Wednesday
    private val time = ZonedDateTime.of(2021, 3, 3, 21, 41, 1, 0, ZoneId.of("UTC"))

    @Test
    fun findNextAlarmTime_nextDay() {
        val subs = mapOf(Pair("Thursday", "04:30"))

        val expected = time.plusDays(1)
            .withHour(4)
            .withMinute(30)
            .withSecond(0)
            .toInstant()

        Assert.assertEquals(expected, AlertScheduling.findNextAlarmTime(time, subs))
    }

    @Test
    fun findNextAlarmTime_sameDay() {
        val subs = mapOf(Pair("Wednesday", "11:30"))

        // time is a wednesday, so we expect the scheduling to be for the next week
        val expected = time.plusDays(7)
            .withHour(11)
            .withMinute(30)
            .withSecond(0)
            .toInstant()

        Assert.assertEquals(expected, AlertScheduling.findNextAlarmTime(time, subs))
    }

    @Test
    fun findNextAlarmTime_multipleSubs() {
        val subs = mapOf(Pair("Sunday", "12:30"), Pair("Monday", "09:45"))

        val expected = time.plusDays(4)
            .withHour(12)
            .withMinute(30)
            .withSecond(0)
            .toInstant()

        Assert.assertEquals(expected, AlertScheduling.findNextAlarmTime(time, subs))
    }
}
