package com.medic.app.ai

import com.medic.app.data.RetrievedChunk
import com.medic.app.core.TriageResult

/**
 * Builds the prompt sent to AiService.generate(). The grounding rules here
 * are the second line of defense after SafetyTree -- the safety tree
 * decides severity and the top-line directive; this prompt makes sure any
 * elaboration the LLM adds stays tied to retrieved, source-tagged passages
 * instead of the model's own (unverified, possibly wrong) medical training.
 */
object PromptTemplates {

    fun groundedFirstAid(
        userQuery: String,
        retrieved: List<RetrievedChunk>,
        triage: TriageResult
    ): String {
        val passagesBlock = if (retrieved.isEmpty()) {
            "(No corpus passages matched closely enough -- say you don't have a confident " +
            "grounded answer for this specific detail, and fall back to the safety-tree " +
            "directive only.)"
        } else {
            retrieved.joinToString("\n\n") { rc ->
                "[${rc.chunk.id}] (${rc.chunk.category}/${rc.chunk.subtopic}, " +
                "score=${"%.2f".format(rc.score)}):\n${rc.chunk.text}"
            }
        }

        return """
            You are an offline first-aid assistant running with no internet connection.
            A deterministic safety system has already classified this situation as
            severity=${triage.severity} (rule: ${triage.matchedRule}) with this directive:
            "${triage.directive}"

            RULES YOU MUST FOLLOW:
            1. Do not contradict or soften the severity/directive above. You may explain
               and elaborate on it, but the user must come away with that same urgency level.
            2. Answer ONLY using facts contained in the passages below. If the passages
               don't cover something the user asked, say so explicitly instead of filling
               in from general medical knowledge.
            3. Cite every factual claim with the bracketed passage id it came from, e.g. [FA-0004].
            4. Keep the answer short and speakable -- this may be read aloud by
               text-to-speech to someone in an emergency. Prefer short sentences.
            5. End with this exact disclaimer line: "This is offline first-aid guidance,
               not a substitute for professional medical care -- seek evacuation when possible."

            RETRIEVED PASSAGES:
            $passagesBlock

            USER QUERY:
            "$userQuery"

            Respond now, following all rules above.
        """.trimIndent()
    }

    fun sosSummary(conversationContext: String): String {
        return """
            You are drafting a structured distress summary from the conversation below,
            to be read by a rescuer who has limited time. Output ONLY these fields, each
            on its own line, no extra commentary:

            INJURY: <one short phrase>
            APPROX_POSITION: <whatever location info is available, or "unknown">
            PEOPLE_AFFECTED: <number or "unknown">
            IMMEDIATE_NEEDS: <comma-separated short list>
            SEVERITY: <CRITICAL/SERIOUS/MODERATE/MINOR as already determined>

            CONVERSATION CONTEXT:
            $conversationContext
        """.trimIndent()
    }

    fun translate(text: String, fromLang: String, toLang: String): String {
        return """
            Translate the following message from $fromLang to $toLang. This is a
            medic-casualty communication in an emergency -- prioritize clarity and
            literal accuracy over fluency. Output ONLY the translated text, nothing else.

            TEXT: "$text"
        """.trimIndent()
    }
}
