package com.medic.app.nav.star

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GeometryStarSolverTest {

    private val solver = GeometryStarSolver()

    @Test
    fun `fails when fewer than four stars detected`() {
        val context = StarSolveContext(
            detection = StarDetectionResult(
                stars = listOf(
                    DetectedStar(1f, 1f, 1f),
                    DetectedStar(2f, 2f, 0.8f),
                    DetectedStar(3f, 3f, 0.6f)
                ),
                imageWidth = 100,
                imageHeight = 100,
                detectorName = "test"
            ),
            imageFileName = null,
            contentHashHex = "",
            devicePitchDeg = 45.0,
            deviceAzimuthDeg = 10.0,
            utc = Instant.parse("2026-06-28T04:00:00Z")
        )
        val catalog = StarCatalog("test", emptyList())
        val result = solver.solve(context, catalog)
        assertFalse(result.success)
    }

    @Test
    fun `normalize heading wraps to 0-360`() {
        assertEquals(10.0, normalizeHeading(370.0), 0.001)
        assertEquals(350.0, normalizeHeading(-10.0), 0.001)
    }

    @Test
    fun `deriveHeadingFromField returns bounded heading`() {
        val anchor = CatalogStar("HR 7883", "Polaris", 2.5303, 89.2641, 1.98)
        val heading = solver.deriveHeadingFromField(
            anchor = anchor,
            utc = Instant.parse("2026-06-28T04:00:00Z"),
            deviceAzimuthDeg = 90.0,
            devicePitchDeg = 30.0
        )
        assertNotNull(heading)
        assertTrue(heading in 0.0..360.0)
    }
}
