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

package ch.corten.aha.worldclock.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.joda.time.DateTimeZone;

import ch.corten.aha.worldclock.TimeZoneInfo;
import ch.corten.aha.worldclock.weather.WeatherObservation;

public final class WorldClock {

    private WorldClock() {
    }

    public static final String AUTHORITY = "ch.corten.aha.worldclock.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static class Clocks implements BaseColumns {
        public static enum MoveTarget {
            UP,
            DOWN
        }

        static final String TABLE_NAME = "clocks";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;

        public static final String _ID = BaseColumns._ID;
        public static final String TIMEZONE_ID = "timezone_id";
        public static final String CITY = "city";
        public static final String AREA = "area";
        public static final String TIME_DIFF = "time_diff";
        public static final String USE_IN_WIDGET = "use_in_widget";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";

        public static final String ORDER_KEY = "order_key";

        /*
         * Add columns for weather.
         */
        public static final String TEMPERATURE = "temperature";
        public static final String WIND_SPEED = "wind_speed";
        public static final String WIND_DIRECTION = "wind_direction";
        public static final String HUMIDITY = "humidity";
        public static final String WEATHER_CONDITION = "weather_condition";
        public static final String CONDITION_CODE = "condition_code";
        public static final String LAST_UPDATE = "last_update";

        /**
         * Create a new clock.
         * 
         * @param timeZoneId
         *            the time zone id
         * @param city
         *            the city
         * @param area
         *            the area
         * @param timeDiff
         *            the time difference to GMT in minutes
         */
        public static void addClock(Context context, String timeZoneId,
                String city, String area, int timeDiff, double latitude,
                double longitude) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(TIMEZONE_ID, timeZoneId);
            initialValues.put(CITY, city);
            initialValues.put(AREA, area);
            initialValues.put(TIME_DIFF, timeDiff);
            initialValues.put(LATITUDE, latitude);
            initialValues.put(LONGITUDE, longitude);

            ContentResolver cr = context.getContentResolver();
            long orderKey;
            Cursor c = cr.query(CONTENT_URI, new String[] {"MAX(order_key) as max_order_key"}, null, null, null);
            try {
                if (c.moveToFirst()) {
                    orderKey = c.getLong(c.getColumnIndex("max_order_key")) + 1;
                } else {
                    orderKey = 0;
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            initialValues.put(ORDER_KEY, orderKey);

            cr.insert(CONTENT_URI, initialValues);
        }

        public static void move(Context context, long id, MoveTarget target) {
            ContentResolver cr = context.getContentResolver();
            long orderKey = getOrderKey(cr, id);
            long otherOrderKey;
            long otherId;

            String selection;
            String sortOrder;
            switch (target) {
            case DOWN:
                selection = ORDER_KEY + " > ?";
                sortOrder = ORDER_KEY + " ASC";
                break;
            case UP:
                selection = ORDER_KEY + " < ?";
                sortOrder = ORDER_KEY + " DESC";
                break;
            default:
                throw new RuntimeException("unknown target: " + target);
            }
            Cursor c = cr.query(CONTENT_URI, new String[] {_ID, ORDER_KEY}, selection , new String[] {Long.toString(orderKey)}, sortOrder);
            try {
                if (c.moveToFirst()) {
                    otherId = c.getLong(c.getColumnIndex(_ID));
                    otherOrderKey = c.getLong(c.getColumnIndex(ORDER_KEY));
                } else {
                    // move not possible
                    return;
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            setOrderKey(cr, id, otherOrderKey);
            setOrderKey(cr, otherId, orderKey);
        }

        private static void setOrderKey(ContentResolver cr, long id, long orderKey) {
            ContentValues values = new ContentValues();
            values.put(ORDER_KEY, orderKey);
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
            cr.update(uri, values, null, null);
        }

        private static long getOrderKey(ContentResolver cr, long id) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
            Cursor c = cr.query(uri, new String[] {ORDER_KEY}, null, null, null);
            try {
                c.moveToNext();
                return c.getLong(c.getColumnIndex(ORDER_KEY));
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        public static boolean updateWeather(Context context, long id, WeatherObservation obs) {
            ContentValues values = new ContentValues();
            values.put(TEMPERATURE, obs.getTemperature());
            values.put(WIND_SPEED, obs.getWindSpeed());
            values.put(WIND_DIRECTION, obs.getWindDirection());
            values.put(HUMIDITY, obs.getHumidity());
            values.put(WEATHER_CONDITION, obs.getWeatherCondition());
            values.put(CONDITION_CODE, obs.getConditionCode());
            values.put(LAST_UPDATE, obs.getUpdateTime().getTime());

            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
            int count = context.getContentResolver().update(uri, values, null, null);
            return count > 0;
        }

        public static boolean updateOrder(Context context) {
            int count = 0;
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(CONTENT_URI, new String[] {_ID, TIMEZONE_ID, TIME_DIFF}, null, null, _ID);
            if (c != null) {
                try {
                    while (c.moveToNext()) {
                        String timeZoneId = c.getString(c.getColumnIndex(TIMEZONE_ID));
                        int storedDiff = c.getInt(c.getColumnIndex(TIME_DIFF));
                        DateTimeZone tz = DateTimeZone.forID(timeZoneId);
                        int diff = TimeZoneInfo.getTimeDifference(tz);
                        if (storedDiff != diff) {
                            // update entry
                            long id = c.getLong(c.getColumnIndex(_ID));
                            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
                            ContentValues values = new ContentValues();
                            values.put(TIME_DIFF, diff);
                            count += cr.update(uri, values, null, null);
                        }
                    }
                } finally {
                    c.close();
                }
            }
            return count > 0;
        }

        public static Cursor widgetList(Context context, String[] projection, boolean autoSort) {
            String sortOrder = autoSort
                    ? Clocks.TIME_DIFF + " ASC, " + Clocks.CITY + " ASC"
                    : Clocks.ORDER_KEY + " ASC";
            return context.getContentResolver().query(Clocks.CONTENT_URI,
                    projection, Clocks.USE_IN_WIDGET + " = 1", null, sortOrder);
        }
    }

    public static class Cities {
        static final String TABLE_NAME = "cities";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;

        public static final String _ID = BaseColumns._ID;
        public static final String NAME = "name";
        public static final String ASCII_NAME = "asciiname";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        /**
         * Country name.
         */
        public static final String COUNTRY = "country";
        public static final String TIMEZONE_ID = "timezone_id";
    }
}
