"""
Builds the offline first-aid corpus used by the RAG pipeline.

Content is organized by the TCCC MARCH framework:
  M - Massive Hemorrhage
  A - Airway
  R - Respiration
  C - Circulation
  H - Hypothermia / Head injury

Each chunk is:
  - short (1-4 sentences), self-contained, and answerable on its own
  - tagged with a stable id, MARCH category, severity hint, and a source tag
    (paraphrased from publicly known TCCC/Stop the Bleed/Red Cross first-aid
    consensus guidance -- NOT copied verbatim from any single text)
  - written so the grounded LLM prompt can cite the chunk id directly

Output: /home/claude/medic/corpus/first_aid_corpus.json
Also writes /home/claude/medic/corpus/first_aid_corpus.jsonl (one chunk per line,
convenient for the embedding step Person 1 will run).
"""

import json
import itertools

counter = itertools.count(1)

def chunk(category, subtopic, severity, text, source):
    return {
        "id": f"FA-{next(counter):04d}",
        "category": category,       # M / A / R / C / H
        "subtopic": subtopic,
        "severity": severity,        # CRITICAL / SERIOUS / MODERATE / MINOR / INFO
        "text": text.strip(),
        "source": source,
    }

chunks = []

# -----------------------------------------------------------------------
# M — MASSIVE HEMORRHAGE
# -----------------------------------------------------------------------
M = "MASSIVE_HEMORRHAGE"
S = "TCCC-MARCH/StopTheBleed-consensus"

chunks += [
    chunk(M, "arterial_bleed_recognition", "CRITICAL",
        "Arterial bleeding is bright red and typically spurts or pulses in time "
        "with the heartbeat. It can cause life-threatening blood loss within minutes "
        "and is the highest priority injury to treat.", S),
    chunk(M, "arterial_bleed_recognition", "CRITICAL",
        "Venous bleeding is darker red and flows steadily rather than spurting. "
        "It is serious but generally slower to cause life-threatening blood loss than "
        "arterial bleeding.", S),
    chunk(M, "tourniquet_application", "CRITICAL",
        "Apply a tourniquet 2-3 inches above the wound (toward the torso), never "
        "directly over a joint. Tighten until bleeding fully stops, not just slows.", S),
    chunk(M, "tourniquet_application", "CRITICAL",
        "After tightening a tourniquet, write the exact time of application on the "
        "tourniquet itself or on the casualty's skin/forehead. This time is critical "
        "for downstream medical decisions.", S),
    chunk(M, "tourniquet_application", "CRITICAL",
        "A properly applied tourniquet causes significant pain - this is expected and "
        "is not a sign it was applied wrong. Do not loosen it to relieve pain once "
        "bleeding has stopped.", S),
    chunk(M, "tourniquet_application", "CRITICAL",
        "If a single tourniquet does not fully stop arterial bleeding, apply a second "
        "tourniquet directly above (proximal to) the first one and tighten both.", S),
    chunk(M, "tourniquet_decision", "CRITICAL",
        "Use a tourniquet for any life-threatening limb bleeding where direct pressure "
        "has failed or is not practical (e.g. multiple casualties, ongoing danger). "
        "Do not delay tourniquet use to try gentler measures first when bleeding is severe.", S),
    chunk(M, "tourniquet_improvised", "SERIOUS",
        "An improvised tourniquet can be made from a folded triangular bandage, belt, "
        "or strip of cloth at least 2 inches wide, combined with a rigid stick or rod "
        "as a windlass to twist and tighten it. Avoid thin cord or wire, which can cut skin.", S),
    chunk(M, "wound_packing", "CRITICAL",
        "For deep wounds in areas a tourniquet cannot reach (groin, armpit, neck, torso), "
        "pack gauze or clean cloth directly into the wound cavity, pressing it firmly "
        "against the bleeding source, then hold continuous direct pressure for at least "
        "3 minutes.", S),
    chunk(M, "wound_packing", "CRITICAL",
        "When packing a wound, push the material as deep into the wound as the injury "
        "allows rather than just covering the surface - surface-only pressure will not "
        "control deep bleeding.", S),
    chunk(M, "wound_packing", "SERIOUS",
        "After wound packing controls bleeding, apply a pressure bandage over the packed "
        "wound to maintain compression and free up the rescuer's hands.", S),
    chunk(M, "direct_pressure", "SERIOUS",
        "For bleeding that is not life-threatening, firm continuous direct pressure with "
        "a clean cloth or gauze for several minutes is often enough to stop it. Do not "
        "repeatedly lift the cloth to check - this restarts clotting.", S),
    chunk(M, "hemostatic_agents", "INFO",
        "Hemostatic gauze (impregnated with a clotting agent) is packed into a wound the "
        "same way as plain gauze, then held with firm pressure; it is not a substitute "
        "for a tourniquet on a limb with massive arterial bleeding.", S),
    chunk(M, "junctional_bleeding", "CRITICAL",
        "Bleeding at a junctional area (groin, armpit, neck) cannot be controlled by a "
        "standard limb tourniquet. Use wound packing plus firm direct pressure, or a "
        "junctional tourniquet device if available.", S),
    chunk(M, "amputation", "CRITICAL",
        "For a traumatic amputation, apply a tourniquet above the amputation site "
        "immediately, even before assessing other injuries, due to the speed of blood loss.", S),
    chunk(M, "internal_bleeding_signs", "SERIOUS",
        "Suspect internal bleeding if there is a rigid, swelling, or bruised abdomen, "
        "coughing or vomiting blood, or signs of shock with no visible external wound. "
        "This cannot be treated with direct pressure and needs urgent evacuation.", S),
    chunk(M, "scalp_bleeding", "MODERATE",
        "Scalp wounds bleed heavily because of high blood supply to the area but are "
        "rarely life-threatening on their own. Apply firm direct pressure with gauze; "
        "watch for signs of an underlying skull or brain injury.", S),
    chunk(M, "nosebleed", "MINOR",
        "For a simple nosebleed, have the person lean slightly forward (not back) and "
        "pinch the soft part of the nose firmly for 10-15 minutes without checking early.", S),
]

