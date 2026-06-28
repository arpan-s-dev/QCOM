# LODESTAR — Claude Code Startup Prompt & Multi-Agent Tracking Setup

You're running **Cursor + Claude Code + Copilot** across **2 people** on one repo. The risk isn't code quality — it's agents overwriting each other and losing the thread. The fix below makes every agent **read shared tracking files before acting and write back after**, so all three stay in sync.

---

## HOW TO USE THIS

1. Open your repo folder in **Claude Code**.
2. Paste **PROMPT 1** (the bootstrap) first. It scaffolds the project + all tracking files.
3. From then on, every agent (Claude Code, Cursor, Copilot Chat) starts each session by reading `AGENTS.md` and `STATUS.md` (PROMPT 2 enforces this).
4. Person 1 and Person 2 each work their lanes; the tracking files keep you from colliding.

---
---

# PROMPT 1 — BOOTSTRAP (paste into Claude Code first, once)

```
You are setting up a hackathon mono-repo for "Lodestar" — an OFFLINE,
on-device AI survival copilot for the Qualcomm x Meta ExecuTorch Hackathon.
Target device: Samsung Galaxy S25 Ultra (Snapdragon 8 Elite, Hexagon NPU).
Runtime: ExecuTorch 1.0 + Qualcomm QNN backend. App: native Android / Kotlin /
Jetpack Compose. The app MUST run fully in airplane mode — ZERO cloud LLM calls
at runtime. Team: 2 people. Tooling: Cursor + Claude Code + GitHub Copilot.

== PART A: SCAFFOLD THE REPO ==
Create this structure (empty/stub files where noted, real config where you can):

/lodestar
  /android                # Jetpack Compose app
    /app/src/main/java/ai/lodestar/
      /ui                  # Compose screens + status strip
      /ai                  # AiService interface (transcribe/generate) + stubs
      /medical             # RAG + deterministic safety tree
      /nav                 # solar compass + spoof detection + position state
      /core                # airplane-mode + sensors + metrics
  /runtime                 # ExecuTorch/QNN export + quantization scripts (Python)
    /scripts
    /models                # .pte outputs (gitignored)
  /corpus                  # first-aid TCCC/MARCH passages + embeddings
  /docs                    # tracking + planning MD files (see PART B)
  README.md                # public-repo readme + MIT license
  .gitignore               # ignore /runtime/models, *.pte, large weights, venv

Add an AiService Kotlin interface with:
  suspend fun transcribe(audioPath: String): String
  suspend fun generate(prompt: String): String
and a StubAiService returning canned strings, so the app runs before models land.

== PART B: CREATE THE MULTI-AGENT TRACKING SYSTEM in /docs ==
Create these Markdown files with the templates I describe. These are the SHARED
SOURCE OF TRUTH that Cursor, Claude Code, and Copilot all read before acting and
update after. Use checkboxes and tables so any agent can parse state fast.

1) /docs/AGENTS.md  — the rules every agent follows. Contents:
   - Project one-liner + the "airplane mode / no cloud calls at runtime" rule.
   - The hard constraint list (pin QNN version; medical = reference/triage only;
     heading not GPS-grade lat/long; native Android not web).
   - WORKFLOW EVERY AGENT MUST FOLLOW:
       a. Read STATUS.md + the relevant TASKS file BEFORE writing code.
       b. Claim a task by setting its status to IN_PROGRESS + your name in TASKS.
       c. Do the work in the OWNER's directory only (see ownership map).
       d. After finishing: update STATUS.md, check the box in TASKS, append a
          one-line entry to CHANGELOG.md, and note any new gotcha in DECISIONS.md.
   - OWNERSHIP MAP:
       Person 1 -> /runtime, /android/.../ai, /android/.../core
       Person 2 -> /android/.../ui, /android/.../medical, /android/.../nav,
                   /corpus, README
   - FILE-LOCK ETIQUETTE: if a task is IN_PROGRESS by someone else, don't touch
     those files; pick another task.

2) /docs/STATUS.md  — live dashboard. A table:
   | Component | Owner | Status (TODO/IN_PROGRESS/BLOCKED/DONE) | Notes | Last updated |
   Seed rows for: QNN env+stock .pte, Llama 3B on NPU, Whisper on NPU,
   airplane-mode harness, NPU metrics, Compose shell+status strip, AiService stubs,
   medical corpus, RAG retrieval, safety tree, solar compass, spoof detection,
   translation, SOS card, README+license. Add a top line: "DE-RISK GATE: stock
   .pte running on NPU + QNN version pinned? [ ] — nothing custom ships until YES."

3) /docs/TASKS_PERSON1.md and /docs/TASKS_PERSON2.md  — ordered checklists.
   Use the steps below verbatim as checkboxes, each tagged [P1]/[P2], each with a
   status field. (Pull the exact steps from the lists I paste in PART C.)

4) /docs/DECISIONS.md  — running log of locked decisions + discovered gotchas.
   Seed it with: chosen models (Llama 3.2 3B Q4, Whisper-Base), the pinned QNN
   SDK version (PLACEHOLDER — Person 1 fills in), "night star plate-solve = v2
   roadmap, not built", "no cloud calls at runtime". New gotchas get appended
   with a date.

5) /docs/CHANGELOG.md  — append-only one-line-per-change log:
   `YYYY-MM-DD HH:MM | <agent/person> | <component> | <what changed>`

6) /docs/DEMO.md  — the airplane-mode run-of-show (5 steps) + anticipated judge
   questions + "what is real vs roadmap" so we never overclaim on stage.

== PART C: SEED THE TASK CHECKLISTS ==
[Paste PERSON 1 STEPS and PERSON 2 STEPS from the plan here when you run this, so
the agent fills TASKS_PERSON1.md and TASKS_PERSON2.md with real checkboxes.]

== PART D ==
Initialize git, write the .gitignore, make the first commit "scaffold + agent
tracking system". Then PRINT a summary of what you created and tell me the single
next action for Person 1 and for Person 2.

Do NOT write feature logic yet — only scaffold, stubs, config, and the tracking
files. Keep it minimal and runnable.
```

