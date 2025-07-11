# RobocodeBot AGENTS Guide

This file provides guidance for OpenAI Codex and other AI agents when working with this repository.

## Repository Structure

- `/Bot` - Source for the Robocode bot
  - `/infinite/mind` - Java classes implementing the bot logic
  - `*.cmd` and `*.bat` - Windows scripts to compile or run the bot
  - `/lib` - JAR dependencies (do not modify)
- `/docs` - Generated Javadoc and web assets
- `/robocode_docs` - PDF copies of the Tank Royale Bot API docs
- `/scripts` - Utility scripts such as `generate_docs.sh` and `patch_javadoc.py`
- `/.vscode` - Editor tasks and settings

## Coding Conventions

- Use Java 11 or newer when adding code
- Indent Java code with four spaces
- Keep method and variable names descriptive
- Add comments for any non‑obvious logic

## Development Tasks

- Compile the bot with `javac -cp "Bot/lib/*" Bot/infinite/mind/*.java`
- On Windows, you can use `compile.bat` or `build.bat`
- Generate documentation with `scripts/generate_docs.sh`

## Pull Requests

When creating a pull request:
1. Provide a clear description of the changes
2. Reference relevant issues if applicable
3. Ensure the project builds without errors
4. Keep the PR focused on a single concern

