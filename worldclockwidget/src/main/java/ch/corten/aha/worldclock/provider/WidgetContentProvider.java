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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

public class WidgetContentProvider extends ContentProvider {

    private WorldClockDatabase mClockDbHelper;

    private static final int CLOCKS = 1;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(WorldClock.WIDGET_AUTHORITY, Clocks.TABLE_NAME, CLOCKS);
    }

    private static final Map<String, String> PROJECTION_MAP;
    static {
        Map<String, String> m = new HashMap<>();
        m.put(Clocks._ID, Clocks._ID);
        m.put(Clocks.TIMEZONE_ID, Clocks.TIMEZONE_ID);
        m.put(Clocks.CITY, Clocks.CITY);
        m.put(Clocks.TEMPERATURE, Clocks.TEMPERATURE);
        m.put(Clocks.CONDITION_CODE, Clocks.CONDITION_CODE);
        m.put(Clocks.WEATHER_CONDITION, Clocks.WEATHER_CONDITION);
        m.put(Clocks.LATITUDE, Clocks.LATITUDE);
        m.put(Clocks.LONGITUDE, Clocks.LONGITUDE);
        PROJECTION_MAP = Collections.unmodifiableMap(m);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw readOnly();
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
        case CLOCKS:
            return Clocks.WIDGET_CONTENT_TYPE;
        default:
            throw invalidUri(uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw readOnly();
    }

    private static IllegalArgumentException invalidUri(Uri uri) {
        return new IllegalArgumentException("URI not recognized: " + uri.toString());
    }

    private static IllegalArgumentException readOnly() {
        return new IllegalArgumentException("Cannot write, this provider is read-only.");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case CLOCKS:
                break;
            default:
                throw invalidUri(uri);
        }

        String querySelection = Clocks.USE_IN_WIDGET + " = 1";

        String querySortOrder;
        if ("auto".equals(sortOrder)) {
            querySortOrder = Clocks.TIME_DIFF + " ASC, " + Clocks.CITY + " ASC";
        } else {
            querySortOrder = Clocks.ORDER_KEY + " ASC";
        }

        SQLiteQueryBuilder qb = createQueryBuilder();
        Cursor c = qb.query(getDatabase(), projection, querySelection, null, null, null, querySortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    private SQLiteQueryBuilder createQueryBuilder() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Clocks.TABLE_NAME);
        qb.setProjectionMap(PROJECTION_MAP);
        // TODO from API 14 on we could use strict mode
        // qb.setStrict(true);
        return qb;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw readOnly();
    }

    private SQLiteDatabase getDatabase() {
        if (mClockDbHelper == null) {
            mClockDbHelper = new WorldClockDatabase(getContext());
        }
        return mClockDbHelper.getReadableDatabase();
    }

}
