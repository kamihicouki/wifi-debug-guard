package io.github.kamihicouki.wifidebugguard;

import rikka.shizuku.ShizukuProvider;

public final class GuardShizukuProvider extends ShizukuProvider {
 static {
  ShizukuProvider.disableAutomaticSuiInitialization();
 }
}
