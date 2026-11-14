package io.github.kamihicouki.wifidebugguard;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.widget.*;

public final class MainActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private SharedPreferences prefs;
 private TextView status, selected;
 private AlertDialog selectionDialog;
 private final Runnable refresh=new Runnable() { public void run() {
  selected.setText("登録Wi-Fi: " + prefs.getString("ssid","未登録") + "\nBSSID: " + prefs.getString("bssid","未登録"));
  long checked=prefs.getLong("checked",0);
  status.setText(prefs.getString("status","停止中。READMEのADB権限設定を済ませてください。")
   + (checked==0?"":"\n最終評価: "+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(checked))));
  handler.postDelayed(this,2000);
 } };
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  prefs=createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,48,32,32);
  TextView title=new TextView(this); title.setText("Wi-Fi Debug Guard"); title.setTextSize(24); root.addView(title);
  TextView explanation=new TextView(this);
  explanation.setText("信頼できるWi-Fiに接続し、位置情報をONにして登録してください。SSIDとBSSIDが両方一致するときだけワイヤレスデバッグをONにします。\nADBで設定権限・正確な位置情報・バックグラウンド位置情報・通知を許可する必要があります。詳細はREADMEを参照。\nWi-Fi識別子は認証手段ではありません。登録内容は端末内だけに保存します。"); root.addView(explanation);
  selected=new TextView(this); root.addView(selected);
  status=new TextView(this); root.addView(status);
  Button register=new Button(this); register.setText("現在接続中のWi-Fiを登録");
  register.setOnClickListener(v -> selectCurrentWifi()); root.addView(register);
  Button start=new Button(this); start.setText("自動管理を開始"); start.setOnClickListener(v -> {
   if (!NetworkPolicy.validIdentity(prefs.getString("ssid",null),prefs.getString("bssid",null)) || !permissionsReady()) {
    Toast.makeText(this,"Wi-Fi登録と設定・正確な位置情報・常時位置情報・通知の権限が必要です",Toast.LENGTH_LONG).show(); return;
   }
   prefs.edit().putBoolean("enabled",true).apply(); startGuard();
  }); root.addView(start);
  Button stop=new Button(this); stop.setText("自動管理を停止してデバッグOFF"); stop.setOnClickListener(v -> stopGuard()); root.addView(stop);
  ScrollView scroll=new ScrollView(this); scroll.addView(root); setContentView(scroll);
  if (prefs.getBoolean("enabled",false)) startGuard();
 }
 private boolean permissionsReady() {
  return checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")==PackageManager.PERMISSION_GRANTED
   && checkSelfPermission("android.permission.ACCESS_FINE_LOCATION")==PackageManager.PERMISSION_GRANTED
   && checkSelfPermission("android.permission.ACCESS_BACKGROUND_LOCATION")==PackageManager.PERMISSION_GRANTED
   && (Build.VERSION.SDK_INT<33 || checkSelfPermission("android.permission.POST_NOTIFICATIONS")==PackageManager.PERMISSION_GRANTED);
 }
 private void startGuard() {
  try { startForegroundService(new Intent(this,GuardService.class)); }
  catch (RuntimeException e) { prefs.edit().putBoolean("enabled",false).putString("status","開始できません。端末の実行制限を確認して再開してください").apply(); }
 }
 @SuppressWarnings("deprecation") private void selectCurrentWifi() {
  try {
   WifiInfo info=getSystemService(WifiManager.class).getConnectionInfo();
   if (!getSystemService(android.location.LocationManager.class).isLocationEnabled() || info==null || info.getNetworkId()==-1 || !NetworkPolicy.validIdentity(NetworkPolicy.normalizeSsid(info.getSSID()),info.getBSSID())) {
    Toast.makeText(this,"接続Wi-Fiを取得できません。Wi-Fi・位置情報・権限を確認してください",Toast.LENGTH_LONG).show(); return;
   }
   final String ssid=NetworkPolicy.normalizeSsid(info.getSSID()), bssid=info.getBSSID();
   selectionDialog=new AlertDialog.Builder(this).setTitle("このWi-Fiでデバッグを許可")
    .setMessage(ssid+"\n"+bssid+"\n登録すると自動管理を停止します。確認後に開始してください。")
    .setNegativeButton("キャンセル",null).setPositiveButton("登録",(d,w) -> {
     stopGuard(); prefs.edit().putString("ssid",ssid).putString("bssid",bssid).apply();
    }).show();
  } catch (RuntimeException e) { Toast.makeText(this,"Wi-Fi情報の取得に失敗しました。権限を確認してください",Toast.LENGTH_LONG).show(); }
 }
 private void stopGuard() {
  prefs.edit().putBoolean("enabled",false).apply();
  String message="自動管理は停止中です。デバッグOFFを要求しました";
  try {
   if (checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")!=PackageManager.PERMISSION_GRANTED || !Settings.Global.putInt(getContentResolver(),"adb_wifi_enabled",0)) message="管理停止。デバッグOFFにできません。端末設定でOFFにしてください";
  } catch (RuntimeException e) { message="管理停止。デバッグOFFに失敗。端末設定でOFFにしてください"; }
  stopService(new Intent(this,GuardService.class)); prefs.edit().putString("status",message).apply();
 }
 @Override public void onDestroy() { if(selectionDialog!=null) selectionDialog.dismiss(); super.onDestroy(); }
 @Override public void onResume() { super.onResume(); handler.post(refresh); }
 @Override public void onPause() { handler.removeCallbacksAndMessages(null); super.onPause(); }
}
