package io.github.kamihicouki.wifidebugguard;
import android.content.*;
import android.util.Log;
import android.provider.Settings;
public final class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c, Intent i) {
  String action=i==null?null:i.getAction();
  if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
      && !Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)
      && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) return;
  SharedPreferences prefs=c.createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  if (!prefs.getBoolean("enabled",false)) return;
  if (prefs.getInt("disclosure_version",0)<1) {
   disableDebug(c);
   prefs.edit().putString("status","位置情報利用の説明と同意が必要です。アプリを開いて確認してください。デバッグOFFは端末設定で確認してください")
    .putLong("checked",System.currentTimeMillis()).apply();
   return;
  }
  Log.i("WifiDebugGuard", "boot event="+action);
  try { c.startForegroundService(new Intent(c,GuardService.class)); }
  catch (RuntimeException e) {
   Log.e("WifiDebugGuard","automatic service start failed",e);
   disableDebug(c);
   prefs.edit().putString("status","自動起動に失敗しました。位置情報と権限を確認しアプリから再開してください。デバッグOFFは端末設定で確認してください")
    .putLong("checked",System.currentTimeMillis()).apply();
  }
 }
 private void disableDebug(Context c) {
  try {
   if (c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")==android.content.pm.PackageManager.PERMISSION_GRANTED)
    Settings.Global.putInt(c.getContentResolver(),"adb_wifi_enabled",0);
  } catch (RuntimeException e) { Log.e("WifiDebugGuard","cannot disable after start failure",e); }
 }
}
