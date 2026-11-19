package fr.estiam.traillog.model

import org.junit.Assert.*
import org.junit.Test

class TrailTest {
    @Test fun acceptsValidTrail() {
        assertTrue(Trail("t1", "A", "Alpes", 3.5, 120).isValid())
    }
    @Test fun rejectsInvalidTrail() {
        assertFalse(Trail("t1", "  ", "Alpes", 3.5, 120).isValid())
        assertFalse(Trail("t1", "A", "Alpes", -1.0, 120).isValid())
        assertFalse(Trail("t1", "A", "Alpes", 3.5, -1).isValid())
    }
    @Test fun acceptsHighElevationGain() {
        // Lab 1: "Mountain Test", 8.0 km, 750 m must be valid (no upper bound in the model).
        assertTrue(Trail("t2", "Mountain Test", "", 8.0, 750).isValid())
        assertTrue(Trail("t3", "Mountain Test", "", 8.0, 250).isValid())
    }
}
