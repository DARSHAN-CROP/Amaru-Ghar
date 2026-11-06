package com.darshan.amaruhome;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

/** Home-screen widgets: "Today" (chores, next reminder, anniversary, budget left) and "Shopping list". */
public class Widgets {
    public static class Today extends AppWidgetProvider {
        @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { Widgets.refresh(c); }
    }
    public static class Shop extends AppWidgetProvider {
        @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { Widgets.refresh(c); }
    }

    static JSONObject data(Context c) {
        try { return new JSONObject(c.getSharedPreferences("ag", Context.MODE_PRIVATE).getString("w", "{}")); } catch (Exception e) { return new JSONObject(); }
    }

    static String join(JSONArray a, int max, String bullet) {
        StringBuilder sb = new StringBuilder();
        if (a != null) for (int i = 0; i < a.length() && i < max; i++) { if (sb.length() > 0) sb.append("\n"); sb.append(bullet).append(a.optString(i)); }
        return sb.toString();
    }

    static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        JSONObject d = data(c);
        boolean gu = d.optBoolean("gu", true);
        PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        for (int id : m.getAppWidgetIds(new ComponentName(c, Today.class))) {
            RemoteViews v = new RemoteViews(c.getPackageName(), R_layout_today());
            v.setTextViewText(R_id_title(), gu ? "સાવસાણી ફેમિલી" : "Savsani Family");
            String tasks = join(d.optJSONArray("tasks"), 3, "• ");
            v.setTextViewText(R_id_tasks(), (gu ? "આજના કામ: " : "Chores: ") + d.optInt("taskN", 0) + (tasks.isEmpty() ? "" : "\n" + tasks));
            JSONObject nx = d.optJSONObject("next");
            String line = nx == null ? (gu ? "કોઈ રિમાઇન્ડર નથી" : "No reminders") : (gu ? "આગળ: " : "Next: ") + nx.optString("t") + " (" + (nx.optInt("n") <= 0 ? (gu ? "આજે" : "today") : nx.optInt("n") + (gu ? " દિવસ" : " d")) + ")";
            JSONObject an = d.optJSONObject("anni");
            if (an != null) line += "\n💍 " + an.optInt("n") + (gu ? " દિવસ બાકી" : " days to go");
            if (d.has("left") && !d.isNull("left")) line += "\n💰 " + (gu ? "બાકી બજેટ ₹" : "Budget left ₹") + d.optLong("left");
            v.setTextViewText(R_id_info(), line);
            v.setOnClickPendingIntent(R_id_root(), open);
            m.updateAppWidget(id, v);
        }
        for (int id : m.getAppWidgetIds(new ComponentName(c, Shop.class))) {
            RemoteViews v = new RemoteViews(c.getPackageName(), R_layout_shop());
            v.setTextViewText(R_id_title(), (gu ? "શોપિંગ લિસ્ટ" : "Shopping list") + " (" + d.optInt("shopN", 0) + ")");
            String s = join(d.optJSONArray("shop"), 6, "☐ ");
            v.setTextViewText(R_id_tasks(), s.isEmpty() ? (gu ? "બધું લઈ લીધું ✅" : "All done ✅") : s);
            v.setOnClickPendingIntent(R_id_root(), open);
            m.updateAppWidget(id, v);
        }
    }

    static int R_layout_today() { return R.layout.widget_today; }
    static int R_layout_shop() { return R.layout.widget_shop; }
    static int R_id_title() { return R.id.w_title; }
    static int R_id_tasks() { return R.id.w_tasks; }
    static int R_id_info() { return R.id.w_info; }
    static int R_id_root() { return R.id.w_root; }
}
