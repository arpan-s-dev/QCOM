package com.medic.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HospitalFinderTest {

    @Test
    fun nearest_ranks_by_distance_from_downtown_sf() {
        val hospitals = listOf(
            Hospital("Zuckerberg SF General", 37.7554, -122.4045),
            Hospital("UCSF Parnassus", 37.7631, -122.4586),
            Hospital("Far Away", 37.9000, -122.5000)
        )
        val nearest = HospitalFinder.nearest(hospitals, 37.7749, -122.4194, count = 2)
        assertEquals(2, nearest.size)
        assertTrue(nearest[0].distanceKm < nearest[1].distanceKm)
        assertEquals("Zuckerberg SF General", nearest[0].hospital.name)
    }

    @Test
    fun bearing_is_cardinal_east_for_point_east() {
        val bearing = GeoMath.bearingDegrees(37.7749, -122.4194, 37.7749, -122.4094)
        assertTrue(bearing in 85.0..95.0)
    }
}
