#!/usr/bin/env bash
# SessionStart hook. Prints a short context block to stdout — Claude sees it as part of the session.

set -euo pipefail

branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo '(no git)')"
status_lines="$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ' || echo '0')"

cat <<EOF
[session-start]
branch: $branch
uncommitted files: $status_lines
EOF
