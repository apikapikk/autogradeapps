#!/usr/bin/env bash
set -euo pipefail
mkdir -p submissions
for name in good bad broken; do cp -R "src/test/resources/fixtures/$name" "submissions/$name"; done
mvn -q compile exec:java -Dexec.args="--batch submissions --assignment payroll"
mvn -q compile exec:java -Dexec.args="--batch submissions"
rm -rf submissions/good submissions/bad submissions/broken
