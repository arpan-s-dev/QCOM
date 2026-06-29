package com.medic.app.ai

import android.content.Context
import android.util.Log
import java.io.File

/**
 * On-device paths for pre-exported Qwen3 hybrid PTE bundles (SM8750 / QNN).
 * Models are pushed via [android/push_qwen_models.ps1] — too large for APK assets.
 */
object QwenModelPaths {
    private const val TAG = "QwenModelPaths"

    const val PTE_NAME = "hybrid_llama_qnn.pte"
    const val TOKENIZER_NAME = "tokenizer.json"
    const val CHAT_TEMPLATE_NAME = "chat_template.jinja"

    /** Bundles we accept, in preference order (0.6B export matches our ExecuTorch v1.0.0 toolchain). */
    private val MODEL_BUNDLES = listOf(
        ModelBundle("qwen3-0_6b", minPteBytes = 400_000_000L, maxSeqLen = 1024),
        ModelBundle("qwen3-1_7b", minPteBytes = 1_000_000_000L, maxSeqLen = 4096),
    )

    internal data class ModelBundle(
        val subdir: String,
        val minPteBytes: Long,
        val maxSeqLen: Int,
    )

    @Volatile
    private var resolved: Pair<File, ModelBundle>? = null

    /** App-scoped external dir (legacy adb push target). */
    fun stagingDir(context: Context, subdir: String): File =
        File("/storage/emulated/0/Android/data/${context.packageName}/files/models/$subdir")

    fun preferredDir(context: Context, subdir: String): File {
        val base = context.getExternalFilesDir("models")
            ?: context.getExternalFilesDir(null)?.let { File(it, "models") }
            ?: File(context.filesDir, "models")
        return File(base, subdir).apply { mkdirs() }
    }

    private fun candidates(context: Context, subdir: String): List<File> = listOf(
        File(context.filesDir, "models/$subdir"),
        preferredDir(context, subdir),
        stagingDir(context, subdir),
    )

    /** First valid bundle directory and its metadata. */
    internal fun resolve(context: Context): Pair<File, ModelBundle> {
        resolved?.let { return it }
        synchronized(this) {
            resolved?.let { return it }
            for (bundle in MODEL_BUNDLES) {
                for (dir in candidates(context, bundle.subdir)) {
                    if (bundleValid(dir, bundle)) {
                        Log.i(TAG, "Using ${bundle.subdir} at ${dir.absolutePath} (maxSeq=${bundle.maxSeqLen})")
                        return Pair(dir, bundle).also { resolved = it }
                    }
                }
            }
            val fallback = preferredDir(context, MODEL_BUNDLES.last().subdir)
            Log.w(TAG, "No valid Qwen bundle — run android/push_qwen_models.ps1")
            return Pair(fallback, MODEL_BUNDLES.last())
        }
    }

    fun resolveModelDir(context: Context): File = resolve(context).first

    fun maxSeqLen(context: Context): Int = resolve(context).second.maxSeqLen

    fun pteFile(context: Context): File = File(resolveModelDir(context), PTE_NAME)

    fun tokenizerFile(context: Context): File = File(resolveModelDir(context), TOKENIZER_NAME)

    fun isReady(context: Context): Boolean {
        for (bundle in MODEL_BUNDLES) {
            for (dir in candidates(context, bundle.subdir)) {
                if (bundleValid(dir, bundle)) return true
            }
        }
        return false
    }

    /** Clear cached resolution after pushing a new model without restarting the app. */
    fun invalidateCache() {
        synchronized(this) { resolved = null }
    }

    private fun bundleValid(dir: File, bundle: ModelBundle): Boolean {
        val pte = File(dir, PTE_NAME)
        val tok = File(dir, TOKENIZER_NAME)
        return pte.isFile && pte.length() > bundle.minPteBytes &&
            tok.isFile && tok.length() > 1_000L
    }
}
