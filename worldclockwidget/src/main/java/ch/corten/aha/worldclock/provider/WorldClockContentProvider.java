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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import ch.corten.aha.worldclock.provider.WorldClock.Cities;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

public class WorldClockContentProvider extends ContentProvider {

    private WorldClockDatabase mClockDbHelper;
    private CityDatabase mCityDbHelper;

    private static final int CLOCKS = 1;
    private static final int CLOCKS_ITEM = 2;
    private static final int CITIES = 3;
    private static final int CITIES_ITEM = 4;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(WorldClock.AUTHORITY, Clocks.TABLE_NAME, CLOCKS);
        URI_MATCHER.addURI(WorldClock.AUTHORITY, Clocks.TABLE_NAME + "/#", CLOCKS_ITEM);
        URI_MATCHER.addURI(WorldClock.AUTHORITY, Cities.TABLE_NAME, CITIES);
        URI_MATCHER.addURI(WorldClock.AUTHORITY, Cities.TABLE_NAME + "/#", CITIES_ITEM);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
        case CLOCKS:
            break;
        case CLOCKS_ITEM:
            selection = "_ID = " + uri.getLastPathSegment();
            break;
        case CITIES:
        case CITIES_ITEM:
            throw citiesReadOnly();
        default:
            throw invalidUri(uri);
        }

        SQLiteDatabase db = getClockDbHelper().getWritableDatabase();
        int deleted = db.delete(Clocks.TABLE_NAME, selection, selectionArgs);
        if (deleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleted;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
        case CLOCKS:
            return Clocks.CONTENT_TYPE;
        case CLOCKS_ITEM:
            return Clocks.CONTENT_ITEM_TYPE;
        case CITIES:
            return Cities.CONTENT_TYPE;
        case CITIES_ITEM:
            return Cities.CONTENT_ITEM_TYPE;
        default:
            throw invalidUri(uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (URI_MATCHER.match(uri)) {
        case CLOCKS:
            break;
        case CITIES:
            throw citiesReadOnly();
        default:
            throw invalidUri(uri);
        }

        SQLiteDatabase db = getClockDbHelper().getWritableDatabase();
        long id = db.insert(Clocks.TABLE_NAME, null, values);
        Uri insertUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(insertUri, null);
        return insertUri;
    }

    private static IllegalArgumentException invalidUri(Uri uri) {
        return new IllegalArgumentException("URI not recognized: " + uri.toString());
    }

    private static IllegalArgumentException citiesReadOnly() {
        return new IllegalArgumentException("Cannot write cities, they are read-only.");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        String table;
        SQLiteOpenHelper helper;
        int match = URI_MATCHER.match(uri);
        switch (match) {
        case CLOCKS:
        case CLOCKS_ITEM:
            table = Clocks.TABLE_NAME;
            helper = getClockDbHelper();
            break;
        case CITIES:
        case CITIES_ITEM:
            table = Cities.TABLE_NAME;
            helper = getCityDbHelper();
            break;
        default:
            throw invalidUri(uri);
        }

        switch (match) {
        case CLOCKS:
        case CITIES:
            if (TextUtils.isEmpty(sortOrder)) {
                sortOrder = "_ID ASC";
            }
            break;
        case CITIES_ITEM:
        case CLOCKS_ITEM:
            selection = "_ID = " + uri.getLastPathSegment();
            break;
        default:
            throw invalidUri(uri);
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
        case CLOCKS:
            break;
        case CLOCKS_ITEM:
            selection = "_ID = " + uri.getLastPathSegment();
            break;
        case CITIES:
        case CITIES_ITEM:
            throw citiesReadOnly();
        default:
            throw invalidUri(uri);
        }

        SQLiteDatabase db = getClockDbHelper().getReadableDatabase();
        int updated = db.update(Clocks.TABLE_NAME, values, selection, selectionArgs);
        if (updated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updated;
    }

    private WorldClockDatabase getClockDbHelper() {
        if (mClockDbHelper == null) {
            mClockDbHelper = new WorldClockDatabase(getContext());
        }
        return mClockDbHelper;
    }

    private CityDatabase getCityDbHelper() {
        if (mCityDbHelper == null) {
            mCityDbHelper = new CityDatabase(getContext());
        }
        return mCityDbHelper;
    }
}
