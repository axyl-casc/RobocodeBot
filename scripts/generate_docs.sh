#!/bin/bash
set -e
OUTDIR="docs/script-dir/javadoc"
mkdir -p "$OUTDIR"
javadoc -d "$OUTDIR" -cp Bot/lib/* -sourcepath Bot infinite.mind
python3 "$(dirname "$0")/patch_javadoc.py" "$OUTDIR"
echo "Documentation generated in $OUTDIR"
