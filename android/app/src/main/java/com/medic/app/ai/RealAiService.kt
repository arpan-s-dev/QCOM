package com.medic.app.ai

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class OnDeviceCapability {
    EMBED,
    GENERATE,
    TRANSCRIBE,
    TRANSLATE
}

data class OnDeviceBackendStatus(
    val backendName: String,
    val loadedCapabilities: Set<OnDeviceCapability>
) {
    val isReady: Boolean
        get() = REQUIRED_CAPABILITIES.all(loadedCapabilities::contains)

    fun supports(capability: OnDeviceCapability): Boolean = loadedCapabilities.contains(capability)

    companion object {
        val REQUIRED_CAPABILITIES: Set<OnDeviceCapability> = setOf(
            OnDeviceCapability.EMBED,
            OnDeviceCapability.GENERATE,
            OnDeviceCapability.TRANSCRIBE
        )
    }
}

/**
 * Android-side bridge that exposes the app's canonical [AiService] contract
 * while the actual model runners live behind a swappable backend (JNI,
 * ExecuTorch wrapper, Qualcomm AI Hub wrapper, etc.).
 *
 * This keeps UI/RAG code stable: once Person 1 lands a backend that talks to
 * the real on-device models, the app only swaps construction, not call sites.
 */
interface OnDeviceModelBackend {
    val status: OnDeviceBackendStatus

    suspend fun embed(text: String): FloatArray
    suspend fun generate(prompt: String): String
    suspend fun transcribe(audioPcm16: ShortArray): String
    suspend fun translate(text: String, fromLang: String, toLang: String): String
}

class AiCapabilityUnavailableException(message: String) : IllegalStateException(message)

class RealAiService(
    private val backend: OnDeviceModelBackend,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AiService {

    override val isReady: Boolean
        get() = backend.status.isReady

    override suspend fun embed(text: String): FloatArray = withContext(dispatcher) {
        require(text.isNotBlank()) { "embed() requires non-blank text" }
        ensureCapability(OnDeviceCapability.EMBED)
        backend.embed(text).also { embedding ->
            require(embedding.isNotEmpty()) { "embed() returned an empty embedding" }
        }
    }

    override suspend fun generate(prompt: String): String = withContext(dispatcher) {
        require(prompt.isNotBlank()) { "generate() requires a non-blank prompt" }
        ensureCapability(OnDeviceCapability.GENERATE)
        backend.generate(prompt).trim().also { response ->
            require(response.isNotEmpty()) { "generate() returned a blank response" }
        }
    }

    override suspend fun transcribe(audioPcm16: ShortArray): String = withContext(dispatcher) {
        require(audioPcm16.isNotEmpty()) { "transcribe() requires PCM16 audio samples" }
        ensureCapability(OnDeviceCapability.TRANSCRIBE)
        backend.transcribe(audioPcm16).trim().also { transcript ->
            require(transcript.isNotEmpty()) { "transcribe() returned a blank transcript" }
        }
    }

    override suspend fun translate(text: String, fromLang: String, toLang: String): String = withContext(dispatcher) {
        require(text.isNotBlank()) { "translate() requires non-blank text" }
        require(fromLang.isNotBlank()) { "translate() requires a source language" }
        require(toLang.isNotBlank()) { "translate() requires a target language" }

        if (fromLang.equals(toLang, ignoreCase = true)) {
            return@withContext text
        }

        ensureCapability(OnDeviceCapability.TRANSLATE)
        backend.translate(text, fromLang, toLang).trim().also { translation ->
            require(translation.isNotEmpty()) { "translate() returned a blank translation" }
        }
    }

    private fun ensureCapability(capability: OnDeviceCapability) {
        if (!backend.status.supports(capability)) {
            throw AiCapabilityUnavailableException(
                "${backend.status.backendName} does not have $capability loaded"
            )
        }
    }
}

/**
 * Safe default bridge while model export / JNI plumbing is still landing.
 * The app can construct this explicitly during integration to fail fast with
 * a clear message instead of quietly falling back to cloud or fake behavior.
 */
class UninitializedOnDeviceModelBackend(
    override val status: OnDeviceBackendStatus = OnDeviceBackendStatus(
        backendName = "uninitialized-backend",
        loadedCapabilities = emptySet()
    )
) : OnDeviceModelBackend {

    override suspend fun embed(text: String): FloatArray = unavailable(OnDeviceCapability.EMBED)

    override suspend fun generate(prompt: String): String = unavailable(OnDeviceCapability.GENERATE)

    override suspend fun transcribe(audioPcm16: ShortArray): String = unavailable(OnDeviceCapability.TRANSCRIBE)

    override suspend fun translate(text: String, fromLang: String, toLang: String): String =
        unavailable(OnDeviceCapability.TRANSLATE)

    private fun <T> unavailable(capability: OnDeviceCapability): T {
        throw AiCapabilityUnavailableException(
            "${status.backendName} cannot serve $capability until the on-device model runner is wired"
        )
    }
}
