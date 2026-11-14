package io.github.kamihicouki.wifidebugguard;
public final class SetupPolicy {
 public enum Step { DISCLOSURE, FINE_LOCATION, BACKGROUND_LOCATION, NOTIFICATIONS, ADB_PERMISSION, LOCATION_SETTING, READY }
 public static Step next(boolean disclosure,boolean fine,boolean background,boolean notifications,boolean adb,boolean location) { if (!disclosure) return Step.DISCLOSURE;
  if (!fine) return Step.FINE_LOCATION;
  if (!background) return Step.BACKGROUND_LOCATION;
  if (!notifications) return Step.NOTIFICATIONS;
  if (!adb) return Step.ADB_PERMISSION;
  if (!location) return Step.LOCATION_SETTING;
  return Step.READY; }
}
