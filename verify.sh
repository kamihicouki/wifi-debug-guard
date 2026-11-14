#!/bin/sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
test_output=$(mktemp -d)
trap 'rm -rf "$test_output"' EXIT HUP INT TERM
javac -d "$test_output" app/src/main/java/io/github/kamihicouki/wifidebugguard/NetworkPolicy.java app/src/main/java/io/github/kamihicouki/wifidebugguard/SetupPolicy.java tests/NetworkPolicyTest.java tests/SetupPolicyTest.java
java -cp "$test_output" io.github.kamihicouki.wifidebugguard.NetworkPolicyTest
java -cp "$test_output" io.github.kamihicouki.wifidebugguard.SetupPolicyTest
./gradlew --no-daemon :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:lintRelease
