package io.github.kamihicouki.wifidebugguard;
public final class DashboardStateTest {
 static int count;
 public static void main(String[] args) {
  eq("停止中",DashboardState.management(false,false));
  eq("開始待ち",DashboardState.management(true,false));
  eq("管理中",DashboardState.management(true,true));
  eq("停止処理中",DashboardState.management(false,true));
  eq("ON",DashboardState.debug(1)); eq("OFF",DashboardState.debug(0)); eq("確認不能",DashboardState.debug(-1)); eq("確認不能",DashboardState.debug(9));
  eq("対象Wi-Fiを登録してください",DashboardState.wifi(false,false,false,false));
  eq("Wi-Fi未接続",DashboardState.wifi(true,false,false,false));
  eq("接続先を確認できません",DashboardState.wifi(true,true,false,false));
  eq("対象Wi-Fiに接続中",DashboardState.wifi(true,true,true,true));
  eq("別のWi-Fiに接続中",DashboardState.wifi(true,true,true,false));
  System.out.println(count+" dashboard state checks passed");
 }
 static void eq(String a,String b) { count++; if(!a.equals(b)) throw new AssertionError(a+" != "+b); }
}
