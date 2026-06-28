package com.medic.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medic.app.ai.AiService
import com.medic.app.ai.PromptTemplates
import com.medic.app.ai.StubAiService
import com.medic.app.ai.TriageOrchestrator
import com.medic.app.ai.VoiceLoopManager
import com.medic.app.data.CorpusChunk
import com.medic.app.nav.PositionSource
import com.medic.app.nav.PositionState
import com.medic.app.nav.PositionStateMachine
import com.medic.app.nav.SolarCompass
import com.medic.app.ui.components.AppSection
import com.medic.app.ui.components.ChatMessage
import com.medic.app.ui.components.Sender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class SosSummary(
    val injury: String = "",
    val position: String = "",
    val peopleAffected: String = "",
    val needs: String = "",
    val severity: String = ""
)

data class AppUiState(
    val section: AppSection = AppSection.TREAT,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isListening: Boolean = false,
    val positionState: PositionState = PositionState(source = PositionSource.GPS_TRUSTED),
    val airplaneModeOn: Boolean = true,

    // ORIENT screen state
    val sunAzimuthDeg: Double? = null,
    val sunElevationDeg: Double? = null,
    val correctedHeadingDeg: Double? = null,

    // COMMUNICATE screen state
    val medicText: String = "",
    val casualtyTranslation: String = "",
    val sosSummary: SosSummary = SosSummary()
)

