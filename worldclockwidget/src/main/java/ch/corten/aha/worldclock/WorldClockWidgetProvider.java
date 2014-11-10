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

package ch.corten.aha.worldclock;

import java.text.DateFormat;
import java.util.TimeZone;

import ch.corten.aha.widget.RemoteViewUtil;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;

public class WorldClockWidgetProvider extends ClockWidgetProvider {
    private static final boolean SANS_JELLY_BEAN_MR1 = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1;

    static {
        registerClockWidget(WorldClockWidgetProvider.class);
    }

    @Override
    protected void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        updateAppWidgetStatic(context, appWidgetManager, appWidgetId);
    }

    private static void updateAppWidgetStatic(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create an Intent to launch WorldClockActivity
        Intent intent = new Intent(context, WorldClockActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.world_clock_widget);
        views.setOnClickPendingIntent(R.id.app_widget, pendingIntent);

        // update view
        updateViews(context, views);

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static final String[] PROJECTION = {
        Clocks.TIMEZONE_ID,
        Clocks.CITY
    };

    private static final int[] CITY_IDS = {
        R.id.city_text1,
        R.id.city_text2,
        R.id.city_text3,
        R.id.city_text4,
    };

    private static final int[] TIME_IDS = {
        R.id.time_text1,
        R.id.time_text2,
        R.id.time_text3,
        R.id.time_text4,
    };

    private static void updateViews(Context context, RemoteViews views) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoSort = prefs.getBoolean(context.getString(R.string.auto_sort_clocks_key), true);
        Cursor cursor = Clocks.widgetList(context, PROJECTION, autoSort);

        try {
            int n = 0;
            DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
            long now = DateTimeUtils.currentTimeMillis();
            final int maxEntries = context.getResources().getInteger(R.integer.worldclock_widget_max_entries);
            while (cursor.moveToNext() && n < CITY_IDS.length
                    && n < maxEntries) {
                String id = cursor.getString(cursor.getColumnIndex(Clocks.TIMEZONE_ID));
                String city = cursor.getString(cursor.getColumnIndex(Clocks.CITY));
                views.setTextViewText(CITY_IDS[n], city);
                DateTimeZone tz = DateTimeZone.forID(id);
                if (SANS_JELLY_BEAN_MR1) {
                    views.setTextViewText(TIME_IDS[n], TimeZoneInfo.formatDate(df, tz, now));
                } else {
                    TimeZone javaTimeZone = TimeZoneInfo.convertToJavaTimeZone(tz, now);
                    views.setViewVisibility(TIME_IDS[n], View.VISIBLE);
                    RemoteViewUtil.setTextClockTimeZone(views, TIME_IDS[n], javaTimeZone.getID());
                }
                n++;
            }
            int showEmptyText = (n == 0) ? View.VISIBLE : View.INVISIBLE;
            views.setViewVisibility(R.id.empty_text, showEmptyText);
            for (; n < CITY_IDS.length; n++) {
                views.setTextViewText(CITY_IDS[n], "");
                if (SANS_JELLY_BEAN_MR1) {
                    views.setTextViewText(TIME_IDS[n], "");
                } else {
                    views.setViewVisibility(TIME_IDS[n], View.INVISIBLE);
                }
            }
            boolean customColors = prefs.getBoolean(context.getString(R.string.use_custom_colors_key), false);
            int textColor = Color.WHITE;
            if (customColors) {
                int color = prefs.getInt(context.getString(R.string.background_color_key), Color.BLACK);
                RemoteViewUtil.setBackgroundColor(views, R.id.app_widget, color);
                textColor = prefs.getInt(context.getString(R.string.foreground_color_key), Color.WHITE);
            } else {
                RemoteViewUtil.setBackground(views, R.id.app_widget, R.drawable.appwidget_dark_bg);
            }
            for (int i = 0; i < CITY_IDS.length; i++) {
                views.setTextColor(CITY_IDS[i], textColor);
                views.setTextColor(TIME_IDS[i], textColor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onClockTick(Context context) {
        Intent service = new Intent(context, WorldClockWidgetService.class);
        context.startService(service);
    }

    static void updateTime(Context context) {
        // update on the hour
        final long minutes = System.currentTimeMillis() / (60000);
        if (minutes % 60 == 0) {
            Clocks.updateOrder(context);
        }
        // Get the widget manager and ids for this widget provider, then call the shared
        // clock update method.
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WorldClockWidgetProvider.class.getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int appWidgetID: ids) {
            updateAppWidgetStatic(context, appWidgetManager, appWidgetID);
        }
    }
}
