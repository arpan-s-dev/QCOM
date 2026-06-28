"""
Reference implementation of SafetyTree.kt / BleedingClassifier / NegationAwareMatcher,
ported 1:1 in logic, used to ACTUALLY RUN the negation proof tests since no Kotlin
compiler is available in this build sandbox. If you change SafetyTree.kt, update this
file to match and re-run, or treat any drift between the two as a bug.

Run: python3 scripts/verify_safety_tree.py
"""

from enum import Enum
import re


class Severity(Enum):
    CRITICAL = "CRITICAL"
    SERIOUS = "SERIOUS"
    MODERATE = "MODERATE"
    MINOR = "MINOR"
    UNKNOWN = "UNKNOWN"


class TriageResult:
    def __init__(self, severity, matched_rule, directive, raw_input):
        self.severity = severity
        self.matched_rule = matched_rule
        self.directive = directive
        self.raw_input = raw_input

    def __repr__(self):
        return f"TriageResult({self.severity}, rule={self.matched_rule})"


NEGATION_CUES = ["not", "n't", "no ", "never", "none", "without", "lacking", "unable to"]
RESET_WORDS = ["but", "however", "although", "though", ".", ","]
WINDOW_WORDS = 6


def is_negated(text: str, phrase_index: int) -> bool:
    before = text[:phrase_index]
    words = before.strip().split()
    window = " ".join(words[-WINDOW_WORDS:]) if words else ""

    last_negation_pos = -1
    last_reset_pos = -1
    for cue in NEGATION_CUES:
        pos = window.rfind(cue)
        if pos > last_negation_pos:
            last_negation_pos = pos
    for reset in RESET_WORDS:
        pos = window.rfind(reset)
        if pos > last_reset_pos:
            last_reset_pos = pos

    if last_negation_pos == -1:
        return False
    return last_negation_pos > last_reset_pos


ACTIVE_BLEED_PHRASES = ["bleeding", "blood", "spurting", "gushing", "hemorrhage", "wound is open"]
STOPPED_CUES = ["stopped", "controlled", "under control", "stable now", "slowed"]
ARTERIAL_CUES = ["spurting", "spurts", "pulsing", "pulses", "bright red", "gushing"]


def classify_bleeding(text_raw: str):
    text = text_raw.lower()
    mentions_bleeding = any(p in text for p in ACTIVE_BLEED_PHRASES)
    if not mentions_bleeding:
        return None

    is_arterial = any(c in text for c in ARTERIAL_CUES)

    # Clause-level re-escalation check: split on contrast/sequence words and
    # re-test the LAST clause for active-bleeding language. This catches
    # "stopped, but then started again" -- where "stopped" alone would
    # wrongly read as resolved, but a later clause re-escalates it.
    RESUME_CUES = ["started again", "started bleeding again", "still going",
                   "still bleeding", "began again", "opened back up", "reopened"]
    clauses = re.split(r"\b(?:but|then|however|although)\b|[,.;]", text)
    for clause in clauses[1:]:  # skip first clause, only check what follows a split
        if any(rc in clause for rc in RESUME_CUES):
            return Severity.CRITICAL

    any_stopped_cue_negated = False
    any_stopped_cue_affirmed = False

    for cue in STOPPED_CUES:
        search_from = 0
        while True:
            idx = text.find(cue, search_from)
            if idx == -1:
                break
            negated = is_negated(text, idx)
            if negated:
                any_stopped_cue_negated = True
            else:
                any_stopped_cue_affirmed = True
            search_from = idx + len(cue)

    if any_stopped_cue_negated:
        return Severity.CRITICAL
    if any_stopped_cue_affirmed:
        return Severity.SERIOUS
    if is_arterial:
        return Severity.CRITICAL
    return Severity.SERIOUS


NOT_BREATHING_PHRASES = [
    "not breathing", "isn't breathing", "stopped breathing", "no breathing",
    "not breathe", "can't breathe", "cannot breathe", "no pulse",
    "not responsive and not breathing"
]
SEVERE_BURN_PHRASES = [
    "third degree", "3rd degree", "third-degree", "charred", "white and leathery",
    "burn covers most", "large burn", "burn over 20", "circumferential burn"
]
SERIOUS_PHRASES = [
    "shock", "fracture", "broken bone", "compound fracture", "open fracture",
    "head injury", "concussion", "unconscious", "spine", "neck injury",
    "hypothermia", "frostbite", "second degree", "2nd degree"
]
MODERATE_PHRASES = [
    "infection", "red streak", "swelling", "sprain", "closed fracture", "mild burn", "rash"
]
MINOR_PHRASES = [
    "small cut", "scrape", "minor cut", "first degree", "1st degree", "splinter", "bruise"
]


