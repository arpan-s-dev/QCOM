package com.medic.app.ai

import android.app.Application
import android.util.Log
import com.medic.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Chat pipeline entry point:
 * 1. [create] → [StubAiService] (safe at launch; native QNN load can SIGSEGV before Kotlin catches it)
 * 2. [serviceForQuery] → tries one NPU warm-up; on success swaps to [RealAiService], else keeps stub
 */
object AiServiceFactory {
    private const val TAG = "AiServiceFactory"

    @Volatile
    private var qwenBackend: ExecutorchQwenBackend? = null

    @Volatile
    private var npuReady = false

    @Volatile
    private var loadPermanentlyFailed = false

    private fun backend(application: Application): ExecutorchQwenBackend =
        qwenBackend ?: synchronized(this) {
            qwenBackend ?: ExecutorchQwenBackend(application.applicationContext).also { qwenBackend = it }
        }

    fun preload(application: Application, scope: CoroutineScope) {
        if (!BuildConfig.ENABLE_QNN_BACKEND || !BuildConfig.AUTO_LOAD_QWEN) return
        if (!QwenModelPaths.isReady(application)) return
        scope.launch {
            val ok = warmUp(application)
            Log.i(TAG, if (ok) "Qwen NPU preloaded" else "Qwen NPU preload failed — staying on stub")
        }
    }

    /** Safe default at app launch — never triggers native model load. */
    fun create(application: Application, @Suppress("UNUSED_PARAMETER") scope: CoroutineScope): AiService {
        if (!BuildConfig.ENABLE_QNN_BACKEND) {
            Log.i(TAG, "Using StubAiService — NPU backend disabled in build")
        } else if (QwenModelPaths.isReady(application)) {
            Log.i(TAG, "PTE on device — NPU loads on first chat (stub until warm-up succeeds)")
        } else {
            Log.i(TAG, "Using StubAiService — run android/push_qwen_models.ps1 to install PTE")
        }
        return StubAiService()
    }

    /**
     * Called once per user message. Returns RealAiService only after a successful warm-up;
     * otherwise StubAiService so SafetyTree + UI keep working without crashing.
     */
    suspend fun serviceForQuery(application: Application): AiService {
        if (!BuildConfig.ENABLE_QNN_BACKEND || !QwenModelPaths.isReady(application)) {
            return StubAiService()
        }
        if (npuReady) {
            return RealAiService(backend(application))
        }
        if (loadPermanentlyFailed) {
            return StubAiService()
        }
        return if (warmUp(application)) {
            RealAiService(backend(application))
        } else {
            StubAiService()
        }
    }

    suspend fun warmUp(application: Application): Boolean {
        if (!BuildConfig.ENABLE_QNN_BACKEND || !QwenModelPaths.isReady(application)) return false
        if (npuReady) return true
        if (loadPermanentlyFailed) return false
        val ok = runCatching { backend(application).tryLoad() }
            .onFailure { e ->
                Log.e(TAG, "Qwen NPU warm-up failed — using stub for chat", e)
                loadPermanentlyFailed = true
            }
            .getOrDefault(false)
        if (ok) {
            npuReady = true
            Log.i(TAG, "Qwen NPU ready — RealAiService active")
        } else {
            loadPermanentlyFailed = true
        }
        return ok
    }
}
