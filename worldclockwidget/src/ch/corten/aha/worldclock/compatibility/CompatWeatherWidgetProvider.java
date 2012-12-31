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

package ch.corten.aha.worldclock.compatibility;

import java.text.DateFormat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;
import ch.corten.aha.worldclock.AbstractWeatherWidgetProvider;
import ch.corten.aha.worldclock.R;
import ch.corten.aha.worldclock.WeatherWidget;
import ch.corten.aha.worldclock.WorldClockActivity;

public abstract class CompatWeatherWidgetProvider extends AbstractWeatherWidgetProvider {

    private final int mSize;

    protected CompatWeatherWidgetProvider(int size) {
        mSize = size;
    }

    public int getSize() {
        return mSize;
    }

    @Override
    protected void onClockTick(Context context) {
        // Get the widget manager and ids for this widget provider, then call the shared
        // clock update method.
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int appWidgetID: ids) {
            updateAppWidget(context, appWidgetManager, appWidgetID);
        }
    }

    @Override
    protected void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create an Intent to launch WorldClockActivity
        Intent intent = new Intent(context, WorldClockActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews rv = updateViews(context, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    protected RemoteViews updateViews(Context context, PendingIntent pendingIntent) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.weather_widget_compat);
        rv.removeAllViews(R.id.column_one);
        rv.setOnClickPendingIntent(R.id.empty_view, pendingIntent);
        Cursor c = WeatherWidget.getData(context);
        try {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            if (c.moveToNext()) {
                // we have an item
                rv.setViewVisibility(R.id.empty_view, View.GONE);
                rv.setViewVisibility(R.id.column_one, View.VISIBLE);
                int i = 0;

                do {
                    RemoteViews itemView = new RemoteViews(context.getPackageName(), R.layout.weather_widget_item_compat);
                    WeatherWidget.updateItemView(context, c, itemView, timeFormat);
                    itemView.setOnClickPendingIntent(R.id.widget_item, pendingIntent);
                    rv.addView(R.id.column_one, itemView);
                    i++;
                } while (c.moveToNext() && i < mSize);
            } else {
                rv.setViewVisibility(R.id.empty_view, View.VISIBLE);
                rv.setViewVisibility(R.id.column_one, View.GONE);
            }
        } finally {
            c.close();
        }
        return rv;
    }
}