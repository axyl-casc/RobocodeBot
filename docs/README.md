# Documentation

This directory contains the generated API reference and additional project info.

* **API Reference:** Open [package-summary.html](script-dir/javadoc/infinite/mind/package-summary.html) for the Javadoc generated from the source code.
* **Project README:** See the [root README](../README.md) for instructions on building and running the bot.

The documentation is served via GitHub Pages from this folder.

## Regenerating docs

Run `scripts/generate_docs.sh` to rebuild the Javadoc and automatically patch
package names. The script also rewrites the default "Unnamed Package" label to
"infinite mind pictures bot". The output is placed in `docs/script-dir/javadoc`.