# -----------------------------------------------------------------------
# A — AIRWAY
# -----------------------------------------------------------------------
A = "AIRWAY"
S = "TCCC-MARCH/AHA-consensus"

chunks += [
    chunk(A, "airway_assessment", "CRITICAL",
        "If a casualty is conscious and talking, their airway is open. If they are "
        "unconscious or making gurgling, snoring, or high-pitched sounds, the airway "
        "may be obstructed and needs immediate attention.", S),
    chunk(A, "unconscious_positioning", "CRITICAL",
        "An unconscious casualty who is breathing should be placed in the recovery "
        "position (on their side) so the tongue and any fluids do not block the airway.", S),
    chunk(A, "obstruction_choking_conscious", "CRITICAL",
        "For a conscious adult who is choking and cannot speak or cough, perform "
        "abdominal thrusts (Heimlich maneuver): stand behind them, fist above the navel, "
        "thrust sharply inward and upward until the object is expelled or they lose consciousness.", S),
    chunk(A, "obstruction_choking_unconscious", "CRITICAL",
        "If a choking person becomes unconscious, lower them to the ground and begin CPR "
        "chest compressions - compressions can help dislodge the obstruction even without rescue breaths.", S),
    chunk(A, "jaw_thrust", "CRITICAL",
        "For a casualty with a suspected neck or spine injury who needs an airway opened, "
        "use a jaw-thrust maneuver instead of tilting the head back - lift the jaw forward "
        "without moving the neck.", S),
    chunk(A, "foreign_body_visible", "SERIOUS",
        "Only attempt to remove a visible obstruction from the mouth with a finger sweep "
        "if you can clearly see and reach it; blind finger sweeps can push the object "
        "deeper.", S),
    chunk(A, "facial_trauma_airway", "CRITICAL",
        "Severe facial trauma with heavy bleeding into the mouth or throat can block the "
        "airway even in a conscious casualty - sit them forward if possible so blood "
        "drains out rather than down the airway.", S),
    chunk(A, "burns_airway", "CRITICAL",
        "Suspect airway burns/swelling if there is soot around the mouth/nose, singed "
        "facial hair, or a hoarse voice after a fire or blast - this airway can close "
        "progressively over the following minutes to hours and needs urgent evacuation.", S),
]

