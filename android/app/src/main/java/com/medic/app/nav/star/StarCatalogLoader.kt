package com.medic.app.nav.star

import android.content.Context
import org.json.JSONObject

object StarCatalogLoader {
    fun load(context: Context): StarCatalog {
        val json = context.assets.open("yale_bright_stars.json").bufferedReader().use { it.readText() }
        return parse(json)
    }

    fun parse(json: String): StarCatalog {
        val root = JSONObject(json)
        val name = root.optString("catalog", "Yale Bright Star Catalog")
        val stars = root.getJSONArray("stars")
        val list = buildList {
            for (i in 0 until stars.length()) {
                val o = stars.getJSONObject(i)
                add(
                    CatalogStar(
                        id = o.getString("id"),
                        name = o.optString("name", o.getString("id")),
                        raHours = o.getDouble("ra_hours"),
                        decDeg = o.getDouble("dec_deg"),
                        magnitude = o.getDouble("mag")
                    )
                )
            }
        }
        return StarCatalog(name, list)
    }
}
