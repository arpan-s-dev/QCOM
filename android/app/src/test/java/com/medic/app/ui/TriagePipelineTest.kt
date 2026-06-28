package com.medic.app.ui

import com.medic.app.ai.AiService
import com.medic.app.ai.TriageOrchestrator
import com.medic.app.data.CorpusChunk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriagePipelineTest {

    @Test
    fun `bleeding hasnt stopped returns critical severity and grounded citation`() = runBlocking {
        val ai = DeterministicTestAiService()
        val corpus = listOf(
            CorpusChunk(
                id = "FA-0003",
                category = "MASSIVE_HEMORRHAGE",
                subtopic = "tourniquet_application",
                severity = "CRITICAL",
                text = "Apply a tourniquet above severe limb bleeding when it has not stopped.",
                source = "test-source",
                embedding = ai.embed("bleeding chunk")
            )
        )

        val result = TriageOrchestrator(ai, corpus).handleQuery("The bleeding hasn't stopped.")

        assertEquals("CRITICAL", result.triage.severity.name)
        assertTrue(result.llmAnswer.contains("[FA-0003]"))
        assertEquals(listOf("FA-0003"), result.citedChunkIds)
    }

    private class DeterministicTestAiService : AiService {
        override val isReady: Boolean = true

        override suspend fun embed(text: String): FloatArray =
            if (text.contains("bleeding", ignoreCase = true) || text.contains("tourniquet", ignoreCase = true)) {
                floatArrayOf(1f, 0f)
            } else {
                floatArrayOf(0f, 1f)
            }

        override suspend fun generate(prompt: String): String = when {
            prompt.contains("[FA-0003]") ->
                "Use direct pressure and a tourniquet if the bleeding has not stopped. [FA-0003]\n" +
                    "This is offline first-aid guidance, not a substitute for professional medical care -- seek evacuation when possible."
            else ->
                "No grounded passage found."
        }

        override suspend fun transcribe(audioPcm16: ShortArray): String = ""

        override suspend fun translate(text: String, fromLang: String, toLang: String): String = text
    }
}
