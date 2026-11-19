#!/bin/sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
test_output=$(mktemp -d)
trap 'rm -rf "$test_output"' EXIT HUP INT TERM
javac -d "$test_output" app/src/main/java/io/github/kamihicouki/wifidebugguard/NetworkPolicy.java app/src/main/java/io/github/kamihicouki/wifidebugguard/SetupPolicy.java app/src/main/java/io/github/kamihicouki/wifidebugguard/BootPolicy.java app/src/main/java/io/github/kamihicouki/wifidebugguard/DashboardState.java app/src/main/java/io/github/kamihicouki/wifidebugguard/ShizukuState.java tests/ShizukuStateTest.java tests/NetworkPolicyTest.java tests/SetupPolicyTest.java tests/BootPolicyTest.java tests/DashboardStateTest.java
java -cp "$test_output" io.github.kamihicouki.wifidebugguard.NetworkPolicyTest
java -cp "$test_output" io.github.kamihicouki.wifidebugguard.SetupPolicyTest
java -cp "$test_output" io.github.kamihicouki.wifidebugguard.BootPolicyTest
java -cp "$test_output" io.github.kamihicouki.wifidebugguard.DashboardStateTest
java -cp "$test_output" io.github.kamihicouki.wifidebugguard.ShizukuStateTest
./gradlew --no-daemon :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:lintRelease
