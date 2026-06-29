package com.medic.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medic.app.ai.AiService
import com.medic.app.ai.AiServiceFactory
import com.medic.app.ai.PromptTemplates
import com.medic.app.ai.TriageOrchestrator
import com.medic.app.ai.VoiceLoopManager
import com.medic.app.core.SafetyTree
import com.medic.app.core.TriageResult
import com.medic.app.data.CorpusChunk
import com.medic.app.data.FieldKitItem
import com.medic.app.data.Hospital
import com.medic.app.data.HospitalFinder
import com.medic.app.data.HospitalWithBearing
import com.medic.app.data.OfflineAssetLoader
import com.medic.app.nav.PositionSource
import com.medic.app.nav.PositionState
import com.medic.app.nav.PositionStateMachine
import com.medic.app.nav.SolarCompass
import com.medic.app.demo.DemoScenario
import com.medic.app.demo.DemoScenarios
import com.medic.app.nav.star.StarNavigationPipeline
import com.medic.app.ui.components.AppSection
import com.medic.app.ui.components.ChatMessage
import com.medic.app.ui.components.Sender
import com.medic.app.ui.screens.OrientNavMode
import com.medic.app.ui.screens.StarNavUiState
import com.medic.app.ui.screens.TreatSubMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
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
    val treatSubMode: TreatSubMode = TreatSubMode.TRIAGE,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isListening: Boolean = false,
    val positionState: PositionState = PositionState(
        source = PositionSource.GPS_TRUSTED,
        lastTrustedLat = 37.7749,
        lastTrustedLon = -122.4194
    ),
    val nearestHospitals: List<HospitalWithBearing> = emptyList(),

    // Device position fix backing the nearest-hospital ranking. Until a real
    // fix arrives, the app falls back to a cached approximate position.
    val hasDeviceFix: Boolean = false,
    val deviceFixAccuracyM: Float? = null,
    val deviceFixProvider: String? = null,
    val deviceFixAgeMs: Long? = null,

    val fieldKitDisclaimer: String = "",
    val fieldKitItems: List<FieldKitItem> = emptyList(),

    // ORIENT screen state
    val orientNavMode: OrientNavMode = OrientNavMode.SOLAR,
    val sunAzimuthDeg: Double? = null,
    val sunElevationDeg: Double? = null,
    val correctedHeadingDeg: Double? = null,
    val liveHeadingDeg: Double? = null,
    val starNav: StarNavUiState = StarNavUiState(),

    // Medical wound-photo analysis
    val woundImage: ImageBitmap? = null,
    val woundAnalyzing: Boolean = false,
    val woundAssessment: String? = null,

    // COMMUNICATE screen state
    val medicText: String = "",
    val casualtyTranslation: String = "",
    val sosSummary: SosSummary = SosSummary(),

    /** Live-demo mode — scenarios from the Demo chip next to Offline ready. */
    val demoModeActive: Boolean = false,
    val activeDemoScenarioId: String? = null,
    val demoBanner: String? = null,
    /** Consumed by SafeGuideApp to switch tab; cleared after navigation. */
    val demoNavigateTo: String? = null
)

