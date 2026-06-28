package com.medic.app.core

/**
 * Deterministic safety tree for first-aid triage.
 *
 * This engine is AUTHORITATIVE over the LLM: the LLM may explain, cite, and
 * elaborate, but the severity label and the top-line directive shown to the
 * user always come from this tree, never from free-form model output. This
 * is a deliberate design choice for a life-safety app -- LLMs can be talked
 * into softening a "this is critical" judgment by conversational framing;
 * a regex/keyword state machine cannot.
 *
 * Priority order (per TCCC MARCH), evaluated top to bottom -- the first
 * matching condition wins, because a casualty can have multiple injuries
 * and the most urgent one always governs immediate action:
 *
 *   1. NOT_BREATHING        -> CRITICAL
 *   2. ACTIVE_ARTERIAL_BLEED-> CRITICAL
 *   3. SEVERE_BURN          -> CRITICAL
 *   4. SERIOUS              (shock, fractures, head injury signs, etc.)
 *   5. MODERATE             (minor fractures, infection signs, etc.)
 *   6. MINOR                (small cuts, first-degree burns, etc.)
 *
 * The hard part is negation: "the bleeding hasn't stopped" must be CRITICAL,
 * while "the bleeding has stopped now" must NOT be. A naive keyword match on
 * "bleeding" + "stopped" gets this backwards in both directions if it isn't
 * negation-aware. See [NegationAwareMatcher] below.
 */

enum class Severity { CRITICAL, SERIOUS, MODERATE, MINOR, UNKNOWN }

data class TriageResult(
    val severity: Severity,
    val matchedRule: String,
    val directive: String,
    val rawInput: String
)

/**
 * Minimal negation-aware phrase matcher.
 *
 * Approach: scan the text for a target phrase (e.g. "stopped"). Look at a
 * fixed window of words immediately preceding the match. If a negation cue
 * ("hasn't", "has not", "won't", "didn't", "not", "isn't", "can't", "no")
 * appears in that window WITHOUT an intervening contrast/reset word, the
 * match is flagged negated. We also handle the inverse: a negation cue can
 * itself be later cancelled by a trailing "now"/"anymore"-style resolution
 * phrase that the caller checks for explicitly (see [BleedingClassifier]),
 * because "hasn't stopped" and "has stopped now" need genuinely different
 * downstream rules, not just a negation flip on the same rule.
 */
object NegationAwareMatcher {

    private val negationCues = listOf(
        "not", "n't", "no ", "never", "none", "without", "lacking", "unable to"
    )

    // Words that, if found between a negation cue and the target phrase,
    // mean the negation does NOT apply to that phrase (the negation is
    // about something else entirely). Keeps the window from over-firing on
    // unrelated clauses, e.g. "I'm not sure but the bleeding has stopped."
    private val resetWords = listOf("but", "however", "although", "though", ".", ",")

    private const val WINDOW_WORDS = 6

    fun isNegated(text: String, phraseIndex: Int): Boolean {
        val before = text.substring(0, phraseIndex)
        val words = before.trim().split(Regex("\\s+"))
        val window = words.takeLast(WINDOW_WORDS).joinToString(" ")

        // If a reset word appears after the last negation cue in the window,
        // the negation cue doesn't reach the phrase.
        var lastNegationPos = -1
        var lastResetPos = -1
        for (cue in negationCues) {
            val pos = window.lastIndexOf(cue)
            if (pos > lastNegationPos) lastNegationPos = pos
        }
        for (reset in resetWords) {
            val pos = window.lastIndexOf(reset)
            if (pos > lastResetPos) lastResetPos = pos
        }
        if (lastNegationPos == -1) return false
        return lastNegationPos > lastResetPos
    }

    fun findPhrase(text: String, phrase: String): Int = text.lowercase().indexOf(phrase.lowercase())
}

/**
 * Bleeding-specific classifier. This is the case explicitly called out in
 * the spec: "hasn't stopped" -> CRITICAL, "has stopped now" -> SERIOUS
 * (downgraded from critical once controlled, but still not MINOR -- a
 * casualty who just had a tourniquet/packing applied still needs the
 * shock/evacuation guidance, not a "you're fine" message).
 */
object BleedingClassifier {

    private val activeBleedPhrases = listOf(
        "bleeding", "blood", "spurting", "gushing", "hemorrhage", "wound is open"
    )
    private val stoppedCues = listOf("stopped", "controlled", "under control", "stable now", "slowed")
    private val resolvedQualifiers = listOf("now", "finally", "already", "successfully")
    private val arterialCues = listOf("spurting", "spurts", "pulsing", "pulses", "bright red", "gushing")

