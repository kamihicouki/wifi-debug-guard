package io.github.kamihicouki.wifidebugguard;
/** Presentation decisions shared by the live dashboard and unit tests. */
public final class DashboardState {
 private DashboardState() {}
 public static String management(boolean enabled,boolean running) { return enabled?(running?"管理中":"開始待ち"):(running?"停止処理中":"停止中"); }
 public static String debug(int setting) { return setting==1?"ON":setting==0?"OFF":"確認不能"; }
 public static String wifi(boolean configured,boolean connected,boolean visible,boolean match) {
  if(!configured) return "対象Wi-Fiを登録してください";
  if(!connected) return "Wi-Fi未接続";
  if(!visible) return "接続先を確認できません";
  return match?"対象Wi-Fiに接続中":"別のWi-Fiに接続中";
 }
}
