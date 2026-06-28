package com.medic.app.ai

import android.content.Context
import java.io.File

/**
 * On-device paths for the pre-exported Qwen3 1.7B hybrid PTE (SM8750).
 * The 1.7GB .pte is too large for APK assets — push once via [android/push_qwen_models.ps1].
 */
object QwenModelPaths {
    const val PTE_NAME = "hybrid_llama_qnn.pte"
    const val TOKENIZER_NAME = "tokenizer.json"
    const val CHAT_TEMPLATE_NAME = "chat_template.jinja"
    const val MAX_SEQ_LEN = 4096

    fun modelDir(context: Context): File =
        File(context.getExternalFilesDir("models"), "qwen3-1_7b").apply { mkdirs() }

    fun pteFile(context: Context): File = File(modelDir(context), PTE_NAME)

    fun tokenizerFile(context: Context): File = File(modelDir(context), TOKENIZER_NAME)

    fun isReady(context: Context): Boolean =
        pteFile(context).isFile && pteFile(context).length() > 1_000_000_000L &&
            tokenizerFile(context).isFile && tokenizerFile(context).length() > 1_000L
}
