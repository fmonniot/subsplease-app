package eu.monniot.subpleaseapp.clients


import com.mooveit.library.Fakeit
import java.time.DayOfWeek
import kotlin.random.Random


data class Subscription(
    val name: String,
    val slug: String,
    val releasedOn: DayOfWeek?,
    val subscribed: Boolean
) {

    companion object {
        private val rand = Random(0)

        fun random(): Subscription {
            val name = Fakeit.name().firstName()
            val slug = Fakeit.address().streetAddress()
            val release = DayOfWeek.of(rand.nextInt(1, 8))
            val sub = Random(42).nextBoolean()

            return Subscription(name, slug, release, sub)
        }
    }
}