# -----------------------------------------------------------------------
# R — RESPIRATION
# -----------------------------------------------------------------------
R = "RESPIRATION"
S = "TCCC-MARCH/AHA-consensus"

chunks += [
    chunk(R, "breathing_check", "CRITICAL",
        "Check breathing by looking for chest rise, listening for breath sounds, and "
        "feeling for air for no more than 10 seconds. Absent or only gasping breaths "
        "means CPR should start immediately.", S),
    chunk(R, "cpr_adult", "CRITICAL",
        "For an adult in cardiac arrest, perform chest compressions at a rate of "
        "100-120 per minute, depth of about 2 inches (5 cm), allowing full chest "
        "recoil between compressions, with minimal interruptions.", S),
    chunk(R, "cpr_ratio", "CRITICAL",
        "Standard CPR ratio for a single rescuer with no airway equipment is 30 chest "
        "compressions to 2 rescue breaths, repeated continuously until help arrives or "
        "the person revives.", S),
    chunk(R, "open_chest_wound", "CRITICAL",
        "An open chest wound that sucks air with each breath (a 'sucking chest wound') "
        "should be covered with a chest seal or improvised airtight dressing taped on "
        "three sides, leaving one side unsealed to vent air out on exhale.", S),
    chunk(R, "tension_pneumothorax_signs", "CRITICAL",
        "Suspect a tension pneumothorax if there is increasing difficulty breathing, "
        "one side of the chest not rising normally, and worsening shock after a chest "
        "injury - this is a life-threatening emergency requiring urgent evacuation.", S),
    chunk(R, "flail_chest", "SERIOUS",
        "A flail chest (multiple rib fractures causing part of the chest wall to move "
        "opposite the rest during breathing) should be stabilized by having the casualty "
        "lie on the injured side if possible, which can ease breathing.", S),
    chunk(R, "rib_fracture", "MODERATE",
        "Suspected rib fractures without an open wound do not need splinting; encourage "
        "the casualty to take normal breaths despite pain, since shallow breathing can "
        "lead to pneumonia.", S),
    chunk(R, "asthma_attack", "SERIOUS",
        "For a severe asthma attack, help the person sit upright, use their rescue "
        "inhaler if they have one, and seek urgent help if breathing does not improve "
        "or lips/fingertips turn blue.", S),
    chunk(R, "blast_lung", "CRITICAL",
        "After an explosion, even casualties with no visible chest wound can develop "
        "'blast lung' (internal lung injury from the pressure wave) with delayed "
        "shortness of breath - monitor closely for hours afterward.", S),
]

# -----------------------------------------------------------------------
# C — CIRCULATION / SHOCK
# -----------------------------------------------------------------------
C = "CIRCULATION"
S = "TCCC-MARCH/StopTheBleed-consensus"

