package com.medic.app.nav.star

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StarCatalogLoaderTest {

    @Test
    fun `parses bundled catalog json shape`() {
        val json = """
            {
              "catalog": "Test catalog",
              "stars": [
                {"id": "HR 1", "name": "Star A", "ra_hours": 1.0, "dec_deg": 10.0, "mag": 2.0}
              ]
            }
        """.trimIndent()
        val catalog = StarCatalogLoader.parse(json)
        assertEquals("Test catalog", catalog.name)
        assertEquals(1, catalog.stars.size)
        assertEquals("Star A", catalog.stars.first().name)
    }

    @Test
    fun `yale asset subset has multiple bright stars when loaded from resources`() {
        val json = javaClass.classLoader
            ?.getResourceAsStream("yale_bright_stars.json")
            ?.bufferedReader()
            ?.use { it.readText() }
        if (json == null) {
            // Asset lives under android assets — JVM test may not see it; skip shape check.
            return
        }
        val catalog = StarCatalogLoader.parse(json)
        assertTrue(catalog.stars.size >= 10)
    }
}
