#!/usr/bin/env python3
"""Patch Javadoc files to use custom package display name."""
import sys
from pathlib import Path

PACKAGE_NAME = "Infinite Mind Pictures"
# Custom label used when Javadoc emits "Unnamed Package"
UNNAMED_PACKAGE_LABEL = "infinite mind pictures bot"
DEFAULT_PACKAGE_TOKEN = "infinite.mind"

replacements = [
    (f'title="class in {DEFAULT_PACKAGE_TOKEN}"', f'title="class in {PACKAGE_NAME}"'),
    (f'class in {DEFAULT_PACKAGE_TOKEN}', f'class in {PACKAGE_NAME}'),
    (f'title="Package {DEFAULT_PACKAGE_TOKEN}"', f'title="{PACKAGE_NAME}"'),
    (f'Package {DEFAULT_PACKAGE_TOKEN}', PACKAGE_NAME),
    (f'<title>{DEFAULT_PACKAGE_TOKEN}</title>', f'<title>{PACKAGE_NAME}</title>'),
    (f'>{DEFAULT_PACKAGE_TOKEN}</a>', f'>{PACKAGE_NAME}</a>'),
    ("Unnamed Package", UNNAMED_PACKAGE_LABEL),
]

def patch_file(path: Path):
    text = path.read_text(encoding="utf-8")
    new_text = text
    for old, new in replacements:
        new_text = new_text.replace(old, new)
    if new_text != text:
        path.write_text(new_text, encoding="utf-8")
        return True
    return False


def main():
    if len(sys.argv) != 2:
        print("Usage: patch_javadoc.py <javadoc_directory>")
        sys.exit(1)
    root = Path(sys.argv[1])
    patched = 0
    for html_file in root.rglob('*.html'):
        if patch_file(html_file):
            patched += 1
    print(f"Patched {patched} files in {root}")

if __name__ == '__main__':
    main()
