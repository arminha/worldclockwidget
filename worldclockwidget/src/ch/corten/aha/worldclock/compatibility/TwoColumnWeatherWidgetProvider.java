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

import ch.corten.aha.worldclock.R;
import ch.corten.aha.worldclock.WeatherWidget;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;

public class TwoColumnWeatherWidgetProvider extends CompatWeatherWidgetProvider {
    @Override
    protected RemoteViews updateViews(Context context, PendingIntent pendingIntent) {
        int[] columns = new int[] { R.id.column_one, R.id.column_two };
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.weather_widget_compat_two);
        for (int column : columns) {
            rv.removeAllViews(column);
        }
        rv.setOnClickPendingIntent(R.id.empty_view, pendingIntent);
        Cursor c = WeatherWidget.getData(context);
        try {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            if (c.moveToNext()) {
                // we have an item
                rv.setViewVisibility(R.id.empty_view, View.GONE);
                rv.setViewVisibility(R.id.grid_view, View.VISIBLE);
                int i = 0;

                do {
                    RemoteViews itemView = new RemoteViews(context.getPackageName(), R.layout.weather_widget_item_compat);
                    WeatherWidget.updateItemView(context, c, itemView, timeFormat);
                    itemView.setOnClickPendingIntent(R.id.widget_item, pendingIntent);
                    rv.addView(columns[i], itemView);
                    i = (i + 1) % columns.length;
                } while (c.moveToNext());
            } else {
                rv.setViewVisibility(R.id.empty_view, View.VISIBLE);
                rv.setViewVisibility(R.id.grid_view, View.GONE);
            }
        } finally {
            c.close();
        }
        return rv;
    }
}
