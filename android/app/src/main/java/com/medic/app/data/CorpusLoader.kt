package com.medic.app.data

import android.content.res.AssetManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object CorpusLoader {
    private const val CORPUS_ASSET = "first_aid_corpus.json"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadFirstAidCorpus(
        assets: AssetManager,
        embedder: suspend (String) -> FloatArray
    ): List<CorpusChunk> {
        val json = assets.open(CORPUS_ASSET).bufferedReader().use { it.readText() }
        return parseFirstAidCorpus(json, embedder)
    }

    suspend fun parseFirstAidCorpus(
        jsonText: String,
        embedder: suspend (String) -> FloatArray
    ): List<CorpusChunk> {
        val rows = json.parseToJsonElement(jsonText).jsonArray
        return rows.map { rowElement ->
            val row = rowElement.jsonObject
            val text = row.getValue("text").jsonPrimitive.content
            CorpusChunk(
                id = row.getValue("id").jsonPrimitive.content,
                category = row.getValue("category").jsonPrimitive.content,
                subtopic = row.getValue("subtopic").jsonPrimitive.content,
                severity = row.getValue("severity").jsonPrimitive.content,
                text = text,
                source = row.getValue("source").jsonPrimitive.content,
                relatedTo = row["relatedTo"]?.jsonPrimitive?.contentOrNull?.takeUnless { it.isBlank() },
                embedding = embedder(text)
            )
        }
    }
}
