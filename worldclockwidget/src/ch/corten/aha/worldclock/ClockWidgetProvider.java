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

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;

public abstract class ClockWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_DATA_CHANGED_ACTION = "ch.corten.aha.worldclock.WIDGET_DATA_CHANGED";

    private final String mClockTickAction;

    public ClockWidgetProvider(String clockTickAction) {
        mClockTickAction = clockTickAction; 
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
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.SECOND, 60);
        calendar.set(Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                60000, createClockTickIntent(context));

        Class<? extends BroadcastReceiver> receiver = systemEventReceiver();
        if (receiver != null) {
            PackageManager pm = context.getApplicationContext().getPackageManager();
            ComponentName component = new ComponentName(context, receiver);
            pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createClockTickIntent(context));
        
        Class<? extends BroadcastReceiver> receiver = systemEventReceiver();
        if (receiver != null) {
            PackageManager pm = context.getApplicationContext().getPackageManager();
            ComponentName component = new ComponentName(context, receiver);
            pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (WIDGET_DATA_CHANGED_ACTION.equals(intent.getAction())
                || mClockTickAction.equals(intent.getAction())) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isScreenOn()) {
                onClockTick(context);
            }
        }
    }

    protected abstract void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId);

    protected abstract void onClockTick(Context context);

    /**
     * Returns a {@link BroadcastReceiver} that listens to system events for this widget.
     * The receiver must be registered in the manifest file. It must be disabled by default.
     * It will be enabled only if a widget is used.
     * @return
     */
    protected abstract Class<? extends BroadcastReceiver> systemEventReceiver();

    private PendingIntent createClockTickIntent(Context context) {
        Intent intent = new Intent(mClockTickAction);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }
}