chunks += [
    chunk(C, "shock_recognition", "CRITICAL",
        "Signs of shock include pale or grey skin, rapid weak pulse, rapid shallow "
        "breathing, cold/sweaty skin, confusion, and weakness. Shock can develop even "
        "without visible heavy bleeding.", S),
    chunk(C, "shock_positioning", "SERIOUS",
        "Lay a casualty in shock flat and raise their legs about 12 inches if there is "
        "no suspected spine, pelvis, or leg fracture - this helps blood return to vital "
        "organs.", S),
    chunk(C, "shock_warmth", "SERIOUS",
        "Keep a casualty in shock warm with a blanket or jacket underneath and over "
        "them; shock makes the body lose heat faster, worsening the condition.", S),
    chunk(C, "pulse_check", "MODERATE",
        "A weak or absent radial (wrist) pulse with a present carotid (neck) pulse "
        "suggests significant blood loss and worsening shock.", S),
    chunk(C, "fracture_open", "SERIOUS",
        "For an open fracture (bone visible or piercing skin), do not push the bone back "
        "in. Control bleeding around it with gentle pressure avoiding the bone ends, "
        "then splint it in the position found.", S),
    chunk(C, "fracture_closed", "MODERATE",
        "For a closed fracture, splint the limb in the position it is found, immobilizing "
        "the joints above and below the break, and check that fingers/toes stay warm "
        "and pink after splinting.", S),
    chunk(C, "fracture_splinting", "MODERATE",
        "A splint can be improvised from a rigid item (stick, rolled magazine, trekking "
        "pole) padded and secured with cloth strips above and below the fracture site, "
        "not directly over it.", S),
    chunk(C, "pelvic_fracture", "CRITICAL",
        "Suspect a pelvic fracture after a fall or crush injury with pelvic pain, "
        "instability, or inability to bear weight - avoid rolling the casualty and "
        "minimize movement, as this injury can cause severe internal bleeding.", S),
    chunk(C, "spinal_injury_signs", "CRITICAL",
        "Suspect a spinal injury after a fall, diving, or high-speed impact if there is "
        "neck/back pain, numbness, tingling, or weakness in the limbs - keep the head "
        "and neck still and avoid moving the casualty unless airway/breathing requires it.", S),
    chunk(C, "crush_injury", "SERIOUS",
        "If a limb has been crushed under heavy weight for more than 15 minutes, releasing "
        "it suddenly can cause a dangerous surge of toxins into the bloodstream - if "
        "possible, prepare for this before release rather than after.", S),
    chunk(C, "burns_classification", "INFO",
        "First-degree burns are red and painful without blistering. Second-degree burns "
        "blister and are very painful. Third-degree burns look white, leathery, or charred "
        "and may be painless because nerve endings are destroyed - this is more serious, "
        "not less, despite less pain.", S),
    chunk(C, "burns_treatment_minor", "MINOR",
        "For first or small second-degree burns, cool the area with clean running water "
        "for 10-20 minutes. Do not use ice, butter, or toothpaste, and do not pop blisters.", S),
    chunk(C, "burns_treatment_severe", "CRITICAL",
        "For third-degree burns or burns covering more than 20% of the body, do not cool "
        "extensively (risk of hypothermia) - cover loosely with a clean dry cloth and "
        "treat for shock; this casualty needs urgent evacuation.", S),
    chunk(C, "burns_percentage_estimate", "INFO",
        "A quick way to estimate burn size is the 'rule of palm': the casualty's own "
        "palm (including fingers) is roughly 1% of their total body surface area.", S),
    chunk(C, "burns_circumferential", "CRITICAL",
        "A burn that completely encircles a limb, the chest, or the neck can cause "
        "dangerous swelling that cuts off blood flow or breathing as tissue swells - "
        "this needs urgent medical evaluation even if the burn itself seems stable.", S),
    chunk(C, "chemical_burns", "SERIOUS",
        "For chemical burns, brush off any dry chemical powder first, then flush the area "
        "with large amounts of clean water for at least 15-20 minutes, removing "
        "contaminated clothing.", S),
    chunk(C, "infection_signs", "MODERATE",
        "Watch any wound over the following days for increasing redness spreading "
        "outward, warmth, swelling, pus, foul odor, or fever - these are signs of "
        "infection needing medical attention, not just normal healing.", S),
    chunk(C, "infection_red_streaking", "SERIOUS",
        "Red streaking spreading from a wound toward the heart, combined with fever or "
        "feeling generally unwell, can indicate a spreading infection (lymphangitis or "
        "sepsis risk) and needs urgent medical care.", S),
    chunk(C, "wound_cleaning_minor", "MINOR",
        "For minor cuts and scrapes, rinse with clean water to remove debris, apply an "
        "antibiotic ointment if available, and cover with a clean dressing, changing it daily.", S),
    chunk(C, "blast_injury_pattern", "CRITICAL",
        "Blast injuries can cause four types of harm at once: pressure-wave injury to "
        "lungs/ears/gut, penetrating fragment wounds, blunt trauma from being thrown, "
        "and burns - assess for all four even if only one is obviously visible.", S),
    chunk(C, "eardrum_blast", "MODERATE",
        "Ear pain, ringing, or bleeding from the ear after an explosion can indicate a "
        "ruptured eardrum from the pressure wave; this alone is not life-threatening but "
        "signals the casualty was close to significant blast force.", S),
]

