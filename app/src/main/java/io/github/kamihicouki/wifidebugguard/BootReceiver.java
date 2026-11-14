package io.github.kamihicouki.wifidebugguard;
import android.content.*;
import android.util.Log;
public final class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c, Intent i) {
  String action=i==null?null:i.getAction();
  if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
      && !Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)
      && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) return;
  SharedPreferences prefs=c.createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  if (!prefs.getBoolean("enabled",false)) return;
  Log.i("WifiDebugGuard", "boot event="+action);
  try { c.startForegroundService(new Intent(c,GuardService.class)); }
  catch (RuntimeException e) {
   Log.e("WifiDebugGuard","automatic service start failed",e);
   prefs.edit().putString("status","自動起動に失敗しました。アプリから再開し、端末のデバッグ設定を確認してください")
    .putLong("checked",System.currentTimeMillis()).apply();
  }
 }
}
