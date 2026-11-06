package com.darshan.amaruhome;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

/** Schedules the reminders the web app computed (inexact alarms, no special permission) and shows them as notifications. */
public class Alarms extends BroadcastReceiver {
    static final String CH = "reminders";

    static void schedule(Context c) {
        SharedPreferences p = c.getSharedPreferences("ag", Context.MODE_PRIVATE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // cancel the previous batch
        int old = p.getInt("count", 0);
        for (int i = 0; i < old; i++) am.cancel(pi(c, i, "", ""));
        int n = 0;
        try {
            JSONArray a = new JSONArray(p.getString("alarms", "[]"));
            long now = System.currentTimeMillis();
            for (int i = 0; i < a.length() && n < 100; i++) {
                JSONObject o = a.getJSONObject(i);
                long at = o.getLong("at");
                if (at <= now) continue;
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi(c, n, o.optString("title"), o.optString("body")));
                n++;
            }
        } catch (Exception ignored) { }
        p.edit().putInt("count", n).apply();
    }

    static PendingIntent pi(Context c, int code, String title, String body) {
        Intent i = new Intent(c, Alarms.class).setAction("ag.alarm." + code).putExtra("title", title).putExtra("body", body);
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(Context c, Intent in) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(in.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(in.getAction())) {
            schedule(c);
            Widgets.refresh(c);
            return;
        }
        String title = in.getStringExtra("title");
        if (title == null || title.isEmpty()) return;
        show(c, title, in.getStringExtra("body"));
    }

    static void show(Context c, String title, String body) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel(CH, "Reminders", NotificationManager.IMPORTANCE_HIGH));
        PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, CH) : new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle(title).setContentText(body).setContentIntent(open).setAutoCancel(true);
        nm.notify((int) (System.currentTimeMillis() % 100000), b.build());
    }
}
