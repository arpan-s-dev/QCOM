package com.medic.app.nav.star

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object StarCatalogLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): StarCatalog {
        val json = context.assets.open("yale_bright_stars.json").bufferedReader().use { it.readText() }
        return parse(json)
    }

    fun parse(jsonText: String): StarCatalog {
        val root = json.parseToJsonElement(jsonText).jsonObject
        val name = root["catalog"]?.jsonPrimitive?.contentOrNull ?: "Yale Bright Star Catalog"
        val stars = root.getValue("stars").jsonArray
        val list = buildList {
            for (star in stars) {
                val o = star.jsonObject
                val id = o.getValue("id").jsonPrimitive.content
                add(
                    CatalogStar(
                        id = id,
                        name = o["name"]?.jsonPrimitive?.contentOrNull ?: id,
                        raHours = o.getValue("ra_hours").jsonPrimitive.double,
                        decDeg = o.getValue("dec_deg").jsonPrimitive.double,
                        magnitude = o.getValue("mag").jsonPrimitive.double
                    )
                )
            }
        }
        return StarCatalog(name, list)
    }
}