> When you run PROMPT 1, paste your Person 1 / Person 2 step lists (from the
> 2-person plan) into PART C so the task files get real checkboxes.

---
---

# PROMPT 2 — SESSION-START RULE (paste at the top of EVERY new agent session)

Put this at the start of every Claude Code / Cursor session so no agent acts blind:

```
Before doing anything: read /docs/AGENTS.md, /docs/STATUS.md, and my
/docs/TASKS_PERSON{N}.md. Tell me which task you're picking up and confirm no one
else has it IN_PROGRESS. Work ONLY in my owned directories per the ownership map.
When done: update STATUS.md, check the box in my TASKS file, append a line to
CHANGELOG.md, and add any new gotcha to DECISIONS.md. Never add code that makes a
network/cloud LLM call at runtime — this app must work in airplane mode.
```

---

# PROMPT 3 — COPILOT RULE (so Copilot stays in sync too)

Copilot Chat doesn't read your repo conventions automatically. Add a
`.github/copilot-instructions.md` so Copilot follows the same rules — paste this
into Claude Code:

```
Create /.github/copilot-instructions.md telling GitHub Copilot:
- This is Lodestar, an OFFLINE on-device Android app (ExecuTorch + QNN, Snapdragon
  NPU). Never suggest code that calls a cloud LLM/API at runtime — must work in
  airplane mode.
- Before suggesting changes, assume /docs/STATUS.md and /docs/TASKS_*.md are the
  source of truth; keep suggestions within the current task's directory.
- Medical features are first-aid REFERENCE/TRIAGE only, never diagnosis; keep the
  deterministic safety tree authoritative over the LLM.
- Prefer Kotlin/Compose idioms; coordinates in monospace; respect the existing
  AiService interface.
- We are pursuing the GitHub Copilot-Powered Build Award — keep Copilot usage
  visible and meaningful.
```

---

## WHY THIS WORKS (the short version)

- **One source of truth.** `STATUS.md` + `TASKS_*.md` mean every agent and both
  humans see the same state. No "wait, did you already do the Whisper wrapper?"
- **No collisions.** The ownership map + IN_PROGRESS locks keep Cursor and Claude
  Code out of each other's files.
- **No drift.** `DECISIONS.md` captures the pinned QNN version and locked model
  choices so a fresh agent session doesn't re-litigate them or pick a different model.
- **Demo safety.** `DEMO.md` records what's real vs roadmap, so you never
  overclaim when tired at hour 11.
- **Copilot Award.** `.github/copilot-instructions.md` keeps Copilot aligned and
  documents your use of it for the GitHub judges.

## THE FIRST TWO ACTIONS AFTER BOOTSTRAP
- **Person 1:** run prompt P1.1 — get a stock `.pte` on the NPU and **fill the
  pinned QNN SDK version into `/docs/DECISIONS.md`**. Flip the DE-RISK GATE to YES.
- **Person 2:** run prompt P2.1 — Compose shell + status strip against the
  `StubAiService`, so the app is runnable while Person 1 wrangles the chip.
