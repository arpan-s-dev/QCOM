package com.medic.app.demo

import com.medic.app.ui.calm.SgScreen

/**
 * Curated live-demo scenarios — each runs deterministically without NPU/mic.
 * Asset paths are under `assets/demo/`.
 */
data class DemoScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val screen: SgScreen,
    val steps: String
)

object DemoScenarios {
    const val POWELL_LAT = 37.789261
    const val POWELL_LON = -122.408653

    const val PALM_PROMPT =
        "There's a lot of blood on his palm and it hasn't stopped. I'm near Powell Street in San Francisco."

    /** Exact SafetyTree test strings — always produce CRITICAL / SERIOUS. */
    const val NEGATION_CRITICAL =
        "The bleeding hasn't stopped, it's still pouring out"

    const val NEGATION_SERIOUS =
        "The bleeding has stopped now after I applied pressure"

    val all: List<DemoScenario> = listOf(
        DemoScenario(
            id = "full_powell_field",
            title = "1 · Full demo (start here)",
            subtitle = "Powell St + CRITICAL + hospitals",
            screen = SgScreen.ASSISTANT,
            steps = "Sets Powell GPS, sends palm prompt. Then tap Hospital tab."
        ),
        DemoScenario(
            id = "hospitals_powell",
            title = "2 · Hospital heading",
            subtitle = "Saint Francis 0.7 km west",
            screen = SgScreen.HOSPITAL,
            steps = "Powell St GPS only — nearest ER list updates."
        ),
        DemoScenario(
            id = "negation_critical",
            title = "3 · Negation CRITICAL",
            subtitle = "hasn't stopped → CRITICAL",
            screen = SgScreen.ASSISTANT,
            steps = "Deterministic SafetyTree — not the LLM."
        ),
        DemoScenario(
            id = "negation_serious",
            title = "4 · Negation SERIOUS",
            subtitle = "has stopped now → SERIOUS",
            screen = SgScreen.ASSISTANT,
            steps = "Run right after scenario 3 to show contrast."
        ),
        DemoScenario(
            id = "night_sky_star_fix",
            title = "5 · Night sky STAR_FIX",
            subtitle = "SF stars · heading ~47°",
            screen = SgScreen.LOCATION,
            steps = "Loads bundled Treasure Island photo automatically."
        ),
        DemoScenario(
            id = "wound_photo",
            title = "6 · Wound photo",
            subtitle = "Palm injury checklist",
            screen = SgScreen.MEDICAL,
            steps = "Loads bundled wounded-hand photo."
        )
    )

    fun byId(id: String): DemoScenario? = all.find { it.id == id }
}
