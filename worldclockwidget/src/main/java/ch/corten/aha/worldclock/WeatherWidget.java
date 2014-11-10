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

import java.text.DateFormat;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;

import ch.corten.aha.widget.RemoteViewUtil;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

public final class WeatherWidget {

    private WeatherWidget() {
    }

    private static final boolean SANS_JELLY_BEAN_MR1 = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1;

    private static final String[] PROJECTION = {
        Clocks._ID,
        Clocks.TIMEZONE_ID,
        Clocks.CITY,
        Clocks.TEMPERATURE,
        Clocks.CONDITION_CODE,
        Clocks.WEATHER_CONDITION,
        Clocks.LATITUDE,
        Clocks.LONGITUDE
    };

    public static Cursor getData(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoSort = prefs.getBoolean(context.getString(R.string.auto_sort_clocks_key), true);
        return Clocks.widgetList(context, PROJECTION, autoSort);
    }

    public static void updateItemView(Context context, Cursor cursor, RemoteViews rv, DateFormat timeFormat) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean customColors = prefs.getBoolean(context.getString(R.string.use_custom_colors_key), false);

        rv.setTextViewText(R.id.city_text, cursor.getString(cursor.getColumnIndex(Clocks.CITY)));

        String id = cursor.getString(cursor.getColumnIndex(Clocks.TIMEZONE_ID));
        long now = DateTimeUtils.currentTimeMillis();
        DateTimeZone tz = DateTimeZone.forID(id);
        if (SANS_JELLY_BEAN_MR1) {
            rv.setTextViewText(R.id.time_text, TimeZoneInfo.showTimeWithOptionalWeekDay(tz, now, timeFormat));
        } else {
            TimeZone javaTimeZone = TimeZoneInfo.convertToJavaTimeZone(tz, now);
            RemoteViewUtil.setTextClockTimeZone(rv, R.id.time_text, javaTimeZone.getID());
            rv.setTextViewText(R.id.weekday_text, TimeZoneInfo.showDifferentWeekday(tz, now));
        }

        rv.setTextViewText(R.id.condition_text, cursor
                .getString(cursor.getColumnIndex(Clocks.WEATHER_CONDITION)));

        String temperature = BindHelper.getTemperature(context, cursor, false);
        rv.setTextViewText(R.id.temp_text, temperature);

        int condCode = cursor.getInt(cursor.getColumnIndex(Clocks.CONDITION_CODE));
        double lat = cursor.getDouble(cursor.getColumnIndex(Clocks.LATITUDE));
        double lon = cursor.getDouble(cursor.getColumnIndex(Clocks.LONGITUDE));
        if (!customColors) {
            rv.setImageViewResource(R.id.condition_image, WeatherIcons.getIcon(condCode, lon, lat));
        }

        if (customColors) {
            int color = prefs.getInt(context.getString(R.string.background_color_key), Color.BLACK);
            RemoteViewUtil.setBackgroundColor(rv, R.id.widget_item, color);

            int foreground = prefs.getInt(context.getString(R.string.foreground_color_key), Color.WHITE);
            rv.setTextColor(R.id.city_text, foreground);
            rv.setTextColor(R.id.time_text, foreground);
            rv.setTextColor(R.id.condition_text, foreground);
            rv.setTextColor(R.id.temp_text, foreground);
            if (!SANS_JELLY_BEAN_MR1) {
                rv.setTextColor(R.id.weekday_text, foreground);
            }

            int res = WeatherIcons.getIcon(condCode, lon, lat);
            if (foreground != Color.WHITE) {
                Drawable drawable = context.getResources().getDrawable(res);
                Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
                drawable.setColorFilter(foreground, Mode.MULTIPLY);
                Canvas canvas = new Canvas(bmp);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                drawable.setColorFilter(null);
                rv.setImageViewBitmap(R.id.condition_image, bmp);
            } else {
                rv.setImageViewResource(R.id.condition_image, WeatherIcons.getIcon(condCode, lon, lat));
            }
        } else {
            RemoteViewUtil.setBackground(rv, R.id.widget_item, R.drawable.appwidget_dark_bg);

            int defaultColor = 0xffbebebe;
            rv.setTextColor(R.id.city_text, Color.WHITE);
            rv.setTextColor(R.id.time_text, defaultColor);
            rv.setTextColor(R.id.condition_text, defaultColor);
            rv.setTextColor(R.id.temp_text, Color.WHITE);
            if (!SANS_JELLY_BEAN_MR1) {
                rv.setTextColor(R.id.weekday_text, defaultColor);
            }
        }
    }
}
