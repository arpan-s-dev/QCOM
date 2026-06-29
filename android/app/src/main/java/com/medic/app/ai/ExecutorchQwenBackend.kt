package com.medic.app.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.pytorch.executorch.extension.llm.LlmCallback
import org.pytorch.executorch.extension.llm.LlmModule

/**
 * ExecuTorch + Qualcomm QNN backend for the pre-built Qwen3 1.7B hybrid .pte.
 * Matches LlamaDemo QNN_TEXT_MODEL category (static/hybrid llama-qnn pte).
 */
class ExecutorchQwenBackend(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OnDeviceModelBackend {

    private val loadMutex = Mutex()
    private var module: LlmModule? = null
    private var loaded = false

    override val status: OnDeviceBackendStatus
        get() = OnDeviceBackendStatus(
            backendName = if (loaded) "executorch-qwen3-1.7b-qnn" else "executorch-qwen3 (not loaded)",
            // Advertise GENERATE when PTE is on disk so RealAiService reaches tryLoad().
            loadedCapabilities = buildSet {
                if (loaded || QwenModelPaths.isReady(context)) {
                    add(OnDeviceCapability.GENERATE)
                }
            }
        )

    /** Load PTE from app storage. Returns false if files missing or native load fails. */
    suspend fun tryLoad(): Boolean = loadMutex.withLock {
        if (loaded) return true
        if (!QwenModelPaths.isReady(context)) {
            Log.w(TAG, "Qwen PTE not on device. Run android/push_qwen_models.ps1 with phone connected.")
            return false
        }
        withContext(dispatcher) {
            try {
                val pte = QwenModelPaths.pteFile(context).absolutePath
                val tok = QwenModelPaths.tokenizerFile(context).absolutePath
                Log.i(TAG, "Loading Qwen PTE from $pte with QNN config: $QNN_CONFIG")
                val llm = LlmModule(QNN_TEXT_MODEL, pte, tok, TEMPERATURE, QNN_CONFIG)
                val loadResult = llm.load()
                if (loadResult != 0) {
                    Log.e(TAG, "Qwen load() failed with error code $loadResult")
                    return@withContext false
                }
                module = llm
                loaded = true
                Log.i(TAG, "Qwen NPU model loaded successfully")
                true
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "QNN native library load failed — check qnn-runtime version in gradle.properties", e)
                module = null
                loaded = false
                false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load Qwen PTE", e)
                module = null
                loaded = false
                false
            }
        }
    }

    override suspend fun embed(text: String): FloatArray {
        // BGE not wired yet — deterministic stub so triage/RAG plumbing works offline.
        val dim = 384
        val rng = java.util.Random(text.hashCode().toLong())
        return FloatArray(dim) { rng.nextFloat() * 2f - 1f }
    }

    override suspend fun generate(prompt: String): String = withContext(dispatcher) {
        if (!loaded && !tryLoad()) {
            throw AiCapabilityUnavailableException("Qwen NPU model failed to load")
        }
        ensureLoaded()
        val llm = module ?: error("Qwen module not loaded")
        val out = StringBuilder()
        val callback = object : LlmCallback {
            override fun onResult(result: String) {
                out.append(result)
            }

            override fun onStats(stats: String) {
                Log.d(TAG, "Qwen stats: $stats")
            }
        }
        llm.generate(prompt, QwenModelPaths.MAX_SEQ_LEN, callback, false)
        out.toString().trim()
    }

    override suspend fun transcribe(audioPcm16: ShortArray): String =
        unavailable(OnDeviceCapability.TRANSCRIBE)

    override suspend fun translate(text: String, fromLang: String, toLang: String): String =
        unavailable(OnDeviceCapability.TRANSLATE)

    private fun ensureLoaded() {
        if (!loaded) {
            throw AiCapabilityUnavailableException(
                "Qwen NPU model not loaded — push hybrid_llama_qnn.pte to the app models folder"
            )
        }
    }

    private fun <T> unavailable(capability: OnDeviceCapability): T {
        throw AiCapabilityUnavailableException(
            "executorch-qwen backend does not implement $capability yet"
        )
    }

    companion object {
        private const val TAG = "ExecutorchQwenBackend"
        /** Same as LlamaDemo ModelUtils.QNN_TEXT_MODEL for Qualcomm hybrid/kv pte. */
        private const val QNN_TEXT_MODEL = 4
        private const val TEMPERATURE = 0.3f
        /** Required for Qwen3 hybrid PTE on QNN — see LlamaDemo buildQnnConfigString(). */
        private const val QNN_CONFIG =
            "decoder_model_version:qwen3;kv_updater:SmartMask;eval_mode:1"
    }
}
