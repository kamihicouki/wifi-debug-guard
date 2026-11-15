package io.github.kamihicouki.wifidebugguard;
public final class BootPolicy {
 public static boolean shouldStart(boolean boot,boolean replacement,boolean autoStart,boolean enabled,boolean consent,boolean configured) { return consent && configured && ((boot && autoStart) || (replacement && enabled)); }
 /** Failed automatic startup may disable debugging only for an existing configured guard request. */
 public static boolean shouldDisableOnFailure(boolean requested,boolean configured) { return requested && configured; }
}
