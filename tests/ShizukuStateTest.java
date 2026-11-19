package io.github.kamihicouki.wifidebugguard;

public final class ShizukuStateTest {
 private static void check(boolean passed, String description) {
  if (!passed) throw new AssertionError(description);
 }

 public static void main(String[] args) {
  ShizukuState state = new ShizukuState();
  check(state.current() == ShizukuState.Value.UNCONFIRMED, "monitor starts unconfirmed");
  check(!state.died(), "death without confirmed running never triggers a notification");
  check(state.current() == ShizukuState.Value.UNCONFIRMED, "initial unavailability is unconfirmed");
  check(state.running(), "a live Binder arms detection");
  check(state.current() == ShizukuState.Value.RUNNING, "live Binder is running");
  check(!state.running(), "duplicate live observation adds no transition");
  check(state.died(), "first confirmed death triggers a notification");
  check(state.current() == ShizukuState.Value.STOPPED, "confirmed death is stopped");
  check(!state.died(), "duplicate death adds no notification");
  check(state.running(), "new live Binder re-arms detection");
  check(state.died(), "next distinct stop triggers one more notification");
  check(!new ShizukuState().died(), "new monitor does not inherit earlier running state");
  System.out.println("12 Shizuku state checks passed");
 }
}
