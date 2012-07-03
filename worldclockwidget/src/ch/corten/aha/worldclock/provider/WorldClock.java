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

package ch.corten.aha.worldclock.provider;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

public class WorldClock {
    public static final String AUTHORITY = "ch.corten.aha.worldclock.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    
    public static class Clocks implements BaseColumns {
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
        
        /**
         * Create a new clock.
         * 
         * @param timeZone
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
            
            context.getContentResolver().insert(CONTENT_URI, initialValues);
        }
    }
    
    public static class Cities {
        static final String TABLE_NAME = "cities";
        
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;
        
        /**
         * The geonameid of the city
         */
        public static final String _ID = BaseColumns._ID;
        public static final String NAME = "name";
        public static final String ASCII_NAME = "asciiname";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        /**
         * ISO-3166 2-letter country code, 2 characters
         */
        public static final String COUNTRY_CODE = "country_code";
        public static final String TIMEZONE_ID = "timezone_id";
    }
}
