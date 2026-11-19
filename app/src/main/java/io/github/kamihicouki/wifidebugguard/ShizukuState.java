package io.github.kamihicouki.wifidebugguard;

final class ShizukuState {
 enum Value { UNCONFIRMED, RUNNING, STOPPED }

 private Value value = Value.UNCONFIRMED;

 Value current() {
  return value;
 }

 boolean running() {
  if (value == Value.RUNNING) return false;
  value = Value.RUNNING;
  return true;
 }

 boolean died() {
  if (value != Value.RUNNING) return false;
  value = Value.STOPPED;
  return true;
 }
}
