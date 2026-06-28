package com.medic.app.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealAiServiceTest {

    @Test
    fun `isReady requires embed generate and transcribe`() {
        val service = RealAiService(
            backend = FakeBackend(
                status = OnDeviceBackendStatus(
                    backendName = "partial",
                    loadedCapabilities = setOf(OnDeviceCapability.EMBED, OnDeviceCapability.GENERATE)
                )
            )
        )

        assertFalse(service.isReady)
    }

    @Test
    fun `isReady is true when required capabilities are loaded`() {
        val service = RealAiService(
            backend = FakeBackend(
                status = OnDeviceBackendStatus(
                    backendName = "full",
                    loadedCapabilities = setOf(
                        OnDeviceCapability.EMBED,
                        OnDeviceCapability.GENERATE,
                        OnDeviceCapability.TRANSCRIBE
                    )
                )
            )
        )

        assertTrue(service.isReady)
    }

    @Test
    fun `generate delegates to backend and trims response`() {
        runBlocking {
            val service = RealAiService(
                backend = FakeBackend(
                    status = readyStatus(),
                    generateResult = "  grounded answer  "
                )
            )

            val result = service.generate("prompt")

            assertEquals("grounded answer", result)
        }
    }

    @Test
    fun `translate short circuits when languages already match`() {
        runBlocking {
            val backend = FakeBackend(status = readyStatus())
            val service = RealAiService(backend = backend)

            val result = service.translate("hola", "Spanish", "spanish")

            assertEquals("hola", result)
            assertFalse(backend.translateCalled)
        }
    }

    @Test(expected = AiCapabilityUnavailableException::class)
    fun `translate fails fast when translation model is unavailable`() {
        runBlocking {
            val service = RealAiService(
                backend = FakeBackend(status = readyStatus())
            )

            service.translate("hello", "English", "Spanish")
        }
    }

    @Test
    fun `embed delegates to backend`() {
        runBlocking {
            val expected = floatArrayOf(0.1f, 0.2f, 0.3f)
            val service = RealAiService(
                backend = FakeBackend(
                    status = readyStatus(),
                    embedResult = expected
                )
            )

            val result = service.embed("tourniquet")

            assertArrayEquals(expected, result, 0f)
        }
    }

    private fun readyStatus(): OnDeviceBackendStatus = OnDeviceBackendStatus(
        backendName = "ready",
        loadedCapabilities = setOf(
            OnDeviceCapability.EMBED,
            OnDeviceCapability.GENERATE,
            OnDeviceCapability.TRANSCRIBE
        )
    )

    private class FakeBackend(
        override val status: OnDeviceBackendStatus,
        private val embedResult: FloatArray = floatArrayOf(1f),
        private val generateResult: String = "response",
        private val transcribeResult: String = "transcript",
        private val translateResult: String = "translation"
    ) : OnDeviceModelBackend {
        var translateCalled: Boolean = false

        override suspend fun embed(text: String): FloatArray = embedResult

        override suspend fun generate(prompt: String): String = generateResult

        override suspend fun transcribe(audioPcm16: ShortArray): String = transcribeResult

        override suspend fun translate(text: String, fromLang: String, toLang: String): String {
            translateCalled = true
            return translateResult
        }
    }
}
