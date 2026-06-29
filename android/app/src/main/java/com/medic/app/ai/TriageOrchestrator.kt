package com.medic.app.ai

import com.medic.app.core.SafetyTree
import com.medic.app.core.TriageResult
import com.medic.app.data.CorpusChunk
import com.medic.app.data.Retriever

data class TriagePipelineResult(
    val triage: TriageResult,
    val llmAnswer: String,
    val citedChunkIds: List<String>
)

/**
 * The single entry point the UI calls for "user described an injury, give
 * me a grounded triage answer." Order of operations matters:
 *   1. SafetyTree.evaluate() runs FIRST and is authoritative for severity.
 *   2. Retrieval runs against the corpus for supporting detail.
 *   3. The LLM is prompted to elaborate WITHOUT overriding step 1's severity.
 * Steps 2-3 use whatever AiService is bound (StubAiService during dev,
 * Person 1's real implementation in production) -- this class never knows
 * which one it's talking to.
 */
class TriageOrchestrator(
    private val aiService: AiService,
    private val corpus: List<CorpusChunk>
) {

    suspend fun handleQuery(userQuery: String): TriagePipelineResult {
        val triage = SafetyTree.evaluate(userQuery)

        val retrieved = if (corpus.isEmpty()) {
            emptyList()
        } else {
            val queryEmbedding = aiService.embed(userQuery)
            Retriever.topK(queryEmbedding, corpus, k = 4)
        }

        val prompt = PromptTemplates.groundedFirstAid(userQuery, retrieved, triage)
        val answer = aiService.generate(prompt)

        val citedIds = Regex("""\[FA-\d{4}]""").findAll(answer)
            .map { it.value.removePrefix("[").removeSuffix("]") }
            .distinct()
            .toList()

        return TriagePipelineResult(triage, answer, citedIds)
    }
}