# -----------------------------------------------------------------------
# H — HYPOTHERMIA / HEAD INJURY
# -----------------------------------------------------------------------
H = "HYPOTHERMIA_HEAD"
S = "TCCC-MARCH/WMS-consensus"

chunks += [
    chunk(H, "hypothermia_signs", "SERIOUS",
        "Signs of hypothermia include uncontrolled shivering (which stops in severe "
        "cases), confusion, slurred speech, clumsiness, and slow breathing/pulse. "
        "Severe hypothermia with no shivering is a medical emergency.", S),
    chunk(H, "hypothermia_treatment", "SERIOUS",
        "Move the person out of wind/wet conditions, remove wet clothing, and insulate "
        "them from the ground. Warm the core (torso, neck, head, groin) before "
        "extremities, using body heat or warm (not hot) packs.", S),
    chunk(H, "hypothermia_handling", "CRITICAL",
        "Handle a severely hypothermic casualty very gently - rough movement or sudden "
        "exertion can trigger a fatal heart rhythm. Do not let them try to walk or exert "
        "themselves.", S),
    chunk(H, "hypothermia_rewarming_caution", "MODERATE",
        "Do not rewarm a hypothermic casualty by immersing them in hot water or applying "
        "direct high heat - rapid rewarming of the extremities can shock the system; warm gradually.", S),
    chunk(H, "frostbite", "MODERATE",
        "For frostbite, do not rub or massage the affected area. Rewarm gradually with "
        "warm (not hot) water around 100-105F (38-40C) once away from the cold, and avoid "
        "refreezing the area afterward.", S),
    chunk(H, "head_injury_signs", "CRITICAL",
        "After a head injury, watch for worsening headache, repeated vomiting, increasing "
        "confusion, unequal pupil size, seizures, or declining alertness - these suggest "
        "a serious brain injury needing urgent evacuation.", S),
    chunk(H, "concussion_mild", "MODERATE",
        "Mild concussion signs (brief confusion, headache, dizziness that improves) "
        "should still be monitored for at least 24 hours, as some serious head injuries "
        "worsen gradually after seeming mild at first.", S),
    chunk(H, "loss_of_consciousness", "CRITICAL",
        "Any loss of consciousness, even brief, after a head injury should be treated as "
        "potentially serious and the casualty should not be left alone or allowed to "
        "sleep deeply without being checked regularly.", S),
    chunk(H, "seizure_first_aid", "SERIOUS",
        "During a seizure, do not restrain the person or put anything in their mouth. "
        "Clear the area of hazards, cushion their head, and turn them onto their side "
        "once shaking stops to keep the airway clear.", S),
    chunk(H, "heat_exhaustion", "MODERATE",
        "Heat exhaustion presents with heavy sweating, weakness, nausea, and cool clammy "
        "skin. Move the person to shade, cool with water/fanning, and have them sip water "
        "if alert.", S),
    chunk(H, "heat_stroke", "CRITICAL",
        "Heat stroke presents with hot dry or flushed skin, confusion, and a body "
        "temperature that feels very hot - this is life-threatening. Cool aggressively "
        "with water/ice and evacuate urgently; do not wait for full recovery before "
        "seeking help.", S),
    chunk(H, "general_priority_reminder", "INFO",
        "Treatment priority always follows MARCH order: stop massive bleeding first, "
        "then secure the airway, support breathing, treat for shock/circulation, then "
        "manage hypothermia and head injury - do not skip ahead to a less urgent step "
        "while an earlier one is unaddressed.", S),
]

