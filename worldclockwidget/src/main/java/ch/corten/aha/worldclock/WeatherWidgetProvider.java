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

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WeatherWidgetProvider extends AbstractWeatherWidgetProvider {

    static {
        registerWeatherWidget(WeatherWidgetProvider.class);
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
        // update on the hour
        final long minutes = System.currentTimeMillis() / (60000);
        if (minutes % 60 == 0) {
            Clocks.updateOrder(context);
        }
        final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        final ComponentName cn = new ComponentName(context, WeatherWidgetProvider.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.grid_view);
    }
}
