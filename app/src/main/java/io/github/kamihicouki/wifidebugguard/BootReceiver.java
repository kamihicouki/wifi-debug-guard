package io.github.kamihicouki.wifidebugguard;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.provider.Settings;
public final class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i) {
  String action=i==null?null:i.getAction();
  boolean boot=Intent.ACTION_BOOT_COMPLETED.equals(action)||Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action);
  boolean replacement=Intent.ACTION_MY_PACKAGE_REPLACED.equals(action);
  if(!boot && !replacement) return;
  SharedPreferences p=c.createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  boolean enabled=p.getBoolean("enabled",false),auto=p.getBoolean("auto_start_on_boot",enabled);
  boolean requested=boot?auto:enabled;
  if(!requested) return;
  boolean consent=p.getInt("disclosure_version",0)>=1;
  boolean configured=NetworkPolicy.validIdentity(p.getString("ssid",null),p.getString("bssid",null));
  if(!BootPolicy.shouldStart(boot,replacement,auto,enabled,consent,configured)) {
   recordFailure(c,p,requested,configured,!consent?"自動起動待機：位置情報利用の説明と同意が必要です":"自動起動待機：対象Wi-Fiを登録してください");return;
  }
  // Boot is an FGS start exemption, not an exemption from location prerequisites.
  try {
   if(!granted(c,"android.permission.WRITE_SECURE_SETTINGS")) {recordFailure(c,p,requested,configured,"自動起動待機：ADB設定権限が必要です");return;}
   if(!granted(c,"android.permission.ACCESS_FINE_LOCATION")||!granted(c,"android.permission.ACCESS_BACKGROUND_LOCATION")) {recordFailure(c,p,requested,configured,"自動起動待機：位置情報を正確・常に許可にしてください");return;}
   if(Build.VERSION.SDK_INT>=33 && !granted(c,"android.permission.POST_NOTIFICATIONS")) {recordFailure(c,p,requested,configured,"自動起動待機：通知権限が必要です");return;}
   if(!c.getSystemService(android.location.LocationManager.class).isLocationEnabled()) {recordFailure(c,p,requested,configured,"自動起動待機：位置情報設定をONにしてアプリから再開してください");return;}
   p.edit().putBoolean("enabled",true).putString("last_boot_request_action",action).putLong("last_boot_request_at",System.currentTimeMillis()).apply();
   Log.i("WifiDebugGuard","automatic start requested action="+action);
   c.startForegroundService(new Intent(c,GuardService.class));
  } catch(RuntimeException e) {
   Log.e("WifiDebugGuard","automatic service start failed",e);
   recordFailure(c,p,requested,configured,"自動起動に失敗："+e.getClass().getSimpleName()+"。OS制限・権限を確認してアプリから再開してください。デバッグ状態は端末設定で確認してください");
  }
 }
 private static boolean granted(Context c,String p) {return c.checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED;}
 /** Never read Wi-Fi identity here: failure handling must also work before consent. */
 private static void recordFailure(Context c,SharedPreferences p,boolean requested,boolean configured,String status) {
  if(BootPolicy.shouldDisableOnFailure(requested,configured)) {
   try {
    if(!granted(c,"android.permission.WRITE_SECURE_SETTINGS")) status+="。デバッグOFFを要求できません（ADB設定権限なし）。端末設定でOFFを確認してください";
    else if(Settings.Global.putInt(c.getContentResolver(),"adb_wifi_enabled",0)) status+="。デバッグOFFを要求しました。現在の設定は端末で確認してください";
    else status+="。デバッグOFF要求に失敗しました。端末設定でOFFにしてください";
   } catch(RuntimeException e) {
    Log.e("WifiDebugGuard","cannot disable after automatic start failure",e);
    status+="。デバッグOFF要求に失敗："+e.getClass().getSimpleName()+"。端末設定でOFFにしてください";
   }
  }
  p.edit().putString("status",status).putLong("checked",System.currentTimeMillis()).apply();
 }
}
