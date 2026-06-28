# STATUS.md — Live Dashboard

> Single source of truth for build state. Every agent updates this on finishing a task. Status values: `TODO` · `IN_PROGRESS` · `BLOCKED` · `DONE`.

## DE-RISK GATE
**Stock `.pte` running on NPU + QNN version pinned?  [ ] NO**
> Nothing custom ships until this is YES. (Person 1 flips it after the first model runs on the NPU and the version is recorded in DECISIONS.md.)

## Components
| Component | Owner | Status | Notes | Last updated |
|---|---|---|---|---|
| QNN env + stock `.pte` on NPU | P1 | TODO | THE GATE. Do first. | — |
| Llama 3.2 3B (Q4) on NPU | P1 | TODO | 1B fallback if <10 tok/s or throttling | — |
| Whisper-Base/Tiny on NPU | P1 | TODO | From AI Hub | — |
| `AiService` real impl (transcribe/generate) | P1 | TODO | Replaces StubAiService | — |
| Airplane-mode harness + NPU metrics | P1 | TODO | The 40% evidence | — |
| BGE-small embedder (for RAG) | P1 | TODO | NPU or CPU | — |
| Compose shell + status strip | P2 | TODO | Runs on StubAiService | — |
| `StubAiService` (canned) | P2 | TODO | Unblocks app before models | — |
| Medical first-aid corpus (TCCC/MARCH) | P2 | TODO | ~200–400 chunks | — |
| RAG retrieval + grounded prompt | P2 | TODO | top-k cosine, cite sources | — |
| Deterministic safety tree | P2 | TODO | Authoritative over LLM; test negation | — |
| Voice loop (mic→STT→RAG→LLM→TTS) | P2 | TODO | Android native TTS out | — |
| Solar compass (heading) | P2 | TODO | Verify math in Python first | — |
| GPS spoof detection + 3-tier fallback | P2 | TODO | Mock-location demo on real phone | — |
| Translation | P2 | TODO | Bonus — only if core solid | — |
| SOS card | P2 | TODO | Bonus — only if core solid | — |
| README + MIT license + diagram | P2 | TODO | Required for eligibility | — |

## Current Blockers
_(none yet — add here as they appear, with owner + what's needed)_
