# Lodestar Runtime (Person 1)

ExecuTorch 1.0 + Qualcomm QNN backend. All model export and device push scripts live here.

**Target device:** Galaxy S25 Ultra (SM8750 / Snapdragon 8 Elite)  
**Build host:** Ubuntu 22.04 on WSL2 (verified by ExecuTorch docs)

## Quick start

```bash
# 1. One-time WSL setup (apt packages, Android SDK/NDK 26c, ExecuTorch clone)
bash runtime/scripts/setup_wsl.sh

# 2. Download QNN SDK manually (requires Qualcomm account):
#    https://developer.qualcomm.com/software/qualcomm-ai-engine-direct-sdk
#    Recommend QNN 2.37.0 for stability (ExecuTorch 1.0 verified).
#    Unzip to e.g. ~/qnn/2.37.0/

# 3. Copy env template and fill in paths
cp runtime/env.template runtime/.env
# Edit runtime/.env — set QNN_SDK_ROOT, verify ANDROID_NDK_ROOT

# 4. Source env in every new shell
source runtime/scripts/envsetup.sh

# 5. Verify everything before building
bash runtime/scripts/verify_env.sh
```

## Phase 0 checklist (DE-RISK GATE)

| Step | Script / command | Done? |
|------|------------------|-------|
| P1.0a | `setup_wsl.sh` + QNN SDK download | |
| P1.0b | `source runtime/scripts/envsetup.sh` | |
| P1.0c | `bash runtime/scripts/export_deeplab.sh` | |
| P1.0d | `bash runtime/scripts/run_deeplab_device.sh` | |
| P1.0e | Flip DE-RISK GATE in STATUS.md | |

## Directory layout

```
runtime/
├── README.md
├── env.template          # copy → .env (gitignored)
├── scripts/
│   ├── setup_wsl.sh      # one-time host setup
│   ├── envsetup.sh       # source every session
│   ├── verify_env.sh     # preflight checks
│   ├── build_executorch.sh
│   ├── export_deeplab.sh # stock .pte for gate
│   ├── push_qnn_libs.sh  # adb push HTP skel/stub
│   └── run_deeplab_device.sh
└── models/               # exported .pte artifacts (gitignored)
```

## Notes

- **Never** commit `runtime/.env`, QNN SDK, or `.pte` binaries.
- Pin QNN version in root `DECISIONS.md` after first successful device run.
- S25 Ultra uses HTP V79 stubs (`libQnnHtpV79Stub.so` / `libQnnHtpV79Skel.so`).
