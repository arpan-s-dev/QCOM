package com.medic.app.ai

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Picks RealAiService when the Qwen PTE is on device, otherwise StubAiService.
 */
object AiServiceFactory {
    private const val TAG = "AiServiceFactory"

    fun create(application: Application, scope: CoroutineScope): AiService {
        val backend = ExecutorchQwenBackend(application)
        if (!QwenModelPaths.isReady(application)) {
            Log.i(TAG, "Using StubAiService — Qwen PTE not found on device")
            return StubAiService()
        }
        // Return RealAiService immediately; load happens async so first query may wait.
        val service = RealAiService(backend)
        scope.launch(Dispatchers.IO) {
            if (backend.tryLoad()) {
                Log.i(TAG, "Using RealAiService with Qwen NPU backend")
            } else {
                Log.w(TAG, "Qwen load failed — generate() will throw until PTE is fixed")
            }
        }
        return service
    }
}
