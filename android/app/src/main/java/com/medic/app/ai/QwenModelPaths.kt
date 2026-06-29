package com.medic.app.ai

import android.content.Context
import android.util.Log
import java.io.File

/**
 * On-device paths for the pre-exported Qwen3 1.7B hybrid PTE (SM8750).
 * The 1.7GB .pte is too large for APK assets — push once via [android/push_qwen_models.ps1].
 */
object QwenModelPaths {
    private const val TAG = "QwenModelPaths"

    const val PTE_NAME = "hybrid_llama_qnn.pte"
    const val TOKENIZER_NAME = "tokenizer.json"
    const val CHAT_TEMPLATE_NAME = "chat_template.jinja"
    const val MAX_SEQ_LEN = 4096

    private const val MODEL_SUBDIR = "qwen3-1_7b"
    private const val MIN_PTE_BYTES = 1_000_000_000L
    private const val MIN_TOKENIZER_BYTES = 1_000L

    /** App-scoped external dir (adb push target). */
    fun stagingDir(context: Context): File =
        File("/storage/emulated/0/Android/data/${context.packageName}/files/models/$MODEL_SUBDIR")

    /** Preferred writable dir under app external storage, with internal fallback. */
    fun preferredDir(context: Context): File {
        val base = context.getExternalFilesDir("models")
            ?: context.getExternalFilesDir(null)?.let { File(it, "models") }
            ?: File(context.filesDir, "models")
        return File(base, MODEL_SUBDIR).apply { mkdirs() }
    }

    /** First directory that actually contains a valid PTE + tokenizer bundle. */
    fun resolveModelDir(context: Context): File {
        // Internal app-owned copy first — adb-pushed external files are often invisible to the app process.
        val candidates = listOf(
            File(context.filesDir, "models/$MODEL_SUBDIR"),
            preferredDir(context),
            stagingDir(context),
        )
        for (dir in candidates) {
            if (bundleValid(dir)) {
                Log.i(TAG, "Using model dir: ${dir.absolutePath}")
                return dir
            }
        }
        val fallback = preferredDir(context)
        Log.w(
            TAG,
            "Qwen bundle not found. preferred=${fallback.absolutePath} staging=${stagingDir(context).absolutePath}"
        )
        return fallback
    }

    fun pteFile(context: Context): File = File(resolveModelDir(context), PTE_NAME)

    fun tokenizerFile(context: Context): File = File(resolveModelDir(context), TOKENIZER_NAME)

    fun isReady(context: Context): Boolean {
        val dir = resolveModelDir(context)
        val ready = bundleValid(dir)
        if (!ready) {
            val pte = File(dir, PTE_NAME)
            val tok = File(dir, TOKENIZER_NAME)
            Log.w(
                TAG,
                "isReady=false dir=${dir.absolutePath} " +
                    "pteExists=${pte.exists()} pteFile=${pte.isFile} pteLen=${pte.length()} " +
                    "tokExists=${tok.exists()} tokFile=${tok.isFile} tokLen=${tok.length()} " +
                    "extFilesDir=${context.getExternalFilesDir("models")?.absolutePath}"
            )
        }
        return ready
    }

    private fun bundleValid(dir: File): Boolean {
        val pte = File(dir, PTE_NAME)
        val tok = File(dir, TOKENIZER_NAME)
        return pte.isFile && pte.length() > MIN_PTE_BYTES &&
            tok.isFile && tok.length() > MIN_TOKENIZER_BYTES
    }
}
