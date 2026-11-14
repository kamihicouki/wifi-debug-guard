package io.github.kamihicouki.wifidebugguard;
public class NetworkPolicyTest {
 static int count;
 public static void main(String[] args) {
  check(true,true,"Lab WiFi","12:34:56:78:9a:bc",null,null,false);
  check(true,true,"\"Lab WiFi\"","12:34:56:78:9A:BC","Lab WiFi","12:34:56:78:9a:bc",true);
  check(false,true,"Lab WiFi","12:34:56:78:9a:bc","Lab WiFi","12:34:56:78:9a:bc",false);
  check(true,false,"Lab WiFi","12:34:56:78:9a:bc","Lab WiFi","12:34:56:78:9a:bc",false);
  check(true,true,"Other","12:34:56:78:9a:bc","Lab WiFi","12:34:56:78:9a:bc",false);
  check(true,true,"Lab WiFi","12:34:56:78:9a:bd","Lab WiFi","12:34:56:78:9a:bc",false);
  for (String s : new String[]{null,"","<unknown ssid>"}) check(true,true,s,"12:34:56:78:9a:bc","Lab WiFi","12:34:56:78:9a:bc",false);
  for (String b : new String[]{null,"","invalid","02:00:00:00:00:00","00:00:00:00:00:00","ff:ff:ff:ff:ff:ff"}) {
   check(true,true,"Lab WiFi",b,"Lab WiFi",b,false);
  }
  check(true,true,"Guest","ab:cd:ef:12:34:56","Guest","AB:CD:EF:12:34:56",true);
  System.out.println(count+" network policy checks passed");
 }
 static void check(boolean e, boolean c, String s, String b, String ts, String tb, boolean expected) {
  count++;
  if (NetworkPolicy.allowed(e,c,s,b,ts,tb)!=expected) throw new AssertionError("network policy case "+count);
 }
}
