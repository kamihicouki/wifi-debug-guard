package io.github.kamihicouki.wifidebugguard;
public final class BootPolicyTest {
 public static void main(String[] args) {
  // boot, replacement, automatic startup, current enabled, consent, target, expected
  boolean[][] cases={
   {true,false,true,false,true,true,true}, // reboot starts independently of current enabled
   {true,false,true,true,true,true,true},
   {true,false,false,true,true,true,false}, // disabling boot startup wins over current enabled
   {true,false,false,false,true,true,false},
   {false,true,false,true,true,true,true}, // update resumes current management, not boot preference
   {false,true,true,false,true,true,false},
   {false,true,true,true,true,true,true},
   {false,true,false,false,true,true,false},
   {true,false,true,true,false,true,false}, // no collection without explicit consent
   {true,false,true,true,true,false,false}, // no target: no automatic settings changes
   {false,true,true,true,false,true,false},
   {false,true,true,true,true,false,false},
   {false,false,true,true,true,true,false}, // unrelated broadcast cannot start management
   {false,false,false,false,true,true,false}
  };
  for(int n=0;n<cases.length;n++) {
   boolean[] c=cases[n];
   if(BootPolicy.shouldStart(c[0],c[1],c[2],c[3],c[4],c[5])!=c[6]) throw new AssertionError("boot transition "+n);
  }
  // Failure cleanup is reserved for an explicitly requested, configured guard.
  boolean[][] failures={
   {true,true,true},
   {true,false,false}, // a new installation must not touch manual debugging
   {false,true,false}, // persistent stop preserves manual debugging
   {false,false,false}
  };
  for(int n=0;n<failures.length;n++) {
   boolean[] f=failures[n];
   if(BootPolicy.shouldDisableOnFailure(f[0],f[1])!=f[2]) throw new AssertionError("boot failure cleanup "+n);
  }
  System.out.println((cases.length+failures.length)+" boot policy checks passed");
 }
}