def evaluate(input_text: str) -> TriageResult:
    text = input_text.lower()

    if any(p in text for p in NOT_BREATHING_PHRASES):
        return TriageResult(
            Severity.CRITICAL, "NOT_BREATHING",
            "Start CPR immediately. Call for help. Do not stop to treat other injuries first.",
            input_text
        )

    bleed_severity = classify_bleeding(text)
    if bleed_severity == Severity.CRITICAL:
        return TriageResult(
            Severity.CRITICAL, "ACTIVE_ARTERIAL_OR_UNCONTROLLED_BLEED",
            "Apply firm direct pressure or a tourniquet now. Do not wait for the bleeding to look worse before acting.",
            input_text
        )
    if bleed_severity == Severity.SERIOUS:
        return TriageResult(
            Severity.SERIOUS, "BLEEDING_CONTROLLED",
            "Bleeding is controlled. Watch for shock, keep the casualty warm, and monitor the dressing/tourniquet.",
            input_text
        )

    if any(p in text for p in SEVERE_BURN_PHRASES):
        return TriageResult(
            Severity.CRITICAL, "SEVERE_BURN",
            "Cover loosely with a clean dry cloth, treat for shock, and seek urgent evacuation. Do not apply ice or extensive cooling.",
            input_text
        )

    if any(p in text for p in SERIOUS_PHRASES):
        return TriageResult(
            Severity.SERIOUS, "SERIOUS_OTHER",
            "This needs careful stabilization and likely evacuation. Avoid unnecessary movement until assessed further.",
            input_text
        )

    if any(p in text for p in MODERATE_PHRASES):
        return TriageResult(
            Severity.MODERATE, "MODERATE_OTHER",
            "Clean and monitor. Watch for worsening signs over the next hours/days.",
            input_text
        )

    if any(p in text for p in MINOR_PHRASES):
        return TriageResult(
            Severity.MINOR, "MINOR_OTHER",
            "Basic first aid should be sufficient. Monitor for any change.",
            input_text
        )

    return TriageResult(
        Severity.UNKNOWN, "NO_MATCH",
        "I don't have enough detail to triage this confidently. Describe the injury, whether it's bleeding, and whether the person is breathing normally.",
        input_text
    )


# ----------------------------------------------------------------------
# Test cases -- mirrors SafetyTreeTest.kt exactly
# ----------------------------------------------------------------------

TESTS = [
    ("hasn't stopped -> CRITICAL",
     "The bleeding hasn't stopped, it's still pouring out", Severity.CRITICAL),

    ("has stopped now -> SERIOUS",
     "The bleeding has stopped now after I applied pressure", Severity.SERIOUS),

    ("has not stopped (full form) -> CRITICAL",
     "It has not stopped bleeding at all", Severity.CRITICAL),

    ("bleeding but it stopped already -> SERIOUS",
     "He is bleeding but it stopped already, looks fine", Severity.SERIOUS),

    ("unrelated negation earlier in sentence doesn't leak -> SERIOUS",
     "The bleeding hasn't continued, it stopped right after we packed it", Severity.SERIOUS),

    ("contrast word resets negation window -> SERIOUS",
     "I'm not sure, but the bleeding has stopped", Severity.SERIOUS),

    ("arterial spurting, no resolution mentioned -> CRITICAL",
     "Blood is spurting out of his leg in time with his heartbeat", Severity.CRITICAL),

    ("stopped then started again -> CRITICAL (resumed bleeding)",
     "The bleeding stopped but then started again, it is still going", Severity.CRITICAL),

    ("not anymore (resolved) -> SERIOUS",
     "It was bleeding badly but not anymore, all clear now", Severity.SERIOUS),

    ("not really stopped -> CRITICAL",
     "Bleeding has not really stopped, just slowed a little", Severity.CRITICAL),

    ("not breathing wins over controlled bleeding -> CRITICAL / NOT_BREATHING",
     "He is not breathing and there is a small cut that stopped bleeding", Severity.CRITICAL),

    ("severe burn alone -> CRITICAL",
     "There's a third degree burn covering his forearm", Severity.CRITICAL),

    ("open fracture -> SERIOUS",
     "I think he has an open fracture in his arm", Severity.SERIOUS),

    ("infection signs -> MODERATE",
     "The wound has some redness and swelling around it, might be infection", Severity.MODERATE),

    ("small cut -> MINOR",
     "Just a small cut on my finger from the knife", Severity.MINOR),

    ("no match -> UNKNOWN",
     "My foot feels weird today", Severity.UNKNOWN),
]

if __name__ == "__main__":
    passed = 0
    failed = 0
    for name, text, expected in TESTS:
        result = evaluate(text)
        ok = result.severity == expected
        status = "PASS" if ok else "FAIL"
        if ok:
            passed += 1
        else:
            failed += 1
        print(f"[{status}] {name}")
        print(f"       input:    {text!r}")
        print(f"       expected: {expected.value}  got: {result.severity.value}  rule: {result.matched_rule}")

    print(f"\n{passed} passed, {failed} failed out of {len(TESTS)}")
    if failed:
        raise SystemExit(1)
