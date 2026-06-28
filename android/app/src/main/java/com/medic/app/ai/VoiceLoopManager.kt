package com.medic.app.ai

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.*
import java.util.Locale

/**
 * Owns the mic -> transcribe -> (caller does RAG+LLM) -> speak loop.
 * Deliberately thin: this class only knows about audio I/O and TTS, never
 * about triage logic -- the caller (ViewModel) decides what to do with the
 * transcript and what text comes back to be spoken.
 *
 * Disclaimers are NOT spoken aloud by design -- they're long, and TTS-ing
 * legal disclaimer text in the middle of a medical emergency is actively
 * counterproductive. The disclaimer is always shown on screen instead
 * (see ChatMessage.disclaimerShown / the UI layer), per the spec's
 * "Disclaimers on screen" requirement.
 */
class VoiceLoopManager(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    companion object {
        private const val SAMPLE_RATE = 16000 // matches AiService.transcribe() PCM16 contract
        private const val TAG = "VoiceLoopManager"
    }

    fun initTts(onReady: () -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.language = Locale.US
                onReady()
            } else {
                Log.w(TAG, "TextToSpeech init failed with status $status")
            }
        }
    }

    fun speak(text: String) {
        if (!ttsReady) {
            Log.w(TAG, "TTS not ready, skipping speak() for: $text")
            return
        }
        // Strip citation tags like [FA-0004] before speaking -- they're
        // useful on screen but meaningless and disruptive read aloud.
        val spokenText = text.replace(Regex("""\[FA-\d{4}]"""), "").trim()
        tts?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "triage-answer")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        audioRecord?.release()
        audioRecord = null
    }

    /**
     * Records audio for up to [maxDurationMs] or until [isCancelled] returns
     * true, then returns the captured PCM16 buffer for AiService.transcribe().
     * Runs on Dispatchers.IO; caller should launch this in a coroutine scope
     * tied to the mic button's press-and-hold or tap-to-toggle lifecycle.
     */
    @Suppress("MissingPermission") // caller is responsible for runtime permission check before invoking
    suspend fun recordUntilStopped(
        maxDurationMs: Long = 15_000,
        isCancelled: () -> Boolean
    ): ShortArray = withContext(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize.coerceAtLeast(4096)
        )
        audioRecord = record

        val chunks = mutableListOf<ShortArray>()
        val chunkBuf = ShortArray(1024)
        val startTime = System.currentTimeMillis()

        try {
            record.startRecording()
            while (!isCancelled() && System.currentTimeMillis() - startTime < maxDurationMs) {
                val read = record.read(chunkBuf, 0, chunkBuf.size)
                if (read > 0) {
                    chunks.add(chunkBuf.copyOf(read))
                }
            }
        } finally {
            record.stop()
            record.release()
            audioRecord = null
        }

        val total = chunks.sumOf { it.size }
        val merged = ShortArray(total)
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(merged, offset)
            offset += chunk.size
        }
        merged
    }
}
