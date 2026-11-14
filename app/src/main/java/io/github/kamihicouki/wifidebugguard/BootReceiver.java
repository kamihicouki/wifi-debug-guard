package io.github.kamihicouki.wifidebugguard;
import android.content.*;
import android.util.Log;
public final class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c, Intent i) {
  Log.i("WifiDebugGuard", "boot event="+i.getAction());
  if (c.createDeviceProtectedStorageContext().getSharedPreferences("guard",0).getBoolean("enabled",false)) c.startForegroundService(new Intent(c,GuardService.class));
 }
}
