/*
 * Copyright (C) 2012  Armin Häberling
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package ch.corten.aha.worldclock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

public abstract class ClockWidgetProvider extends AppWidgetProvider {
    private static final boolean SANS_JELLY_BEAN_MR1 = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final String WIDGET_DATA_CHANGED_ACTION = "ch.corten.aha.worldclock.WIDGET_DATA_CHANGED";

    public static final String CLOCK_TICK_ACTION = "ch.corten.aha.worldclock.CLOCK_TICK";

    private static final List<Class<? extends AppWidgetProvider>> WIDGET_PROVIDERS = new ArrayList<Class<? extends AppWidgetProvider>>();

    protected static void registerClockWidget(Class<? extends ClockWidgetProvider> provider) {
        WIDGET_PROVIDERS.add(provider);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (SANS_JELLY_BEAN_MR1) {
            // every minute starting on the next full minute
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.SECOND, 60);
            calendar.set(Calendar.SECOND, 0);
            interval = 60000;
        } else {
            // every hour starting on the next full hour
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MINUTE, 60);
            calendar.set(Calendar.MINUTE, 0);
            interval = AlarmManager.INTERVAL_HOUR;
        }
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                interval, createClockTickIntent(context));

        Class<? extends BroadcastReceiver> receiver = ClockWidgetSystemReceiver.class;
        PackageManager pm = context.getApplicationContext().getPackageManager();
        ComponentName component = new ComponentName(context, receiver);
        pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (!isAnyWidgetActive(context, WIDGET_PROVIDERS)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(createClockTickIntent(context));

            Class<? extends BroadcastReceiver> receiver = ClockWidgetSystemReceiver.class;
            PackageManager pm = context.getApplicationContext().getPackageManager();
            ComponentName component = new ComponentName(context, receiver);
            pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (WIDGET_DATA_CHANGED_ACTION.equals(intent.getAction())
                || CLOCK_TICK_ACTION.equals(intent.getAction())) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isScreenOn()) {
                onClockTick(context);
            }
        }
    }

    protected abstract void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId);

    protected abstract void onClockTick(Context context);

    private PendingIntent createClockTickIntent(Context context) {
        Intent intent = new Intent(CLOCK_TICK_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }

    protected boolean isAnyWidgetActive(Context context, List<Class<? extends AppWidgetProvider>> classes) {
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        for (Class<? extends AppWidgetProvider> clazz : classes) {
            ComponentName provider = new ComponentName(context, clazz);
            int[] ids = awm.getAppWidgetIds(provider);
            if (ids.length > 0) {
                return true;
            }
        }
        return false;
    }
}
