package io.github.kamihicouki.wifidebugguard;
public class SetupPolicyTest {
 public static void main(String[] args) {
  check(false,false,false,false,false,false,SetupPolicy.Step.DISCLOSURE);
  check(true,false,false,false,false,false,SetupPolicy.Step.FINE_LOCATION);
  check(true,true,false,false,false,false,SetupPolicy.Step.BACKGROUND_LOCATION);
  check(true,true,true,false,false,false,SetupPolicy.Step.NOTIFICATIONS);
  check(true,true,true,true,false,false,SetupPolicy.Step.ADB_PERMISSION);
  check(true,true,true,true,true,false,SetupPolicy.Step.LOCATION_SETTING);
  check(true,true,true,true,true,true,SetupPolicy.Step.READY);
  System.out.println("7 setup policy checks passed");
 }
 static void check(boolean d,boolean f,boolean b,boolean n,boolean a,boolean l,SetupPolicy.Step expected) {
  if (SetupPolicy.next(d,f,b,n,a,l)!=expected) throw new AssertionError("setup step "+expected);
 }
}
