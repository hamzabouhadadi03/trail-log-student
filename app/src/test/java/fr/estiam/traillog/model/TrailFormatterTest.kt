package fr.estiam.traillog.model

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrailFormatterTest {
    private lateinit var previous: Locale

    @Before fun setUp() { previous = Locale.getDefault(); Locale.setDefault(Locale.US) }
    @After fun tearDown() { Locale.setDefault(previous) }

    @Test fun tenKmIsAboutSixPointTwoMiles() {
        assertEquals("6.2 mi", TrailFormatter.distance(10.0, DistanceUnit.MI))
    }

    @Test fun kmDisplayIsUnchanged() {
        assertEquals("10.0 km", TrailFormatter.distance(10.0, DistanceUnit.KM))
        assertEquals("8.0 km", TrailFormatter.distance(8.0, DistanceUnit.KM))
    }

    @Test fun zeroKmIsZeroMiles() {
        assertEquals("0.0 mi", TrailFormatter.distance(0.0, DistanceUnit.MI))
    }
}
