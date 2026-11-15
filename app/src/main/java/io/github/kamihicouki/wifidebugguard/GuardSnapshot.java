package io.github.kamihicouki.wifidebugguard;
import android.app.ActivityManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.*;
import android.net.wifi.*;
import android.os.Build;
import android.provider.Settings;
/** A read-only observation of current OS state. Reading never starts management or writes settings. */
public final class GuardSnapshot {
 public boolean enabled,autoStart,serviceRunning,consent,securePermission,finePermission,backgroundPermission,notificationPermission,locationEnabled,wifiConnected,targetConfigured,targetMatched;
 public String selectedSsid,currentSsid,selectedBssid,currentBssid,status;
 public int debugSetting=-1;
 public long checked;
 private GuardSnapshot() {}
 private static boolean granted(Context c,String p) { return c.checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED; }
 @SuppressWarnings("deprecation") public static GuardSnapshot read(Context c) {
  GuardSnapshot s=new GuardSnapshot();
  SharedPreferences p=c.createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  s.enabled=p.getBoolean("enabled",false); s.autoStart=p.getBoolean("auto_start_on_boot",s.enabled);
  s.consent=p.getInt("disclosure_version",0)>=1;
  s.selectedSsid=p.getString("ssid",null); s.selectedBssid=p.getString("bssid",null);
  s.status=p.getString("status","未確認");s.checked=p.getLong("checked",0);
  s.targetConfigured=NetworkPolicy.validIdentity(s.selectedSsid,s.selectedBssid);
  s.securePermission=granted(c,"android.permission.WRITE_SECURE_SETTINGS");
  s.finePermission=granted(c,"android.permission.ACCESS_FINE_LOCATION");
  s.backgroundPermission=granted(c,"android.permission.ACCESS_BACKGROUND_LOCATION");
  s.notificationPermission=Build.VERSION.SDK_INT<33 || granted(c,"android.permission.POST_NOTIFICATIONS");
  try { s.locationEnabled=c.getSystemService(android.location.LocationManager.class).isLocationEnabled(); } catch(RuntimeException ignored) {}
  try { int value=Settings.Global.getInt(c.getContentResolver(),"adb_wifi_enabled",-1);s.debugSetting=(value==0 || value==1)?value:-1; } catch(RuntimeException ignored) {}
  try {
   ActivityManager am=c.getSystemService(ActivityManager.class);
   for(ActivityManager.RunningServiceInfo service:am.getRunningServices(Integer.MAX_VALUE)) {
    if(service.service.getPackageName().equals(c.getPackageName()) && service.service.getClassName().equals(GuardService.class.getName()) && service.pid>0) {s.serviceRunning=true;break;}
   }
  } catch(RuntimeException ignored) {}
  try {
   NetworkInfo ni=c.getSystemService(ConnectivityManager.class).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
   s.wifiConnected=ni!=null && ni.isConnected();
   if(s.consent && s.finePermission && s.backgroundPermission && s.locationEnabled) {
    WifiInfo info=c.getSystemService(WifiManager.class).getConnectionInfo();
    if(info!=null && info.getNetworkId()!=-1) {
     String ssid=NetworkPolicy.normalizeSsid(info.getSSID()),bssid=info.getBSSID();
     if(NetworkPolicy.validIdentity(ssid,bssid)) {s.currentSsid=ssid;s.currentBssid=bssid;}
     s.targetMatched=NetworkPolicy.allowed(true,s.wifiConnected,info.getSSID(),info.getBSSID(),s.selectedSsid,s.selectedBssid);
    }
   }
  } catch(RuntimeException ignored) {}
  return s;
 }
}