# -----------------------------------------------------------------------
# Scenario-phrased variants: the same facts above, re-expressed as the kind
# of short spoken/typed query a panicked user would actually use. This is
# NOT duplicate padding -- it materially helps retrieval recall, because
# raw users say "my friend got stabbed" not "penetrating trauma." Each
# variant is tagged back to its parent chunk id via "related_to".
# -----------------------------------------------------------------------

scenario_source = "TCCC-MARCH/StopTheBleed-consensus (scenario rephrasing)"

scenarios = [
    (M, "arterial_bleed_recognition", "CRITICAL",
     "If blood is spurting out in time with the pulse and is bright red, treat it as "
     "arterial bleeding right away - this is the single highest priority injury to act on.",
     "FA-0001"),
    (M, "tourniquet_application", "CRITICAL",
     "Don't worry about hurting them by tightening the tourniquet too much - a working "
     "tourniquet is supposed to hurt and to fully stop the bleeding, not just slow it.",
     "FA-0005"),
    (M, "tourniquet_application", "CRITICAL",
     "Once you've stopped the bleeding with a tourniquet, say the time out loud and write "
     "it down on the tourniquet or their skin - rescuers later will need to know exactly "
     "how long it's been on.", "FA-0004"),
    (M, "wound_packing", "CRITICAL",
     "If the wound is somewhere a tourniquet can't go, like the groin or armpit, stuff "
     "gauze or a clean cloth deep into the wound itself and press hard for a few minutes "
     "straight without letting go.", "FA-0009"),
    (M, "direct_pressure", "SERIOUS",
     "If the bleeding looks slow and steady rather than spurting, just press down hard "
     "with a clean cloth and hold it - don't keep lifting it to peek.", "FA-0012"),
    (M, "amputation", "CRITICAL",
     "If a limb has been severed, get a tourniquet on above the stump immediately - this "
     "comes before checking anything else.", "FA-0015"),
    (M, "internal_bleeding_signs", "SERIOUS",
     "If their belly is swelling, hard, or bruised and there's no cut to see, that can "
     "mean internal bleeding - pressing on the outside won't help, they need evacuation.",
     "FA-0016"),
    (A, "obstruction_choking_conscious", "CRITICAL",
     "If someone is choking and can't talk or cough at all, get behind them and do sharp "
     "inward-and-upward thrusts just above the belly button until it clears.", "FA-0021"),
    (A, "obstruction_choking_unconscious", "CRITICAL",
     "If a choking person passes out, lay them down and start chest compressions right "
     "away - the compressions themselves can help pop the object loose.", "FA-0022"),
    (A, "airway_assessment", "CRITICAL",
     "If they're making gurgling or snoring sounds and aren't fully awake, their airway "
     "might be blocked - this needs attention right now, not after other injuries.",
     "FA-0019"),
    (R, "breathing_check", "CRITICAL",
     "If you watch and listen for 10 seconds and there's no normal breathing, or just "
     "occasional gasping, start CPR immediately - don't wait longer to be sure.", "FA-0028"),
    (R, "cpr_adult", "CRITICAL",
     "Push hard and fast in the center of the chest, about 2 inches deep, around 100 to "
     "120 times a minute, letting the chest fully spring back up between pushes.", "FA-0029"),
    (R, "open_chest_wound", "CRITICAL",
     "If a chest wound is making a sucking or hissing sound with each breath, tape a "
     "plastic wrapper or dressing over it on three sides only, leaving one edge open so "
     "air can escape.", "FA-0031"),
    (R, "tension_pneumothorax_signs", "CRITICAL",
     "If breathing keeps getting harder, one side of the chest isn't rising right, and "
     "they're sliding into shock after a chest injury, that's a life-threatening "
     "emergency - get them evacuated urgently.", "FA-0032"),
    (C, "shock_recognition", "CRITICAL",
     "Pale, cold, sweaty skin with a fast weak pulse and confusion means shock - this can "
     "happen even without obvious heavy bleeding.", "FA-0040"),
    (C, "shock_positioning", "SERIOUS",
     "If there's no broken pelvis, leg, or spine injury, lay them flat and lift their legs "
     "about a foot to help blood get back to the vital organs.", "FA-0041"),
    (C, "fracture_open", "SERIOUS",
     "If you can see bone sticking out of a wound, do not push it back in - just control "
     "the bleeding around it gently and splint the limb as it lies.", "FA-0045"),
    (C, "burns_classification", "INFO",
     "A burn that's white, leathery, or charcoal-black and doesn't hurt much is actually "
     "worse than a painful blistered burn - the nerve endings have been destroyed.",
     "FA-0051"),
    (C, "burns_treatment_severe", "CRITICAL",
     "For a severe, deep, or large burn, don't keep cooling it for a long time - cover it "
     "loosely with a clean dry cloth and treat for shock instead, then get them out urgently.",
     "FA-0053"),
    (C, "burns_circumferential", "CRITICAL",
     "If a burn wraps all the way around an arm, leg, the chest, or the neck, the swelling "
     "afterward can cut off blood flow or breathing - this needs medical care even if it "
     "looks stable right now.", "FA-0055"),
    (C, "spinal_injury_signs", "CRITICAL",
     "After a fall or hard impact, if there's neck or back pain, numbness, or weakness in "
     "the arms or legs, keep their head and neck completely still and don't move them "
     "unless you have to.", "FA-0044"),
    (C, "blast_injury_pattern", "CRITICAL",
     "After an explosion, check for all four kinds of injury at once - blast pressure "
     "damage to lungs and ears, flying fragment wounds, blunt trauma from being thrown, "
     "and burns - even if only one is obvious at first.", "FA-0059"),
    (H, "hypothermia_handling", "CRITICAL",
     "If someone is severely cold and has stopped shivering, handle them very gently and "
     "don't let them try to walk or move on their own - rough movement can trigger a "
     "dangerous heart rhythm.", "FA-0063"),
    (H, "head_injury_signs", "CRITICAL",
     "After a head injury, if the headache keeps getting worse, they keep vomiting, their "
     "pupils look uneven, or they're getting more confused, that's a serious brain injury "
     "needing urgent evacuation.", "FA-0066"),
    (H, "heat_stroke", "CRITICAL",
     "If someone's skin is hot and dry (not sweaty) and they're confused after heat "
     "exposure, that's heat stroke - cool them aggressively with water or ice and get "
     "help urgently, don't wait for them to seem better first.", "FA-0071"),
]

for cat, sub, sev, text, related in scenarios:
    c = chunk(cat, sub, sev, text, scenario_source)
    c["related_to"] = related
    chunks.append(c)

print(f"Generated {len(chunks)} chunks")
by_cat = {}
for c in chunks:
    by_cat.setdefault(c["category"], 0)
    by_cat[c["category"]] += 1
print("By category:", by_cat)

with open("/home/claude/medic/corpus/first_aid_corpus.json", "w") as f:
    json.dump(chunks, f, indent=2)

with open("/home/claude/medic/corpus/first_aid_corpus.jsonl", "w") as f:
    for c in chunks:
        f.write(json.dumps(c) + "\n")

print("Wrote first_aid_corpus.json and .jsonl")
