package io.github.kamihicouki.wifidebugguard;

/** Pure, fail-closed matching for one user-selected access point. */
public final class NetworkPolicy {
 private NetworkPolicy() {}
 /** Decode the single outer quote pair returned by WifiInfo, once at the read boundary. */
 public static String normalizeSsid(String ssid) {
  if (ssid != null && ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) return ssid.substring(1, ssid.length()-1);
  return ssid;
 }
 /** Validate an already-decoded SSID. Never decode a saved SSID again. */
 public static boolean validIdentity(String ssid, String bssid) {
  return ssid != null && !ssid.isEmpty() && !"<unknown ssid>".equals(ssid)
   && bssid != null && bssid.matches("(?i)([0-9a-f]{2}:){5}[0-9a-f]{2}")
   && !"02:00:00:00:00:00".equals(bssid) && !"00:00:00:00:00:00".equals(bssid)
   && !"ff:ff:ff:ff:ff:ff".equalsIgnoreCase(bssid);
 }
 public static boolean allowed(boolean enabled, boolean connected, String ssid, String bssid,
                               String selectedSsid, String selectedBssid) {
  String currentSsid = normalizeSsid(ssid);
  return enabled && connected && validIdentity(currentSsid,bssid) && validIdentity(selectedSsid,selectedBssid)
   && selectedSsid.equals(currentSsid) && selectedBssid.equalsIgnoreCase(bssid);
 }
}
