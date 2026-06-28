package com.medic.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object OfflineAssetLoader {

    fun loadHospitals(context: Context): List<Hospital> {
        val json = context.assets.open("sf_hospitals.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("hospitals")
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    Hospital(
                        name = o.getString("name"),
                        latitude = o.getDouble("latitude"),
                        longitude = o.getDouble("longitude")
                    )
                )
            }
        }
    }

    fun loadFieldKit(context: Context): Pair<String, List<FieldKitItem>> {
        val json = context.assets.open("field_kit_reference.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val disclaimer = root.getString("disclaimer")
        val arr = root.getJSONArray("items")
        val items = buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    FieldKitItem(
                        name = o.getString("name"),
                        whatItIsFor = o.getString("what_it_is_for"),
                        howToUseSafely = o.getString("how_to_use_safely"),
                        warning = o.getString("warning"),
                        isMedicine = o.optBoolean("is_medicine", false)
                    )
                )
            }
        }
        return disclaimer to items
    }
}
