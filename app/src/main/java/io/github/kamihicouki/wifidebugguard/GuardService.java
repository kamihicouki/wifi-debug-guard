package io.github.kamihicouki.wifidebugguard;
import android.app.*;
import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
public final class GuardService extends Service {
 private final Handler handler = new Handler(Looper.getMainLooper());
 private SharedPreferences prefs;
 private String lastStatus = "";
 private boolean destroyed, startupFailed, foregroundStarted, receiverRegistered, callbackRegistered;
 private final Runnable poll = new Runnable() { public void run() { if (evaluate()) handler.postDelayed(this,15000); } };
 private final BroadcastReceiver receiver = new BroadcastReceiver() { public void onReceive(Context c, Intent i) { evaluate(); } };
 private final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
  public void onAvailable(Network n) { handler.post(() -> evaluate()); }
  public void onLost(Network n) { handler.post(() -> evaluate()); }
  public void onCapabilitiesChanged(Network n, NetworkCapabilities caps) { handler.post(() -> evaluate()); }
 };
 @Override public void onCreate() {
  super.onCreate(); prefs = createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  NotificationManager nm = getSystemService(NotificationManager.class);
  nm.createNotificationChannel(new NotificationChannel("guard","指定Wi-Fiのデバッグ自動管理",NotificationManager.IMPORTANCE_LOW));
  if (!prefs.getBoolean("enabled",false)) { stopSelf(); return; }
  if (prefs.getInt("disclosure_version",0)<1) {
   startupFailed=true;
   disableAfterFailure();
   updateStatus("位置情報利用の説明と同意が必要です。アプリを開いて確認してください。デバッグOFFは端末設定で確認してください");
   stopSelf(); return;
  }
  try {
   // Wi-Fi identifiers are location-sensitive data. Do not claim an unrelated
   // FGS type to evade location/while-in-use permission requirements.
   if (!hasLocationPermissions() || !getSystemService(android.location.LocationManager.class).isLocationEnabled()) {
    throw new SecurityException("location prerequisites unavailable");
   }
   startForeground(1, notification("接続先を確認しています"),android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
   foregroundStarted=true;
   IntentFilter f = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
   f.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); f.addAction(LocationManagerAction);
   // These are protected system broadcasts; the system-only receiver exemption
   // applies to the explicit export-flag requirement on Android 14+.
   registerReceiver(receiver,f);
   receiverRegistered=true;
   getSystemService(ConnectivityManager.class).registerNetworkCallback(new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(), callback);
   callbackRegistered=true;
   handler.post(poll);
  } catch (RuntimeException e) {
   startupFailed=true;
   Log.e("WifiDebugGuard","service initialization failed",e);
   disableAfterFailure();
   updateStatus("常駐を開始できません。位置情報ON・正確/常に許可を確認してアプリから再開してください。デバッグOFFは端末設定で確認してください");
   stopSelf();
  }
 }
 private static final String LocationManagerAction="android.location.MODE_CHANGED";
 private Notification notification(String state) {
  PendingIntent pi=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
  return new Notification.Builder(this,"guard").setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).setContentTitle("Wi-Fi Debug Guard").setContentText(state).setContentIntent(pi).setOngoing(true).build();
 }
 @SuppressWarnings("deprecation") private boolean evaluate() {
  // A queued callback or a sticky restart must not revive stopped management,
  // nor replace the Activity's warning that disabling debugging failed.
  if (destroyed || startupFailed) return false;
  if (!prefs.getBoolean("enabled",false)) {
   handler.removeCallbacksAndMessages(null);
   stopSelf();
   return false;
  }
  if (prefs.getInt("disclosure_version",0)<1) {
   disableAfterFailure();
   updateStatus("位置情報利用の説明と同意が必要です。アプリを開いて確認してください。デバッグOFFは端末設定で確認してください");
   handler.removeCallbacksAndMessages(null);
   stopSelf(); return false;
  }
  try {
   boolean locationPermission=hasLocationPermissions();
   WifiManager wm = getSystemService(WifiManager.class);
   WifiInfo info = locationPermission ? wm.getConnectionInfo() : null;
   ConnectivityManager cm=getSystemService(ConnectivityManager.class);
   NetworkInfo ni=cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
   boolean connected=locationPermission && getSystemService(android.location.LocationManager.class).isLocationEnabled() && ni!=null && ni.isConnected() && info!=null && info.getNetworkId()!=-1;
   boolean enabled=prefs.getBoolean("enabled",false);
   boolean allowed=NetworkPolicy.allowed(enabled,connected,info==null?null:info.getSSID(),info==null?null:info.getBSSID(),prefs.getString("ssid",null),prefs.getString("bssid",null));
   boolean permission=checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")==android.content.pm.PackageManager.PERMISSION_GRANTED;
   int desired=allowed?1:0;
   int current=Settings.Global.getInt(getContentResolver(),"adb_wifi_enabled",0);
   if (permission && current!=desired) {
    if (!Settings.Global.putInt(getContentResolver(),"adb_wifi_enabled",desired)) throw new IllegalStateException("setting rejected");
    Log.i("WifiDebugGuard","set adb_wifi_enabled="+desired);
   }
   current=Settings.Global.getInt(getContentResolver(),"adb_wifi_enabled",0);
   String status=!permission?"設定権限不足：ON/OFFを制御できません":!locationPermission?(current==0?"位置情報権限不足（正確・常に許可）：設定値OFF":"位置情報権限不足（正確・常に許可）：OFF要求中、端末設定を確認"):allowed?(current==1?"指定Wi-Fi：設定値ON（ADB接続未確認）":"指定Wi-Fi：有効化要求中（端末の許可を確認）"):current==0?"指定Wi-Fi以外・停止中・接続先不明：OFF":"デバッグOFF要求中（設定を確認）";
   updateStatus(status);
  } catch (RuntimeException e) {
   Log.e("WifiDebugGuard","evaluation failed",e);
   disableAfterFailure();
   updateStatus("エラー："+e.getClass().getSimpleName()+"。デバッグOFFは未確認です。端末設定を確認してください");
  }
  return true;
 }
 private boolean hasLocationPermissions() {
  return checkSelfPermission("android.permission.ACCESS_FINE_LOCATION")==android.content.pm.PackageManager.PERMISSION_GRANTED
   && checkSelfPermission("android.permission.ACCESS_BACKGROUND_LOCATION")==android.content.pm.PackageManager.PERMISSION_GRANTED;
 }
 private void disableAfterFailure() {
  try { if (checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")==android.content.pm.PackageManager.PERMISSION_GRANTED) Settings.Global.putInt(getContentResolver(),"adb_wifi_enabled",0); }
  catch (RuntimeException e) { Log.e("WifiDebugGuard","cannot disable after failure",e); }
 }
 private void updateStatus(String status) {
  prefs.edit().putString("status",status).putLong("checked",System.currentTimeMillis()).apply();
  if (!status.equals(lastStatus)) {
   lastStatus=status;
   Log.i("WifiDebugGuard",status);
   try { if (foregroundStarted) getSystemService(NotificationManager.class).notify(1,notification(status)); }
   catch (RuntimeException e) { Log.e("WifiDebugGuard","notification update failed",e); }
  }
 }
 @Override public int onStartCommand(Intent i,int flags,int id) { return evaluate()?START_STICKY:START_NOT_STICKY; }
 @Override public IBinder onBind(Intent i) { return null; }
 @Override public void onDestroy() { destroyed=true; handler.removeCallbacksAndMessages(null); if (receiverRegistered) unregisterReceiver(receiver); if (callbackRegistered) getSystemService(ConnectivityManager.class).unregisterNetworkCallback(callback); super.onDestroy(); }
}
