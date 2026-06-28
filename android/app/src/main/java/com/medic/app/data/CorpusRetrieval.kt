package com.medic.app.data

import kotlin.math.sqrt

/**
 * Mirrors the JSON schema in /corpus/first_aid_corpus.json. Person 1's
 * embedding pipeline reads that file, embeds each `text` field with BGE,
 * and ships the resulting vectors as a binary asset (e.g.
 * assets/corpus_vectors.bin) alongside this JSON metadata in the APK.
 */
data class CorpusChunk(
    val id: String,
    val category: String,
    val subtopic: String,
    val severity: String,
    val text: String,
    val source: String,
    val relatedTo: String? = null,
    val embedding: FloatArray = FloatArray(0)
)

data class RetrievedChunk(val chunk: CorpusChunk, val score: Float)

/**
 * Simple in-memory cosine-similarity retrieval. The corpus is small enough
 * (under a few hundred short chunks) that a flat scan is fast on-device --
 * no need for an approximate-nearest-neighbor index.
 */
object Retriever {

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Embedding dimension mismatch: ${a.size} vs ${b.size}" }
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom == 0f) 0f else dot / denom
    }

    /**
     * Returns the top [k] chunks by cosine similarity to [queryEmbedding].
     * [minScore] filters out low-confidence matches so the grounded prompt
     * isn't forced to cite irrelevant passages just to fill k slots --
     * if nothing clears the bar, the LLM should say so rather than guess.
     */
    fun topK(
        queryEmbedding: FloatArray,
        corpus: List<CorpusChunk>,
        k: Int = 4,
        minScore: Float = 0.25f
    ): List<RetrievedChunk> {
        return corpus
            .asSequence()
            .filter { it.embedding.isNotEmpty() }
            .map { RetrievedChunk(it, cosineSimilarity(queryEmbedding, it.embedding)) }
            .filter { it.score >= minScore }
            .sortedByDescending { it.score }
            .take(k)
            .toList()
    }
}
