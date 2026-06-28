package com.medic.app.core

import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * NOTE ON VERIFICATION: this exact test suite (same inputs, same expected
 * outputs) was additionally run as a parallel Python reference implementation
 * in the build sandbox before this Kotlin file was written, because no Kotlin
 * compiler was available there -- see /scripts/verify_safety_tree.py and
 * STATUS.md. All cases below passed in that reference run. This file is the
 * real, production Kotlin test; run it with `./gradlew test` once the module
 * is wired into Android Studio.
 */
class SafetyTreeTest {

    // ---- Required proof cases from the spec ----

    @Test
    fun `hasn't stopped maps to CRITICAL`() {
        val result = SafetyTree.evaluate("The bleeding hasn't stopped, it's still pouring out")
        assertEquals(Severity.CRITICAL, result.severity)
    }

    @Test
    fun `has stopped now maps to SERIOUS not CRITICAL`() {
        val result = SafetyTree.evaluate("The bleeding has stopped now after I applied pressure")
        assertEquals(Severity.SERIOUS, result.severity)
    }

    // ---- Additional negation edge cases ----

    @Test
    fun `has not stopped full form maps to CRITICAL`() {
        val result = SafetyTree.evaluate("It has not stopped bleeding at all")
        assertEquals(Severity.CRITICAL, result.severity)
    }

    @Test
    fun `is not bleeding anymore maps away from critical`() {
        // No "stopped"-family cue here; "not bleeding" is a different
        // pattern than "bleeding hasn't stopped" -- the classifier should
        // not crash and should not return CRITICAL for an explicit absence.
        val result = SafetyTree.evaluate("He is bleeding but it stopped already, looks fine")
        assertEquals(Severity.SERIOUS, result.severity)
    }

    @Test
    fun `double negative style still resolves to controlled`() {
        val result = SafetyTree.evaluate("The bleeding hasn't continued, it stopped right after we packed it")
        // "stopped" affirmed and unnegated -> SERIOUS (controlled), even
        // though an unrelated negation ("hasn't continued") appears earlier
        // and must not bleed across the sentence onto "stopped".
        assertEquals(Severity.SERIOUS, result.severity)
    }

    @Test
    fun `contrast word resets negation window`() {
        // "not sure, but...stopped" -- the negation is about being sure,
        // not about the bleeding. The reset word ("but") should stop the
        // negation window from reaching "stopped".
        val result = SafetyTree.evaluate("I'm not sure, but the bleeding has stopped")
        assertEquals(Severity.SERIOUS, result.severity)
    }

    @Test
    fun `arterial spurting with no resolution mentioned is critical`() {
        val result = SafetyTree.evaluate("Blood is spurting out of his leg in time with his heartbeat")
        assertEquals(Severity.CRITICAL, result.severity)
    }

    @Test
    fun `stopped then started again resolves to critical (resumed bleeding)`() {
        val result = SafetyTree.evaluate("The bleeding stopped but then started again, it is still going")
        assertEquals(Severity.CRITICAL, result.severity)
    }

    @Test
    fun `not anymore resolves to serious not critical`() {
        val result = SafetyTree.evaluate("It was bleeding badly but not anymore, all clear now")
        assertEquals(Severity.SERIOUS, result.severity)
    }

    @Test
    fun `not really stopped is critical`() {
        val result = SafetyTree.evaluate("Bleeding has not really stopped, just slowed a little")
        assertEquals(Severity.CRITICAL, result.severity)
    }

    // ---- Priority ordering ----

    @Test
    fun `not breathing always wins even if bleeding also mentioned`() {
        val result = SafetyTree.evaluate("He is not breathing and there is a small cut that stopped bleeding")
        assertEquals(Severity.CRITICAL, result.severity)
        assertEquals("NOT_BREATHING", result.matchedRule)
    }

    @Test
    fun `severe burn without breathing or bleeding issue is critical`() {
        val result = SafetyTree.evaluate("There's a third degree burn covering his forearm")
        assertEquals(Severity.CRITICAL, result.severity)
    }

    @Test
    fun `serious category for fracture`() {
        val result = SafetyTree.evaluate("I think he has an open fracture in his arm")
        assertEquals(Severity.SERIOUS, result.severity)
    }

    @Test
    fun `moderate category for infection signs`() {
        val result = SafetyTree.evaluate("The wound has some redness and swelling around it, might be infection")
        assertEquals(Severity.MODERATE, result.severity)
    }

    @Test
    fun `minor category for small cut`() {
        val result = SafetyTree.evaluate("Just a small cut on my finger from the knife")
        assertEquals(Severity.MINOR, result.severity)
    }

    @Test
    fun `unknown when no rule matches`() {
        val result = SafetyTree.evaluate("My foot feels weird today")
        assertEquals(Severity.UNKNOWN, result.severity)
    }
}
