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

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class WeatherWidgetProvider extends ClockWidgetProvider {

    public static final String CLOCK_TICK_ACTION = "ch.corten.aha.worldclock.WEATHER_WIDGET_TICK";

    public WeatherWidgetProvider() {
        super(CLOCK_TICK_ACTION);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        // enable weather update service
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                AlarmManager.INTERVAL_HOUR, createWeatherUpdateIntent(context));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        // disable weather update service
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(createWeatherUpdateIntent(context));
    }

    private PendingIntent createWeatherUpdateIntent(Context context) {
        Intent service = new Intent(context, UpdateWeatherService.class);
        service.putExtra(UpdateWeatherService.BACKGROUND_UPDATE, true);
        return PendingIntent.getService(context, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void updateAppWidget(Context context,
            AppWidgetManager appWidgetManager, int appWidgetId) {
        // Set up the intent that starts the WeatherWidgetService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, WeatherWidgetService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        rv.setRemoteAdapter(appWidgetId, R.id.grid_view, intent);
        rv.setEmptyView(R.id.grid_view, R.id.empty_view);

        Intent i = new Intent(context, WorldClockActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);
        rv.setPendingIntentTemplate(R.id.grid_view, pendingIntent);
        rv.setOnClickPendingIntent(R.id.empty_view, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    @Override
    protected void onClockTick(Context context) {
        Calendar cal = Calendar.getInstance();
        // update on the hour
        if (cal.get(Calendar.MINUTE) == 0) {
            Clocks.updateOrder(context);
        }
        final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        final ComponentName cn = new ComponentName(context, WeatherWidgetProvider.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.grid_view);
    }
}
