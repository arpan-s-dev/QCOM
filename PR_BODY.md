## Summary

- prepares the Lodestar hackathon submission branch for review
- ships an offline-first Android app for triage, heading recovery, and communication in airplane mode
- includes demo-facing docs that are explicit about what is live today versus what remains roadmap

## What is in this branch

- **TREAT**: deterministic SafetyTree severity, offline corpus grounding, field-kit reference
- **ORIENT**: solar heading, night-sky photo heading, spoof-aware position trust states
- **ORIENT**: nearest San Francisco hospitals from bundled offline JSON
- **COMMUNICATE**: translation and SOS surfaces, with AI backend swappable through `AiService`
- submission docs: README, DEMO script, pitch outline, PR body, and project status cleanup
- field-instrument UI polish with Compose motion *(merge from APP-UI branch when ready)*

## Demo truth

- runs in **airplane mode** with no cloud dependency at runtime
- de-risk gate passed on device with stock DeepLab `.pte` on QNN
- deterministic medical severity is live today
- `RealAiService` / on-device LLM-ASR integration should only be claimed if it has landed on `demo/final` before merge

## Known limitations

- current medical corpus is curated but still small (`93` chunks)
- spoof detection is currently a speed-gate heuristic, not a full sensor-fusion stack
- hospitals are offline **San Francisco-only** JSON for the demo
- navigation output is heading guidance, not turn-by-turn routing

## Notes for reviewers

- this is a hackathon prototype, not a medical device
- safety labels come from deterministic logic, not from the language model
- MIT licensed