/**
 * Swap point for Person 1's real AiService: change this single line once
 * the real implementation lands. Nothing else in the app needs to change,
 * because everything is built against the AiService interface.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val voiceLoop = VoiceLoopManager(application)

    // Cached last-known fix (simulates pre-jam GPS) refined by heading/DR elsewhere.
    private var roughLat: Double = 37.7749
    private var roughLon: Double = -122.4194
    private val hospitals: List<Hospital> = OfflineAssetLoader.loadHospitals(application)
    private val solarCompass get() = SolarCompass(roughLat, roughLon)
    private val starNavigationPipeline by lazy { StarNavigationPipeline(application) }

    /** While demo is active, real GPS must not overwrite Powell St mock fix. */
    private var demoLocationLocked = false

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        AiServiceFactory.create(application, viewModelScope)
        voiceLoop.initTts()
        val (kitDisclaimer, kitItems) = OfflineAssetLoader.loadFieldKit(getApplication())
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = Sender.SYSTEM,
                    text = "Offline first-aid assistant ready. Describe an injury, or press the " +
                        "mic to speak. Reference only — not a diagnosis or prescription."
                )
            ),
            fieldKitDisclaimer = kitDisclaimer,
            fieldKitItems = kitItems
        )
        refreshSunPosition()
        refreshNearestHospitals()
    }

    fun onTreatSubModeChange(mode: TreatSubMode) {
        _uiState.value = _uiState.value.copy(treatSubMode = mode)
    }

    private fun approximateLat(): Double =
        _uiState.value.positionState.lastTrustedLat ?: roughLat

    private fun approximateLon(): Double =
        _uiState.value.positionState.lastTrustedLon ?: roughLon

    private fun refreshNearestHospitals() {
        val nearest = HospitalFinder.nearest(
            hospitals = hospitals,
            approxLat = approximateLat(),
            approxLon = approximateLon(),
            count = 3
        )
        _uiState.value = _uiState.value.copy(nearestHospitals = nearest)
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

        // Demo mode: instant SafetyTree + stub text — never touch NPU or mic/TTS.
        if (_uiState.value.demoModeActive) {
            appendDemoAssistantReply(text)
            return
        }

        viewModelScope.launch {
            try {
                val aiService = AiServiceFactory.serviceForQuery(getApplication())
                val orchestrator = TriageOrchestrator(aiService, emptyList())
                val result = orchestrator.handleQuery(text)
                val (_, bundle) = com.medic.app.ai.QwenModelPaths.resolve(getApplication())
                val answerText = if (aiService is com.medic.app.ai.StubAiService &&
                    com.medic.app.ai.QwenModelPaths.isReady(getApplication())
                ) {
                    "[NPU model unavailable for ${bundle.subdir} — export a matching PTE " +
                        "(runtime/scripts/export_qwen06_sm8750.sh) or push via push_qwen_models.ps1. " +
                        "Showing offline stub.]\n\n${result.llmAnswer}"
                } else {
                    result.llmAnswer
                }
                appendAssistantReply(answerText, result.triage.severity, result.citedChunkIds, speak = true)
            } catch (e: Exception) {
                appendAssistantReply(
                    "Model error: ${e.message ?: e.javaClass.simpleName}. " +
                        "Use Demo mode or type triage prompts offline.",
                    severity = null,
                    citedChunkIds = emptyList(),
                    speak = false
                )
            }
        }
    }

    private fun appendDemoAssistantReply(userText: String) {
        val triage = SafetyTree.evaluate(userText)
        val answer = demoAnswerText(userText, triage)
        appendAssistantReply(answer, triage.severity, emptyList(), speak = false)
    }

    private fun demoAnswerText(userText: String, triage: TriageResult): String {
        val body = when {
            userText.contains("bleeding", ignoreCase = true) ||
                userText.contains("blood", ignoreCase = true) ->
                "Apply firm direct pressure with a clean cloth. Keep pressure continuous. " +
                    "If bleeding is severe and does not stop, prepare for evacuation.\n\n" +
                    triage.directive
            userText.contains("burn", ignoreCase = true) ->
                "Cool the burn with clean water if safe to do so. Cover loosely. Monitor for shock."
            else -> triage.directive
        }
        return "[DEMO — offline stub]\n\n$body\n\nReference / triage only — not a diagnosis."
    }

    private fun appendAssistantReply(
        text: String,
        severity: com.medic.app.core.Severity?,
        citedChunkIds: List<String>,
        speak: Boolean
    ) {
        val assistantMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.ASSISTANT,
            text = text,
            severity = severity,
            citedChunkIds = citedChunkIds,
            disclaimerShown = true
        )
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + assistantMsg)
        if (speak) voiceLoop.speak(text)
    }

    fun onMicToggle() {
        val nowListening = !_uiState.value.isListening
        _uiState.value = _uiState.value.copy(isListening = nowListening)
        if (!nowListening) return // stopping is handled by recordUntilStopped's isCancelled check

        viewModelScope.launch {
            val pcm = voiceLoop.recordUntilStopped(isCancelled = { !_uiState.value.isListening })
            _uiState.value = _uiState.value.copy(isListening = false)
            val transcript = AiServiceFactory.create(getApplication(), viewModelScope).transcribe(pcm)
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

    /**
     * Called once a real LocationManager fix (or last-trusted DR fix) is
     * available. A device fix is treated as GPS_TRUSTED — it's a real
     * receiver reading, even if cached from before the network went down.
     */
    fun updateRoughLocation(
        lat: Double,
        lon: Double,
        accuracyMeters: Float? = null,
        provider: String? = null,
        ageMillis: Long? = null
    ) {
        if (demoLocationLocked && provider != "demo") return
        roughLat = lat
        roughLon = lon
        _uiState.value = _uiState.value.copy(
            positionState = _uiState.value.positionState.copy(
                source = PositionSource.GPS_TRUSTED,
                lastTrustedLat = lat,
                lastTrustedLon = lon
            ),
            hasDeviceFix = true,
            deviceFixAccuracyM = accuracyMeters,
            deviceFixProvider = provider,
            deviceFixAgeMs = ageMillis
        )
        refreshSunPosition()
        refreshNearestHospitals()
    }

    fun onOrientNavModeChange(mode: OrientNavMode) {
        _uiState.value = _uiState.value.copy(orientNavMode = mode)
    }

    /**
     * Live magnetometer heading for the compass dial. Polled from the Activity;
     * skips no-op updates (same whole degree) so a still phone doesn't churn
     * recomposition.
     */
    fun updateLiveHeading(deg: Double) {
        if (_uiState.value.liveHeadingDeg?.toInt() == deg.toInt()) return
        _uiState.value = _uiState.value.copy(liveHeadingDeg = deg)
    }

    /**
     * Demo control: simulate (or clear) a GPS spoofing attack so the position
     * state machine's fallback to dead reckoning can be shown live. Routes
     * through the same [updatePositionState] the real spoof detector would use.
     */
    fun setSpoofDemo(spoofed: Boolean) {
        updatePositionState(gpsAvailable = true, gpsSpoofed = spoofed)
    }

    /**
     * User added a wound photo. Loads + downscales the image and attaches a
     * reference infection-check. The on-device vision model isn't wired yet, so
     * the assessment is an honest guided checklist, never a diagnosis.
     */
    fun onMedicalImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(woundAnalyzing = true, woundAssessment = null, woundImage = null)
        val app = getApplication<Application>()
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                StarNavigationPipeline.loadBitmap(app, uri)?.let { downscale(it, 1080) }
            }
            _uiState.value = _uiState.value.copy(
                woundImage = bitmap?.asImageBitmap(),
                woundAnalyzing = false,
                woundAssessment = woundCheckReference()
            )
        }
    }

    private fun downscale(bmp: Bitmap, maxDim: Int): Bitmap {
        val longest = maxOf(bmp.width, bmp.height)
        if (longest <= maxDim) return bmp
        val scale = maxDim.toFloat() / longest
        return Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true)
    }

    private fun woundCheckReference(): String =
        "Photo added. On-device image analysis isn't wired to the vision model yet, " +
            "so this is a guided self-check — not a diagnosis.\n\n" +
            "Injury: note depth, active bleeding, and whether the edges gape open.\n\n" +
            "Infection signs to look for: spreading redness around the wound, swelling, " +
            "warmth, pus or cloudy fluid, a bad smell, increasing pain, or fever.\n\n" +
            "If two or more infection signs are present, treat it as a likely infection " +
            "and get to care as soon as you can."

    /**
     * User has physically pointed the phone's top edge at the sun and tapped
     * "SIGHT SUN." [rawDeviceBearing] is whatever the magnetometer/rotation
     * sensor reported at that instant -- the Activity/Composable is
     * responsible for reading that sensor value and passing it in here.
     */
    fun onSightSun(rawDeviceBearing: Double) {
        val correction = solarCompass.computeMagnetometerCorrection(rawDeviceBearing)
        val corrected = solarCompass.trueHeading(rawDeviceBearing, correction)
        applyCelestialHeading(corrected, PositionSource.SOLAR_FIX)
    }

    /**
     * User imported a night-sky photo. Runs star detection + plate solve on a
     * background thread; updates heading via the same pipeline as solar compass.
     */
    fun onNightSkyImageSelected(
        uri: Uri,
        deviceAzimuthDeg: Double,
        devicePitchDeg: Double
    ) {
        val app = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            orientNavMode = OrientNavMode.NIGHT_SKY,
            starNav = StarNavUiState(processing = true, message = "Loading image…")
        )

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val bitmap = StarNavigationPipeline.loadBitmap(app, uri)
                    ?: return@withContext null
                try {
                    val fileName = StarNavigationPipeline.fileNameFromUri(app, uri)
                    val hash = StarNavigationPipeline.hashUri(app, uri)
                    starNavigationPipeline.process(
                        bitmap = bitmap,
                        imageFileName = fileName,
                        contentHashHex = hash,
                        devicePitchDeg = devicePitchDeg,
                        deviceAzimuthDeg = deviceAzimuthDeg
                    )
                } finally {
                    bitmap.recycle()
                }
            }

            if (result == null) {
                _uiState.value = _uiState.value.copy(
                    starNav = StarNavUiState(
                        processing = false,
                        message = "Could not read the selected image."
                    )
                )
                return@launch
            }

            val solve = result.solve
            if (solve.success && solve.trueNorthHeadingDeg != null) {
                applyCelestialHeading(solve.trueNorthHeadingDeg, PositionSource.STAR_FIX)
                _uiState.value = _uiState.value.copy(
                    starNav = StarNavUiState(
                        processing = false,
                        detectedStars = result.detection.stars.size,
                        message = solve.message,
                        approximateLat = solve.approximateLatDeg,
                        latUncertainty = solve.latUncertaintyDeg,
                        solverKind = solve.solverKind
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    starNav = StarNavUiState(
                        processing = false,
                        detectedStars = result.detection.stars.size,
                        message = solve.message,
                        solverKind = solve.solverKind
                    )
                )
            }
        }
    }

    private fun applyCelestialHeading(corrected: Double, source: PositionSource) {
        _uiState.value = _uiState.value.copy(
            correctedHeadingDeg = corrected,
            positionState = _uiState.value.positionState.copy(
                source = source,
                headingDegrees = corrected.toFloat()
            )
        )
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
            val stub = AiServiceFactory.create(getApplication(), viewModelScope)
            val translation = stub.translate(text, fromLang, toLang)
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
            val stub = AiServiceFactory.create(getApplication(), viewModelScope)
            val prompt = PromptTemplates.sosSummary(context)
            val raw = stub.generate(prompt)
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
        refreshNearestHospitals()
    }

    fun clearDemoNavigation() {
        _uiState.value = _uiState.value.copy(demoNavigateTo = null)
    }

    fun exitDemoMode() {
        demoLocationLocked = false
        _uiState.value = _uiState.value.copy(
            demoModeActive = false,
            activeDemoScenarioId = null,
            demoBanner = null
        )
        setSpoofDemo(false)
    }

    /** Run a curated offline demo scenario (no mic / NPU required). */
    fun runDemoScenario(scenario: DemoScenario) {
        _uiState.value = _uiState.value.copy(
            demoModeActive = true,
            activeDemoScenarioId = scenario.id,
            demoBanner = scenario.subtitle,
            demoNavigateTo = scenario.screen.name
        )
        when (scenario.id) {
            "full_powell_field" -> {
                applyPowellDemoLocation()
                submitQuery(DemoScenarios.PALM_PROMPT)
                _uiState.value = _uiState.value.copy(
                    demoBanner = "CRITICAL shown · tap Hospital tab · head WEST ~0.7 km to Saint Francis"
                )
            }
            "hospitals_powell" -> {
                applyPowellDemoLocation()
                _uiState.value = _uiState.value.copy(
                    demoBanner = "Saint Francis Memorial ~0.7 km WEST (270°)"
                )
            }
            "negation_critical" -> {
                submitQuery(DemoScenarios.NEGATION_CRITICAL)
                _uiState.value = _uiState.value.copy(demoBanner = "CRITICAL — hasn't stopped")
            }
            "negation_serious" -> {
                submitQuery(DemoScenarios.NEGATION_SERIOUS)
                _uiState.value = _uiState.value.copy(demoBanner = "SERIOUS — has stopped now")
            }
            "night_sky_star_fix" -> {
                _uiState.value = _uiState.value.copy(orientNavMode = OrientNavMode.NIGHT_SKY)
                runDemoNightSkyAsset("demo/demo_night_sf_treasure_island.jpg", "demo_night_sf_treasure_island.jpg")
            }
            "wound_photo" -> runDemoWoundAsset("demo/demo_wounded_hand.jpg")
        }
    }

    /** During demo, keep Powell St fix instead of overwriting with live GPS. */
    fun onUseMyLocationClicked(refreshRealLocation: () -> Unit) {
        if (demoLocationLocked) {
            applyPowellDemoLocation()
        } else {
            refreshRealLocation()
        }
    }

    private fun applyPowellDemoLocation() {
        demoLocationLocked = true
        updateRoughLocation(DemoScenarios.POWELL_LAT, DemoScenarios.POWELL_LON, accuracyMeters = 8f, provider = "demo")
    }

    private fun runDemoNightSkyAsset(assetPath: String, fileName: String) {
        val app = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            orientNavMode = OrientNavMode.NIGHT_SKY,
            starNav = StarNavUiState(processing = true, message = "Demo image…")
        )
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val bitmap = app.assets.open(assetPath).use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                } ?: return@withContext null
                try {
                    val digest = java.security.MessageDigest.getInstance("SHA-256")
                    app.assets.open(assetPath).use { stream ->
                        val buf = ByteArray(8192)
                        while (true) {
                            val n = stream.read(buf)
                            if (n <= 0) break
                            digest.update(buf, 0, n)
                        }
                    }
                    val hash = digest.digest().joinToString("") { "%02x".format(it) }
                    starNavigationPipeline.process(
                        bitmap = bitmap,
                        imageFileName = fileName,
                        contentHashHex = hash,
                        devicePitchDeg = 0.0,
                        deviceAzimuthDeg = _uiState.value.liveHeadingDeg ?: 0.0
                    )
                } finally {
                    bitmap.recycle()
                }
            }
            if (result == null) {
                applyCelestialHeading(47.0, PositionSource.STAR_FIX)
                _uiState.value = _uiState.value.copy(
                    starNav = StarNavUiState(
                        processing = false,
                        detectedStars = 0,
                        message = "Demo STAR_FIX — Treasure Island SF (~47° true north).",
                        approximateLat = 37.8147,
                        latUncertainty = 1.0,
                        solverKind = "demo-fallback"
                    ),
                    demoBanner = "STAR_FIX ~47° · night sky demo complete"
                )
                return@launch
            }
            val solve = result.solve
            if (solve.success && solve.trueNorthHeadingDeg != null) {
                applyCelestialHeading(solve.trueNorthHeadingDeg, PositionSource.STAR_FIX)
                _uiState.value = _uiState.value.copy(
                    starNav = StarNavUiState(
                        processing = false,
                        detectedStars = result.detection.stars.size,
                        message = solve.message,
                        approximateLat = solve.approximateLatDeg,
                        latUncertainty = solve.latUncertaintyDeg,
                        solverKind = solve.solverKind
                    ),
                    demoBanner = "STAR_FIX ${solve.trueNorthHeadingDeg.roundToInt()}° · ${result.detection.stars.size} stars"
                )
            } else {
                applyCelestialHeading(47.0, PositionSource.STAR_FIX)
                _uiState.value = _uiState.value.copy(
                    starNav = StarNavUiState(
                        processing = false,
                        detectedStars = result.detection.stars.size,
                        message = "Demo STAR_FIX — Treasure Island SF (~47° true north).",
                        approximateLat = 37.8147,
                        latUncertainty = 1.0,
                        solverKind = "demo-fallback"
                    ),
                    demoBanner = "STAR_FIX ~47° · night sky demo complete"
                )
            }
        }
    }

    private fun runDemoWoundAsset(assetPath: String) {
        _uiState.value = _uiState.value.copy(woundAnalyzing = true, woundAssessment = null, woundImage = null)
        val app = getApplication<Application>()
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                app.assets.open(assetPath).use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)?.let { downscale(it, 1080) }
                }
            }
            _uiState.value = _uiState.value.copy(
                woundImage = bitmap?.asImageBitmap(),
                woundAnalyzing = false,
                woundAssessment = woundCheckReference(),
                demoBanner = "Wound photo loaded — infection checklist (not diagnosis)"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceLoop.shutdown()
    }
}
