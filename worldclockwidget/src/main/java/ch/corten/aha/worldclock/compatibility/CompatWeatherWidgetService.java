/*
 * Copyright (C) 2012 - 2014  Armin Häberling
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

import ch.corten.aha.worldclock.R;
import ch.corten.aha.worldclock.WeatherWidget;
import ch.corten.aha.worldclock.WorldClockActivity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;

public class CompatWeatherWidgetService extends IntentService {

    public static final String LAYOUT = "layout";
    public static final String SIZE = "size";
    public static final String APP_WIDGET_ID = "app_widget_id";

    public CompatWeatherWidgetService() {
        super("CompatWeatherWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int appWidgetId = intent.getIntExtra(APP_WIDGET_ID, 0);
        int size = intent.getIntExtra(SIZE, 1);
        int layout = intent.getIntExtra(LAYOUT, CompatWeatherWidgetProvider.LAYOUT_ONE_COLUMN);

        RemoteViews rv = updateViews(size, layout);

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        awm.updateAppWidget(appWidgetId, rv);
    }

    protected RemoteViews updateViews(int size, int layout) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, WorldClockActivity.class), 0);
        RemoteViews rv = getRemoteViews(layout);
        int[] columns = getColumns(layout);
        for (int column : columns) {
            rv.removeAllViews(column);
        }
        rv.setOnClickPendingIntent(R.id.empty_view, pendingIntent);
        Cursor c = WeatherWidget.getData(this);
        try {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
            if (c.moveToNext()) {
                // we have an item
                rv.setViewVisibility(R.id.empty_view, View.GONE);
                rv.setViewVisibility(getGridViewId(layout), View.VISIBLE);
                int i = 0;

                do {
                    RemoteViews itemView = new RemoteViews(this.getPackageName(), R.layout.weather_widget_item_compat);
                    WeatherWidget.updateItemView(this, c, itemView, timeFormat);
                    itemView.setOnClickPendingIntent(R.id.widget_item, pendingIntent);
                    rv.addView(columns[i % columns.length], itemView);
                    i++;
                } while (c.moveToNext() && i < size);
            } else {
                rv.setViewVisibility(R.id.empty_view, View.VISIBLE);
                rv.setViewVisibility(getGridViewId(layout), View.GONE);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return rv;
    }

    private int[] getColumns(int layout) {
        switch (layout) {
        case CompatWeatherWidgetProvider.LAYOUT_ONE_COLUMN:
            return new int[] {R.id.column_one};
        case CompatWeatherWidgetProvider.LAYOUT_TWO_COLUMNS:
            return new int[] {R.id.column_one, R.id.column_two};
        default:
            throw new RuntimeException("Invalid layout: " + layout);
        }
    }

    private RemoteViews getRemoteViews(int layout) {
        switch (layout) {
        case CompatWeatherWidgetProvider.LAYOUT_ONE_COLUMN:
            return new RemoteViews(this.getPackageName(), R.layout.weather_widget_compat);
        case CompatWeatherWidgetProvider.LAYOUT_TWO_COLUMNS:
            return new RemoteViews(this.getPackageName(), R.layout.weather_widget_compat_two);
        default:
            throw new RuntimeException("Invalid layout: " + layout);
        }
    }

    private int getGridViewId(int layout) {
        switch (layout) {
        case CompatWeatherWidgetProvider.LAYOUT_ONE_COLUMN:
            return R.id.column_one;
        case CompatWeatherWidgetProvider.LAYOUT_TWO_COLUMNS:
            return R.id.grid_view;
        default:
            throw new RuntimeException("Invalid layout: " + layout);
        }
    }

}