    /**
     * Returns null if the text doesn't appear to be about bleeding at all,
     * so callers can fall through to other rules.
     */
    fun classify(textRaw: String): Severity? {
        val text = textRaw.lowercase()
        val mentionsBleeding = activeBleedPhrases.any { text.contains(it) }
        if (!mentionsBleeding) return null

        val isArterial = arterialCues.any { text.contains(it) }

        // Clause-level re-escalation check: split on contrast/sequence words
        // and re-test every clause after the first split for active-bleeding
        // language. This catches "stopped, but then started again" -- where
        // "stopped" alone would wrongly read as resolved, but a later clause
        // re-escalates it back to active/uncontrolled.
        val resumeCues = listOf(
            "started again", "started bleeding again", "still going",
            "still bleeding", "began again", "opened back up", "reopened"
        )
        val clauses = text.split(Regex("\\b(?:but|then|however|although)\\b|[,.;]"))
        if (clauses.size > 1) {
            for (clause in clauses.drop(1)) {
                if (resumeCues.any { clause.contains(it) }) return Severity.CRITICAL
            }
        }

        // Find every occurrence of a "stopped"-type cue and check negation.
        var anyStoppedCueNegated = false
        var anyStoppedCueAffirmed = false

        for (cue in stoppedCues) {
            var searchFrom = 0
            while (true) {
                val idx = text.indexOf(cue, searchFrom)
                if (idx == -1) break
                val negated = NegationAwareMatcher.isNegated(text, idx)
                if (negated) {
                    anyStoppedCueNegated = true
                } else {
                    // Affirmed "stopped" -- check it's not immediately undercut,
                    // e.g. "stopped, but it just started again" handled by
                    // caller re-running classify on full sentence; we keep this
                    // classifier intentionally simple/explainable.
                    anyStoppedCueAffirmed = true
                }
                searchFrom = idx + cue.length
            }
        }

        return when {
            // Bleeding explicitly NOT stopped -> still active -> CRITICAL
            // regardless of arterial vs venous, since user already flagged
            // it as ongoing and unresolved.
            anyStoppedCueNegated -> Severity.CRITICAL

            // Bleeding explicitly stopped/controlled -> no longer the most
            // urgent active threat. Still SERIOUS (shock risk, recently
            // life-threatening), not downgraded all the way to MINOR.
            anyStoppedCueAffirmed -> Severity.SERIOUS

            // No "stopped" cue at all -- bleeding mentioned with no
            // resolution status. Arterial markers push this to CRITICAL;
            // otherwise default to SERIOUS and let the LLM/user supply more
            // detail. We do NOT default ambiguous active bleeding to MINOR.
            isArterial -> Severity.CRITICAL
            else -> Severity.SERIOUS
        }
    }
}

object SafetyTree {

    private val notBreathingPhrases = listOf(
        "not breathing", "isn't breathing", "stopped breathing", "no breathing",
        "not breathe", "can't breathe", "cannot breathe", "no pulse", "not responsive and not breathing"
    )
    private val severeBurnPhrases = listOf(
        "third degree", "3rd degree", "third-degree", "charred", "white and leathery",
        "burn covers most", "large burn", "burn over 20", "circumferential burn"
    )
    private val seriousPhrases = listOf(
        "shock", "fracture", "broken bone", "compound fracture", "open fracture",
        "head injury", "concussion", "unconscious", "spine", "neck injury",
        "hypothermia", "frostbite", "second degree", "2nd degree"
    )
    private val moderatePhrases = listOf(
        "infection", "red streak", "swelling", "sprain", "closed fracture", "mild burn", "rash"
    )
    private val minorPhrases = listOf(
        "small cut", "scrape", "minor cut", "first degree", "1st degree", "splinter", "bruise"
    )

    fun evaluate(input: String): TriageResult {
        val text = input.lowercase()

        // Priority 1: NOT BREATHING (always wins, no exceptions)
        if (notBreathingPhrases.any { text.contains(it) }) {
            return TriageResult(
                Severity.CRITICAL, "NOT_BREATHING",
                "Start CPR immediately. Call for help. Do not stop to treat other injuries first.",
                input
            )
        }

        // Priority 2: bleeding (negation-aware)
        val bleedSeverity = BleedingClassifier.classify(text)
        if (bleedSeverity == Severity.CRITICAL) {
            return TriageResult(
                Severity.CRITICAL, "ACTIVE_ARTERIAL_OR_UNCONTROLLED_BLEED",
                "Apply firm direct pressure or a tourniquet now. Do not wait for the bleeding to look worse before acting.",
                input
            )
        }
        if (bleedSeverity == Severity.SERIOUS) {
            return TriageResult(
                Severity.SERIOUS, "BLEEDING_CONTROLLED",
                "Bleeding is controlled. Watch for shock, keep the casualty warm, and monitor the dressing/tourniquet.",
                input
            )
        }

        // Priority 3: severe burns
        if (severeBurnPhrases.any { text.contains(it) }) {
            return TriageResult(
                Severity.CRITICAL, "SEVERE_BURN",
                "Cover loosely with a clean dry cloth, treat for shock, and seek urgent evacuation. Do not apply ice or extensive cooling.",
                input
            )
        }

        // Priority 4: SERIOUS
        if (seriousPhrases.any { text.contains(it) }) {
            return TriageResult(
                Severity.SERIOUS, "SERIOUS_OTHER",
                "This needs careful stabilization and likely evacuation. Avoid unnecessary movement until assessed further.",
                input
            )
        }

        // Priority 5: MODERATE
        if (moderatePhrases.any { text.contains(it) }) {
            return TriageResult(
                Severity.MODERATE, "MODERATE_OTHER",
                "Clean and monitor. Watch for worsening signs over the next hours/days.",
                input
            )
        }

        // Priority 6: MINOR
        if (minorPhrases.any { text.contains(it) }) {
            return TriageResult(
                Severity.MINOR, "MINOR_OTHER",
                "Basic first aid should be sufficient. Monitor for any change.",
                input
            )
        }

        return TriageResult(
            Severity.UNKNOWN, "NO_MATCH",
            "I don't have enough detail to triage this confidently. Describe the injury, whether it's bleeding, and whether the person is breathing normally.",
            input
        )
    }
}
