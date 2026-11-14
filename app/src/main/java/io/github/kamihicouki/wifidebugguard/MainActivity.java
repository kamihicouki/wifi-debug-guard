package io.github.kamihicouki.wifidebugguard;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import android.view.*;
import android.graphics.Insets;
import android.net.Uri;
import android.Manifest;

public final class MainActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private SharedPreferences prefs;
 private TextView status, selected;
 private AlertDialog selectionDialog;
 private static final int REQUEST_FINE=10, REQUEST_BACKGROUND=11, REQUEST_NOTIFICATIONS=12;
 private final Runnable refresh=new Runnable() { public void run() {
  selected.setText("登録Wi-Fi: " + prefs.getString("ssid","未登録") + "\nBSSID: " + prefs.getString("bssid","未登録"));
  long checked=prefs.getLong("checked",0);
  status.setText(prefs.getString("status","停止中。READMEのADB権限設定を済ませてください。")
   + (checked==0?"":"\n最終評価: "+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(checked))));
  handler.postDelayed(this,2000);
 } };
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  getWindow().setDecorFitsSystemWindows(false);
  prefs=createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,48,32,32);
  root.setOnApplyWindowInsetsListener((view,insets) -> {
   Insets bars=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
   view.setPadding(bars.left+32,bars.top+24,bars.right+32,bars.bottom+24); return insets;
  });
  TextView title=new TextView(this); title.setText("Wi-Fi Debug Guard"); title.setTextSize(24); root.addView(title);
  TextView explanation=new TextView(this);
  explanation.setText("信頼できるWi-Fiに接続し、位置情報をONにして登録してください。SSIDとBSSIDが両方一致するときだけワイヤレスデバッグをONにします。\nADBで設定権限・正確な位置情報・バックグラウンド位置情報・通知を許可する必要があります。詳細はREADMEを参照。\nWi-Fi識別子は認証手段ではありません。登録内容は端末内だけに保存します。"); root.addView(explanation);
  Button setup=new Button(this); setup.setText("1. 権限とADBのセットアップ"); setup.setOnClickListener(v -> continueSetup()); root.addView(setup);
  Button privacy=new Button(this); privacy.setText("データと位置情報の利用について"); privacy.setOnClickListener(v -> showDisclosure(false)); root.addView(privacy);
  Button privacyLink=new Button(this); privacyLink.setText("公開プライバシーポリシーを開く");
  privacyLink.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/kamihicouki/wifi-debug-guard/blob/main/docs/play/privacy-policy.md")))); root.addView(privacyLink);
  selected=new TextView(this); root.addView(selected);
  status=new TextView(this); root.addView(status);
  Button register=new Button(this); register.setText("2. 現在接続中のWi-Fiを登録");
  register.setOnClickListener(v -> selectCurrentWifi()); root.addView(register);
  Button start=new Button(this); start.setText("3. 自動管理を開始"); start.setOnClickListener(v -> {
   if (!NetworkPolicy.validIdentity(prefs.getString("ssid",null),prefs.getString("bssid",null)) || !permissionsReady()) {
    Toast.makeText(this,"Wi-Fi登録と設定・正確な位置情報・常時位置情報・通知の権限が必要です",Toast.LENGTH_LONG).show(); return;
   }
   prefs.edit().putBoolean("enabled",true).apply(); startGuard();
  }); root.addView(start);
  Button stop=new Button(this); stop.setText("自動管理を停止してデバッグOFF"); stop.setOnClickListener(v -> stopGuard()); root.addView(stop);
  ScrollView scroll=new ScrollView(this); scroll.addView(root); setContentView(scroll);
  if (prefs.getBoolean("enabled",false)) startGuard();
  root.requestApplyInsets();
 }
 private boolean granted(String permission) { return checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED; }
 private SetupPolicy.Step setupStep() {
  return SetupPolicy.next(prefs.getInt("disclosure_version",0)>=1,
   granted(Manifest.permission.ACCESS_FINE_LOCATION),granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
   Build.VERSION.SDK_INT<33 || granted(Manifest.permission.POST_NOTIFICATIONS),granted(Manifest.permission.WRITE_SECURE_SETTINGS),
   getSystemService(android.location.LocationManager.class).isLocationEnabled());
 }
 private boolean permissionsReady() { return setupStep()==SetupPolicy.Step.READY; }
 private void showDisclosure(boolean requestAfter) {
  selectionDialog=new AlertDialog.Builder(this).setTitle("バックグラウンド位置情報の利用")
   .setMessage("Wi-Fi Debug Guardは、アプリを閉じている間や端末の再起動後も、登録したWi-Fiかを識別してワイヤレスデバッグをON/OFFするために、正確な位置情報とバックグラウンド位置情報へのアクセスを利用します。読み取るのは接続中のWi-Fi名（SSID）とアクセスポイント識別子（BSSID）です。GPS座標・移動履歴は取得しません。\n\n登録したWi-Fi情報と管理状態はこの端末内だけに保存し、外部送信・広告・解析には利用しません。位置情報の利用を許可しない場合も説明画面は利用できますが、自動管理は開始できません。\n\nWi-Fiの識別情報は偽装可能です。信頼できる私有Wi-Fiだけで利用してください。")
   .setNegativeButton("戻る",null).setPositiveButton(requestAfter?"同意して権限設定へ":"閉じる",(d,w) -> {
    if(requestAfter) { prefs.edit().putInt("disclosure_version",1).apply(); continueSetup(); }
   }).show();
 }
 private void continueSetup() {
  switch (setupStep()) {
   case DISCLOSURE: showDisclosure(true); break;
   case FINE_LOCATION:
    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_FINE); break;
   case BACKGROUND_LOCATION:
    selectionDialog=new AlertDialog.Builder(this).setTitle("アプリを閉じている間のWi-Fi識別")
     .setMessage("自動管理を続けるため、次のAndroid権限画面で位置情報を「常に許可」に設定してください。「正確な位置情報」も必要です。拒否すると自動管理は動作しません。")
     .setNegativeButton("今はしない",null).setPositiveButton("権限画面へ",(d,w) -> requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},REQUEST_BACKGROUND)).show(); break;
   case NOTIFICATIONS:
    if(Build.VERSION.SDK_INT>=33) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQUEST_NOTIFICATIONS); break;
   case ADB_PERMISSION:
    TextView command=new TextView(this); command.setPadding(32,16,32,16); command.setTextIsSelectable(true);
    command.setText("信頼できるPCにUSB接続し、ADBで以下を一度だけ実行してください。この権限は端末のデバッグ設定を変更する強い権限です。アプリから自動付与はできません。\n\nadb shell pm grant "+getPackageName()+" android.permission.WRITE_SECURE_SETTINGS\n\n接続中の端末が複数ある場合は adb -s <接続先> を指定してください。");
    selectionDialog=new AlertDialog.Builder(this).setTitle("PCからADB権限を付与").setView(command).setPositiveButton("閉じる",null).show(); break;
   case LOCATION_SETTING:
    selectionDialog=new AlertDialog.Builder(this).setTitle("位置情報設定がOFFです").setMessage("接続Wi-Fiを識別するため端末の位置情報設定をONにしてください。GPS座標は取得しません。")
     .setNegativeButton("戻る",null).setPositiveButton("設定を開く",(d,w) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).show(); break;
   case READY: Toast.makeText(this,"準備できました。Wi-Fiを登録して管理を開始してください",Toast.LENGTH_LONG).show(); break;
  }
 }
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
  super.onRequestPermissionsResult(requestCode,permissions,grantResults);
  boolean accepted=grantResults.length>0;
  for(int result:grantResults) accepted &= result==PackageManager.PERMISSION_GRANTED;
  if(accepted) continueSetup();
  else {
   selectionDialog=new AlertDialog.Builder(this).setTitle("権限は許可されていません")
    .setMessage("自動管理には正確な位置情報・常に許可・通知が必要です。許可しない場合は管理を開始しません。再設定する場合はアプリの権限画面を開いてください。")
    .setNegativeButton("閉じる",null).setPositiveButton("アプリ設定",(d,w) -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())))).show();
  }
 }
 private void startGuard() {
  if (!permissionsReady()) {
   prefs.edit().putString("status","セットアップが必要です。権限と位置情報設定を確認してください").apply(); return;
  }
  try { startForegroundService(new Intent(this,GuardService.class)); }
  catch (RuntimeException e) { prefs.edit().putBoolean("enabled",false).putString("status","開始できません。端末の実行制限を確認して再開してください").apply(); }
 }
 @SuppressWarnings("deprecation") private void selectCurrentWifi() {
  if (!permissionsReady()) { continueSetup(); return; }
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