/**
 * Swap point for Person 1's real AiService: change this single line once
 * the real implementation lands. Nothing else in the app needs to change,
 * because everything is built against the AiService interface.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val aiService: AiService = StubAiService()  // <-- swap to RealAiService() when ready
    private val corpus: List<CorpusChunk> = emptyList()  // <-- populate from bundled corpus + vectors asset at startup
    private val orchestrator = TriageOrchestrator(aiService, corpus)
    private val voiceLoop = VoiceLoopManager(application)

    // Rough fallback location used only to seed the solar compass before a real
    // LocationManager fix is available (e.g. first launch, or already in
    // SOLAR_FIX because GPS is gone). Once a real fix lands, callers should
    // update this via updateRoughLocation() so solar math uses the best
    // location actually known, not this hardcoded default.
    private var roughLat: Double = 37.3
    private var roughLon: Double = -121.9
    private val solarCompass get() = SolarCompass(roughLat, roughLon)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        voiceLoop.initTts()
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = Sender.SYSTEM,
                    text = "Offline first-aid assistant ready. Describe an injury, or press the " +
                        "mic to speak. This app works with no internet connection."
                )
            )
        )
        refreshSunPosition()
    }

    fun onSectionSelected(section: AppSection) {
        _uiState.value = _uiState.value.copy(section = section)
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onSend() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return
        submitQuery(text)
        _uiState.value = _uiState.value.copy(inputText = "")
    }

    private fun submitQuery(text: String) {
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), sender = Sender.USER, text = text)
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + userMsg)

        viewModelScope.launch {
            val result = orchestrator.handleQuery(text)
            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = Sender.ASSISTANT,
                text = result.llmAnswer,
                severity = result.triage.severity,
                citedChunkIds = result.citedChunkIds,
                disclaimerShown = true
            )
            _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + assistantMsg)
            voiceLoop.speak(result.llmAnswer)
        }
    }

    fun onMicToggle() {
        val nowListening = !_uiState.value.isListening
        _uiState.value = _uiState.value.copy(isListening = nowListening)
        if (!nowListening) return // stopping is handled by recordUntilStopped's isCancelled check

        viewModelScope.launch {
            val pcm = voiceLoop.recordUntilStopped(isCancelled = { !_uiState.value.isListening })
            _uiState.value = _uiState.value.copy(isListening = false)
            val transcript = aiService.transcribe(pcm)
            if (transcript.isNotBlank()) {
                submitQuery(transcript)
            }
        }
    }

    // --- ORIENT screen wiring ---

    /**
     * Recomputes the sun's current azimuth/elevation for whatever rough
     * location we have. Cheap enough to call on a timer (e.g. every 30s)
     * from the Activity, or just once per ORIENT screen visit.
     */
    fun refreshSunPosition() {
        val pos = solarCompass.currentSunPosition()
        _uiState.value = _uiState.value.copy(
            sunAzimuthDeg = pos.azimuthDeg,
            sunElevationDeg = pos.elevationDeg
        )
    }

    /** Called once a real LocationManager fix (or last-trusted DR fix) is available. */
    fun updateRoughLocation(lat: Double, lon: Double) {
        roughLat = lat
        roughLon = lon
        refreshSunPosition()
    }

    /**
     * User has physically pointed the phone's top edge at the sun and tapped
     * "SIGHT SUN." [rawDeviceBearing] is whatever the magnetometer/rotation
     * sensor reported at that instant -- the Activity/Composable is
     * responsible for reading that sensor value and passing it in here.
     */
    fun onSightSun(rawDeviceBearing: Double) {
        val correction = solarCompass.computeMagnetometerCorrection(rawDeviceBearing)
        val corrected = solarCompass.trueHeading(rawDeviceBearing, correction)
        _uiState.value = _uiState.value.copy(correctedHeadingDeg = corrected)
    }

    // --- COMMUNICATE screen wiring ---

    fun onMedicTextChange(text: String) {
        _uiState.value = _uiState.value.copy(medicText = text)
    }

    /**
     * Default translation direction is English -> Spanish for the demo;
     * a real build would let the user pick both languages. Kept minimal
     * per the spec's "only if 1-2 are solid" gating for this bonus item.
     */
    fun onTranslate(fromLang: String = "English", toLang: String = "Spanish") {
        val text = _uiState.value.medicText.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            val translation = aiService.translate(text, fromLang, toLang)
            _uiState.value = _uiState.value.copy(casualtyTranslation = translation)
        }
    }

    /**
     * Drafts a structured SOS summary from the chat conversation so far.
     * Severity is taken from the most recent ASSISTANT message's severity
     * tag (set by the safety tree) rather than re-derived here, so the SOS
     * card never disagrees with what TREAT already told the user.
     */
    fun onGenerateSos() {
        val context = _uiState.value.messages.joinToString("\n") { "${it.sender}: ${it.text}" }
        if (context.isBlank()) return

        val lastSeverity = _uiState.value.messages
            .lastOrNull { it.sender == Sender.ASSISTANT && it.severity != null }
            ?.severity?.name ?: "UNKNOWN"

        viewModelScope.launch {
            val prompt = PromptTemplates.sosSummary(context)
            val raw = aiService.generate(prompt)
            _uiState.value = _uiState.value.copy(sosSummary = parseSosSummary(raw, lastSeverity))
        }
    }

    /**
     * Parses the LLM's "FIELD: value" line format from PromptTemplates.sosSummary().
     * Deliberately tolerant -- if a field is missing or malformed, it's left
     * blank rather than crashing the screen. SEVERITY always comes from the
     * safety tree (lastSeverity), never from the LLM's own line, so it can't
     * drift from what TREAT already showed the user.
     */
    private fun parseSosSummary(raw: String, severityFromSafetyTree: String): SosSummary {
        fun fieldValue(label: String): String =
            Regex("""$label:\s*(.+)""", RegexOption.IGNORE_CASE)
                .find(raw)?.groupValues?.get(1)?.trim().orEmpty()

        return SosSummary(
            injury = fieldValue("INJURY").ifBlank { "unknown" },
            position = fieldValue("APPROX_POSITION").ifBlank { "unknown" },
            peopleAffected = fieldValue("PEOPLE_AFFECTED").ifBlank { "unknown" },
            needs = fieldValue("IMMEDIATE_NEEDS").ifBlank { "unknown" },
            severity = severityFromSafetyTree
        )
    }

    // --- Navigation / position state wiring (called by the LocationManager-backed
    //     collector once that's wired to real sensors; exposed here so the UI
    //     layer and SpoofDetector tests have a single place to push updates) ---

    fun updatePositionState(gpsAvailable: Boolean, gpsSpoofed: Boolean) {
        val current = _uiState.value.positionState
        val next = PositionStateMachine.transition(current, gpsAvailable, gpsSpoofed)
        _uiState.value = _uiState.value.copy(positionState = next)
    }

    override fun onCleared() {
        super.onCleared()
        voiceLoop.shutdown()
    }
}
