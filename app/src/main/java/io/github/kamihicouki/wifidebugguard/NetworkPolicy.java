package io.github.kamihicouki.wifidebugguard;

/** Pure, fail-closed matching for one user-selected access point. */
public final class NetworkPolicy {
 private NetworkPolicy() {}
 public static String normalizeSsid(String ssid) {
  if (ssid != null && ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) return ssid.substring(1, ssid.length()-1);
  return ssid;
 }
 public static boolean validIdentity(String ssid, String bssid) {
  ssid = normalizeSsid(ssid);
  return ssid != null && !ssid.isEmpty() && !"<unknown ssid>".equals(ssid)
   && bssid != null && bssid.matches("(?i)([0-9a-f]{2}:){5}[0-9a-f]{2}")
   && !"02:00:00:00:00:00".equals(bssid) && !"00:00:00:00:00:00".equals(bssid)
   && !"ff:ff:ff:ff:ff:ff".equalsIgnoreCase(bssid);
 }
 public static boolean allowed(boolean enabled, boolean connected, String ssid, String bssid,
                               String selectedSsid, String selectedBssid) {
  return enabled && connected && validIdentity(ssid,bssid) && validIdentity(selectedSsid,selectedBssid)
   && normalizeSsid(selectedSsid).equals(normalizeSsid(ssid)) && selectedBssid.equalsIgnoreCase(bssid);
 }
}
