#!/bin/sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
mkdir -p tests/out
javac -d tests/out app/src/main/java/io/github/kamihicouki/wifidebugguard/NetworkPolicy.java tests/NetworkPolicyTest.java
java -cp tests/out io.github.kamihicouki.wifidebugguard.NetworkPolicyTest
./gradlew --no-daemon :app:assembleDebug :app:assembleRelease :app:lintRelease
