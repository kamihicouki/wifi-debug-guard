package io.github.kamihicouki.wifidebugguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import java.io.PrintWriter;

import rikka.shizuku.Shizuku;

final class ShizukuMonitor {
 private static final String TAG = "GuardShizuku";
 private static final String CHANNEL = "shizuku_stop";
 private static final int NOTIFICATION_ID = 2;

 private final Context context;
 private final NotificationManager notifications;
 private final SharedPreferences prefs;
 private final Handler handler;
 private final ShizukuState state = new ShizukuState();
 private final Shizuku.OnBinderReceivedListener received = this::binderReceived;
 private final Shizuku.OnBinderDeadListener dead = this::binderDead;
 private boolean active;
 private long runningAt;

 ShizukuMonitor(Context context, Handler handler) {
  this.context = context;
  this.handler = handler;
  notifications = context.getSystemService(NotificationManager.class);
  prefs = context.createDeviceProtectedStorageContext().getSharedPreferences("shizuku_monitor", Context.MODE_PRIVATE);
 }

 void start() {
  notifications.createNotificationChannel(new NotificationChannel(
   CHANNEL, "Shizukuの停止", NotificationManager.IMPORTANCE_DEFAULT));
  active = true;
  Shizuku.addBinderDeadListener(dead, handler);
  Shizuku.addBinderReceivedListenerSticky(received, handler);
 }

 void close() {
  active = false;
  Shizuku.removeBinderReceivedListener(received);
  Shizuku.removeBinderDeadListener(dead);
 }

 private void binderReceived() {
  if (active && Shizuku.pingBinder() && state.running()) runningAt = System.currentTimeMillis();
 }

 private void binderDead() {
  if (!active || !state.died()) return;
  long stoppedAt = System.currentTimeMillis();
  SharedPreferences.Editor event = prefs.edit()
   .putLong("stopped_at", stoppedAt)
   .putLong("stop_count", prefs.getLong("stop_count", 0) + 1)
   .remove("notification_requested_at");
  try {
   NotificationChannel channel = notifications.getNotificationChannel(CHANNEL);
   if (notifications.areNotificationsEnabled()
       && channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
    PendingIntent openGuard = PendingIntent.getActivity(context, 0,
     new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
    Notification notification = new Notification.Builder(context, CHANNEL)
     .setSmallIcon(android.R.drawable.stat_notify_error)
     .setContentTitle("Shizukuが停止しました")
     .setContentText("Shizukuを使う操作は、端末で稼働状態を確認してください。")
     .setContentIntent(openGuard)
     .setWhen(stoppedAt)
     .setShowWhen(true)
     .setAutoCancel(true)
     .build();
    notifications.notify(NOTIFICATION_ID, notification);
    event.putLong("notification_requested_at", System.currentTimeMillis())
     .putLong("notification_request_count", prefs.getLong("notification_request_count", 0) + 1);
   }
  } catch (RuntimeException failure) {
   Log.w(TAG, "Could not notify about Shizuku stopping", failure);
  } finally {
   event.apply();
  }
 }

 void dump(PrintWriter out) {
  out.println("shizukuState=" + state.current() + " shizukuRunningAt=" + runningAt
   + " shizukuLastStoppedAt=" + prefs.getLong("stopped_at", 0)
   + " shizukuStopCount=" + prefs.getLong("stop_count", 0)
   + " shizukuLastNotificationRequestedAt=" + prefs.getLong("notification_requested_at", 0)
   + " shizukuNotificationRequestCount=" + prefs.getLong("notification_request_count", 0));
 }
}
