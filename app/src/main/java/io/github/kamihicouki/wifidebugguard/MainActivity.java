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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.database.ContentObserver;
import android.net.Uri;
import android.Manifest;

public final class MainActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private SharedPreferences prefs;
 private TextView managementValue,managementNote,debugValue,wifiValue,wifiDetail,nextAction,lastCheck;
 private Button startButton,stopButton;
 private Switch bootSwitch;
 private boolean rendering,observing;
 private String actionMessage="";
 private AlertDialog selectionDialog;
 private static final int REQUEST_FINE=10, REQUEST_BACKGROUND=11, REQUEST_NOTIFICATIONS=12;
 private static final int INK=0xff173b43,TEAL=0xff007d70,MUTED=0xff536c72;
 private final Runnable refresh=new Runnable() { public void run() { render(); handler.postDelayed(this,1000); } };
 private final ContentObserver debugObserver=new ContentObserver(handler) { public void onChange(boolean selfChange) { render(); } };
 private final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener=(p,k) -> handler.post(this::render);
 private int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
 private TextView text(String value,int size,int color) {
  TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,dp(3),0,dp(3));return v;
 }
 private LinearLayout card(LinearLayout parent,String label) {
  LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(12),dp(16),dp(12));
  GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));c.setBackground(bg);
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);parent.addView(c,lp);
  TextView heading=text(label,13,MUTED);heading.setTypeface(null,Typeface.BOLD);c.addView(heading);return c;
 }
 private Button button(LinearLayout parent,String label,Runnable action) {
  Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(INK);b.setOnClickListener(v -> action.run());parent.addView(b,new LinearLayout.LayoutParams(-1,-2));return b;
 }
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);getWindow().setDecorFitsSystemWindows(false);
  prefs=createDeviceProtectedStorageContext().getSharedPreferences("guard",0);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xffedf4f3);
  root.setOnApplyWindowInsetsListener((v,insets) -> {Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left+dp(16),bars.top+dp(16),bars.right+dp(16),bars.bottom+dp(20));return insets;});
  TextView title=text("Wi-Fi Debug Guard",24,INK);title.setTypeface(null,Typeface.BOLD);root.addView(title);
  root.addView(text("指定Wi-Fiだけで、デバッグを自動管理",13,MUTED));
  LinearLayout manage=card(root,"自動管理");managementValue=text("確認中",28,INK);managementValue.setTypeface(null,Typeface.BOLD);manage.addView(managementValue);
  managementNote=text("",13,MUTED);manage.addView(managementNote);
  bootSwitch=new Switch(this);bootSwitch.setText("端末起動時に自動で開始");bootSwitch.setTextSize(15);bootSwitch.setTextColor(INK);bootSwitch.setPadding(0,dp(10),0,dp(10));manage.addView(bootSwitch);
  bootSwitch.setOnCheckedChangeListener((v,checked) -> {if(rendering)return;prefs.edit().putBoolean("auto_start_on_boot",checked).apply();actionMessage=checked?"次の端末起動時に開始します。必要な設定を下で確認してください":"起動時の自動開始をOFFにしました。現在の管理状態は変更しません";render();});
  startButton=button(manage,"自動管理を開始",this::requestStart);
  startButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(TEAL));startButton.setTextColor(Color.WHITE);
  stopButton=button(manage,"管理を停止してデバッグをOFF",this::confirmStop);stopButton.setTextColor(0xffa43c3c);
  manage.addView(text("停止すると、起動時の自動開始もOFFになります。",12,MUTED));
  LinearLayout debug=card(root,"ワイヤレスデバッグの現在の設定");debugValue=text("確認中",26,INK);debugValue.setTypeface(null,Typeface.BOLD);debug.addView(debugValue);
  debug.addView(text("Androidの現在値を読み取っています。ONでもPCとのADB接続成功を示すものではありません。",12,MUTED));
  LinearLayout wifi=card(root,"Wi-Fiの判定");wifiValue=text("確認中",19,INK);wifiValue.setTypeface(null,Typeface.BOLD);wifi.addView(wifiValue);wifiDetail=text("",13,MUTED);wifi.addView(wifiDetail);
  button(wifi,"接続中のWi-Fiを対象として登録",this::selectCurrentWifi);
  LinearLayout setup=card(root,"次に必要な操作");nextAction=text("確認中",15,INK);setup.addView(nextAction);
  button(setup,"権限・初回設定を確認",this::continueSetup);
  button(setup,"Androidのデバッグ設定を開く",() -> startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)));
  lastCheck=text("",12,MUTED);setup.addView(lastCheck);
  button(root,"位置情報の利用説明",() -> showDisclosure(false));
  button(root,"公開プライバシーポリシー",() -> startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/kamihicouki/wifi-debug-guard/blob/main/docs/play/privacy-policy.md"))));
  android.widget.FrameLayout frame=new android.widget.FrameLayout(this);frame.setBackgroundColor(0xffedf4f3);
  android.widget.FrameLayout.LayoutParams content=new android.widget.FrameLayout.LayoutParams(Math.min(getResources().getDisplayMetrics().widthPixels,dp(720)),-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);frame.addView(root,content);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.addView(frame);setContentView(scroll);root.requestApplyInsets();
  render();if(prefs.getBoolean("enabled",false))startGuard();
 }
 private void render() {
  if(managementValue==null)return;
  GuardSnapshot s=GuardSnapshot.read(this);
  if(s.enabled&&s.serviceRunning&&actionMessage.startsWith("管理の開始を要求"))actionMessage="管理を開始しました。起動時の自動開始もONです";
  rendering=true;bootSwitch.setChecked(s.autoStart);rendering=false;
  managementValue.setText(DashboardState.management(s.enabled,s.serviceRunning));managementValue.setTextColor(s.enabled&&s.serviceRunning?TEAL:INK);
  managementNote.setText(!actionMessage.isEmpty()?actionMessage:s.enabled?(s.serviceRunning?"接続先を継続的に確認しています":"管理の開始要求があります。下の不足設定や最終結果を確認してください"):(s.autoStart?"現在は停止中。設定が揃えば次の端末起動時に開始します":"現在も次の端末起動後も、自動管理は停止します"));
  debugValue.setText(DashboardState.debug(s.debugSetting));debugValue.setTextColor(s.debugSetting==1?TEAL:INK);
  wifiValue.setText(DashboardState.wifi(s.targetConfigured,s.wifiConnected,s.currentSsid!=null,s.targetMatched));
  wifiDetail.setText("登録: "+(s.selectedSsid==null?"未登録":s.selectedSsid)+"\n現在: "+(s.currentSsid==null?(s.wifiConnected?"識別情報を取得できません":"未接続"):s.currentSsid)+(s.selectedBssid==null?"":"\n対象AP: "+s.selectedBssid)+(s.currentBssid==null?"":"\n現在AP: "+s.currentBssid));
  String required;
  switch(setupStep()) {
   case DISCLOSURE: required="位置情報の利用説明を確認し、同意する場合は初回設定へ進んでください。";break;
   case FINE_LOCATION: required="正確な位置情報の許可が必要です。";break;
   case BACKGROUND_LOCATION: required="位置情報を「常に許可」にしてください。";break;
   case NOTIFICATIONS: required="常駐状態を確認できるよう、通知を許可してください。";break;
   case ADB_PERMISSION: required="PCからADBで設定変更権限を一度付与してください。";break;
   case LOCATION_SETTING: required="端末の位置情報をONにしてください。";break;
   default: required=!s.targetConfigured?"接続中の信頼できるWi-Fiを登録してください。":s.enabled?(s.serviceRunning?"必要な設定は揃っています。":"設定は揃っています。「自動管理を開始」で再開できます。"):(s.autoStart?"今すぐ使う場合は「自動管理を開始」を押してください。":"「自動管理を開始」を押すと、起動時の自動開始もONになります。");
  }
  nextAction.setText(required);
  lastCheck.setText("最終判定: "+(s.checked==0?"まだ実行していません":java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(s.checked)))+"\n"+s.status);
  startButton.setEnabled(!s.enabled||!s.serviceRunning);
 }
 private void requestStart() {
  if(!permissionsReady()) {actionMessage="初回設定が必要です";render();continueSetup();return;}
  if(!NetworkPolicy.validIdentity(prefs.getString("ssid",null),prefs.getString("bssid",null))) {actionMessage="先に対象Wi-Fiを登録してください";render();return;}
  prefs.edit().putBoolean("enabled",true).putBoolean("auto_start_on_boot",true).apply();actionMessage="管理の開始を要求しました。起動時の自動開始もONです";startGuard();render();handler.postDelayed(this::render,150);
 }
 private void confirmStop() {
  selectionDialog=new AlertDialog.Builder(this).setTitle("管理を停止しますか？")
   .setMessage("ワイヤレスデバッグのOFFを要求し、起動時の自動開始もOFFにします。無線ADB接続は切断されます。再開はこの端末の開始ボタンから行えます。")
   .setNegativeButton("戻る",null).setPositiveButton("停止してOFF",(d,w) -> stopGuard()).show();
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
    if(requestAfter) { prefs.edit().putInt("disclosure_version",1).apply(); render();continueSetup(); }
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
  if (!permissionsReady() || !NetworkPolicy.validIdentity(prefs.getString("ssid",null),prefs.getString("bssid",null))) {
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
    .setMessage(ssid+"\n"+bssid+"\n管理中はこのWi-Fiを新しい対象にします。停止中は登録後に管理を開始してください。")
    .setNegativeButton("キャンセル",null).setPositiveButton("登録",(d,w) -> {
     prefs.edit().putString("ssid",ssid).putString("bssid",bssid).apply();actionMessage="対象Wi-Fiを登録しました";if(prefs.getBoolean("enabled",false))startGuard();render();
    }).show();
  } catch (RuntimeException e) { Toast.makeText(this,"Wi-Fi情報の取得に失敗しました。権限を確認してください",Toast.LENGTH_LONG).show(); }
 }
 private void stopGuard() {
  prefs.edit().putBoolean("enabled",false).putBoolean("auto_start_on_boot",false).apply();
  String message="自動管理は停止中です。デバッグOFFを要求しました";
  try {
   if (checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")!=PackageManager.PERMISSION_GRANTED || !Settings.Global.putInt(getContentResolver(),"adb_wifi_enabled",0)) message="管理停止。デバッグOFFにできません。端末設定でOFFにしてください";
  } catch (RuntimeException e) { message="管理停止。デバッグOFFに失敗。端末設定でOFFにしてください"; }
  stopService(new Intent(this,GuardService.class));prefs.edit().putString("status",message).putLong("checked",System.currentTimeMillis()).apply();actionMessage=message;render();
 }
 @Override public void onDestroy() { if(selectionDialog!=null) selectionDialog.dismiss(); super.onDestroy(); }
 @Override public void onResume() {super.onResume();actionMessage="";prefs.registerOnSharedPreferenceChangeListener(preferenceListener);getContentResolver().registerContentObserver(Settings.Global.getUriFor("adb_wifi_enabled"),false,debugObserver);observing=true;handler.removeCallbacks(refresh);handler.post(refresh);}
 @Override public void onPause() {handler.removeCallbacksAndMessages(null);prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener);if(observing){getContentResolver().unregisterContentObserver(debugObserver);observing=false;}super.onPause();}
}
