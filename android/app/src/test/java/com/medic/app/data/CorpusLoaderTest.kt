package com.medic.app.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class CorpusLoaderTest {

    @Test
    fun `parseFirstAidCorpus preserves fields and assigns embeddings`() = runBlocking {
        val json = """
            [
              {
                "id": "FA-9001",
                "category": "MASSIVE_HEMORRHAGE",
                "subtopic": "tourniquet_application",
                "severity": "CRITICAL",
                "text": "Apply firm pressure to severe bleeding.",
                "source": "test-source",
                "relatedTo": "FA-0003"
              }
            ]
        """.trimIndent()

        val chunks = CorpusLoader.parseFirstAidCorpus(json) {
            floatArrayOf(it.length.toFloat(), 1f)
        }

        assertEquals(1, chunks.size)
        assertEquals("FA-9001", chunks.single().id)
        assertEquals("FA-0003", chunks.single().relatedTo)
        assertArrayEquals(floatArrayOf(39f, 1f), chunks.single().embedding, 0f)
    }
}
