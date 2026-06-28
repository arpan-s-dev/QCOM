package com.medic.app.ai

/**
 * Contract for all model-backed capabilities. Person 1 implements this with
 * the real on-device models (Genie bundle: Qwen3-4B or Phi-3.5-mini, BGE
 * embedder, on-device ASR). Person 2's app (this module) is built entirely
 * against this interface plus [StubAiService], so the UI/RAG/safety-tree
 * layers never block on model integration landing.
 *
 * Everything here must work fully offline / airplane-mode -- no network
 * calls, no remote endpoints. That constraint is the whole point of the app.
 */
interface AiService {

    /** True once the real model(s) are loaded and ready for inference. */
    val isReady: Boolean

    /**
     * Embeds [text] into a fixed-length float vector for cosine-similarity
     * retrieval against the pre-embedded corpus shipped in the APK.
     * Must use the SAME embedding model/dimension the corpus was embedded
     * with, or retrieval will silently return garbage.
     */
    suspend fun embed(text: String): FloatArray

    /**
     * Generates a grounded answer given the user's query and the retrieved
     * top-k corpus passages. Implementations MUST enforce (via prompt and,
     * ideally, output checking) that the model answers ONLY using the
     * supplied passages and includes citation tags like [FA-0004] inline,
     * plus a disclaimer. See [PromptTemplates.groundedFirstAid].
     */
    suspend fun generate(prompt: String): String

    /**
     * Converts a recorded audio buffer (16kHz mono PCM, matching whatever
     * format the mic pipeline captures) into text. Implementation is
     * on-device ASR -- no cloud STT.
     */
    suspend fun transcribe(audioPcm16: ShortArray): String

    /** Free-text translation between two languages, fully on-device. */
    suspend fun translate(text: String, fromLang: String, toLang: String): String
}

/**
 * Canned, deterministic responses so every other layer (UI, safety tree,
 * voice loop, navigation status strip) can be built, demoed, and tested
 * before the real models are wired in. Swap StubAiService -> the real
 * implementation behind a single DI binding; nothing else changes.
 */
class StubAiService : AiService {

    override val isReady: Boolean = true

    override suspend fun embed(text: String): FloatArray {
        // Deterministic fake embedding: stable per input so stub retrieval
        // is at least reproducible during UI development, not used for any
        // real similarity ranking.
        val dim = 384
        val seed = text.hashCode()
        val rng = java.util.Random(seed.toLong())
        return FloatArray(dim) { rng.nextFloat() * 2f - 1f }
    }

    override suspend fun generate(prompt: String): String {
        return when {
            prompt.contains("structured distress summary", ignoreCase = true) ->
                // SOS prompt -- return canned but correctly-shaped FIELD: value lines
                // so MainViewModel.parseSosSummary() has something real to parse
                // even before the real model is wired in.
                "INJURY: unspecified (stub -- describe injury in TREAT for a real summary)\n" +
                "APPROX_POSITION: unknown\n" +
                "PEOPLE_AFFECTED: 1\n" +
                "IMMEDIATE_NEEDS: evacuation, monitoring\n" +
                "SEVERITY: see TREAT tab"
            prompt.contains("bleeding", ignoreCase = true) ->
                "[STUB RESPONSE] Apply firm direct pressure with a clean cloth. " +
                "If bleeding is severe and doesn't stop, apply a tourniquet 2-3 inches " +
                "above the wound. [FA-0002] [FA-0004]\n\n" +
                "This is a stub answer for development -- not yet grounded in the real " +
                "retrieved passages or model. Disclaimer: this app does not replace " +
                "professional medical care; seek evacuation when possible."
            prompt.contains("burn", ignoreCase = true) ->
                "[STUB RESPONSE] Cool minor burns with clean water for 10-20 minutes. " +
                "For severe/large burns, do not cool extensively -- cover loosely and treat " +
                "for shock instead. [FA-0051] [FA-0053]\n\n" +
                "Disclaimer: this app does not replace professional medical care."
            else ->
                "[STUB RESPONSE] I don't have a real model wired in yet -- this is " +
                "placeholder text from StubAiService so the UI keeps working during " +
                "development. Describe the injury (bleeding? breathing? burn?) for a " +
                "more specific stub answer."
        }
    }

    override suspend fun transcribe(audioPcm16: ShortArray): String {
        return "[STUB TRANSCRIPT] (no real ASR wired in -- replace StubAiService to enable mic input)"
    }

    override suspend fun translate(text: String, fromLang: String, toLang: String): String {
        return "[STUB TRANSLATION $fromLang->$toLang] $text"
    }
}
