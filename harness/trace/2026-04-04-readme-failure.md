# Trace: README.md Modification Failures
**Date**: 2026-04-04
**Category**: Tooling failure / File Encoding Issues

## Failure
When attempting to use `replace_file_content` to add a new section (Harness Engineering) to `README.md`, the tool repeatedly failed with:
`chunk 0: target content not found in file`.

## Root Cause
- `README.md` may contain multi-byte characters (Chinese/Unicode markers) or unusual line breaks that caused target content matching to fail.
- Complex Markdown blocks (like tables and Mermaid) within the target string might have been misinterpreted.

## Resolution
Used a Python script (`scripts/update_readme.py`) to read the file, identify the marker (`# 更新日志`), and perform a simple string replacement.

## Prevention for Next Agent
- If `replace_file_content` fails on complex files with multi-byte characters, use a temporary one-off script to perform a more robust manipulation.
