#!/usr/bin/env bash
source ~/lodestar-venv/bin/activate
python -c "import transformers, timm, torchaudio; print('deps OK')"
