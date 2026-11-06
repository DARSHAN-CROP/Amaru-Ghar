package com.darshan.amaruhome;

import static org.junit.Assert.*;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.Shadows;
import android.app.AlarmManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SmokeTest {
    @Test
    public void appStartsAndBridgeWorks() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        MainActivity.Bridge b = a.new Bridge();
        long soon = System.currentTimeMillis() + 3600000L;
        b.scheduleAlarms("[{\"at\":" + soon + ",\"title\":\"Bill\",\"body\":\"Today\"}]");
        AlarmManager am = (AlarmManager) a.getSystemService(Context.ALARM_SERVICE);
        assertNotNull(Shadows.shadowOf(am).getNextScheduledAlarm());
        b.setWidgetData("{\"tasks\":[\"Clean\"],\"taskN\":1,\"shop\":[\"Milk\"],\"shopN\":1,\"next\":{\"t\":\"Insurance\",\"n\":3},\"anni\":{\"n\":100},\"left\":500,\"gu\":true}");
        assertTrue(a.getSharedPreferences("ag", Context.MODE_PRIVATE).getString("w", "").contains("Insurance"));
        Widgets.refresh(a);
        assertTrue(b.canBiometric());
        assertEquals("{}", new org.json.JSONObject(b.getAuth()).optString("refresh").isEmpty() ? "{}" : "x");
    }
}
