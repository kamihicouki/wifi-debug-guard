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
 private final Runnable poll = new Runnable() { public void run() { evaluate(); handler.postDelayed(this,15000); } };
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
  startForeground(1, notification("接続先を確認しています"));
  IntentFilter f = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
  f.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); f.addAction(LocationManagerAction);
  registerReceiver(receiver,f);
  getSystemService(ConnectivityManager.class).registerNetworkCallback(new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(), callback);
  handler.post(poll);
 }
 private static final String LocationManagerAction="android.location.MODE_CHANGED";
 private Notification notification(String state) {
  PendingIntent pi=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
  return new Notification.Builder(this,"guard").setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).setContentTitle("Wi-Fi Debug Guard").setContentText(state).setContentIntent(pi).setOngoing(true).build();
 }
 @SuppressWarnings("deprecation") private void evaluate() {
  try {
   WifiManager wm = getSystemService(WifiManager.class);
   WifiInfo info = wm.getConnectionInfo();
   ConnectivityManager cm=getSystemService(ConnectivityManager.class);
   NetworkInfo ni=cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
   boolean connected=getSystemService(android.location.LocationManager.class).isLocationEnabled() && ni!=null && ni.isConnected() && info!=null && info.getNetworkId()!=-1;
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
   String status=!permission?"設定権限が必要です":allowed?(current==1?"指定Wi-Fi：ワイヤレスデバッグ ON":"指定Wi-Fi：有効化要求中（端末の許可を確認）"):current==0?"指定Wi-Fi以外・停止中・接続先不明：OFF":"デバッグOFF要求中（設定を確認）";
   if (!status.equals(lastStatus)) {
    Log.i("WifiDebugGuard",status+" connected="+connected+" identityVisible="+(info!=null && !"<unknown ssid>".equals(info.getSSID())));
    lastStatus=status; getSystemService(NotificationManager.class).notify(1,notification(status));
   }
   prefs.edit().putString("status",status).putLong("checked",System.currentTimeMillis()).apply();
  } catch (RuntimeException e) {
   Log.e("WifiDebugGuard","evaluation failed",e);
   try { if (checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")==android.content.pm.PackageManager.PERMISSION_GRANTED) Settings.Global.putInt(getContentResolver(),"adb_wifi_enabled",0); }
   catch (RuntimeException ignored) { Log.e("WifiDebugGuard","cannot disable after failure",ignored); }
   prefs.edit().putString("status","エラー："+e.getClass().getSimpleName()).apply();
  }
 }
 @Override public int onStartCommand(Intent i,int flags,int id) { evaluate(); return START_STICKY; }
 @Override public IBinder onBind(Intent i) { return null; }
 @Override public void onDestroy() { handler.removeCallbacksAndMessages(null); unregisterReceiver(receiver); getSystemService(ConnectivityManager.class).unregisterNetworkCallback(callback); super.onDestroy(); }
}
