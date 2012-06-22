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
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class WorldClockContentProvider extends ContentProvider {

    private static final String DATABASE_CREATE =
            "create table clocks (_id integer primary key autoincrement, "
                    + "timezone_id text not null, "
                    + "city text not null, "
                    + "area text not null, "
                    + "time_diff integer not null, "
                    + "use_in_widget integer not null default 1);";
    
    private static final String DATABASE_UPDATE_2 = 
            "alter table clocks add column use_in_widget integer not null default 1";

    private static final String DATABASE_NAME = "worldclock";
    private static final int DATABASE_VERSION = 2;
    
    private DatabaseHelper mDbHelper;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        mUriMatcher.addURI(WorldClock.AUTHORITY, WorldClock.Clocks.TABLE_NAME, 1);
        mUriMatcher.addURI(WorldClock.AUTHORITY, WorldClock.Clocks.TABLE_NAME + "/#", 2);
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        switch (mUriMatcher.match(uri)) {
        case 1:
            table = WorldClock.Clocks.TABLE_NAME;
            break;
        case 2:
            table = WorldClock.Clocks.TABLE_NAME;
            selection = "_ID = " + uri.getLastPathSegment();
            break;
        default:
            throw new RuntimeException("URI not recognized: " + uri.toString());
        }
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(table, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
        case 1:
            return WorldClock.Clocks.CONTENT_TYPE;
        case 2:
            return WorldClock.Clocks.CONTENT_ITEM_TYPE;
        default:
            throw new RuntimeException("URI not recognized: " + uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table;
        switch (mUriMatcher.match(uri)) {
        case 1:
            table = WorldClock.Clocks.TABLE_NAME;
            break;
        default:
            throw new RuntimeException("URI not recognized: " + uri.toString());
        }
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(table, null, values);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        String table;
        switch (mUriMatcher.match(uri)) {
        case 1:
            table = WorldClock.Clocks.TABLE_NAME;
            if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
            break;
        case 2:
            table = WorldClock.Clocks.TABLE_NAME;
            selection = "_ID = " + uri.getLastPathSegment();
            break;
        default:
            throw new RuntimeException("URI not recognized: " + uri.toString());
        }
        
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        String table;
        switch (mUriMatcher.match(uri)) {
        case 1:
            table = WorldClock.Clocks.TABLE_NAME;
            break;
        case 2:
            table = WorldClock.Clocks.TABLE_NAME;
            selection = "_ID = " + uri.getLastPathSegment();
            break;
        default:
            throw new RuntimeException("URI not recognized: " + uri.toString());
        }
        
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        return db.update(table, values, selection, selectionArgs);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2 && newVersion >= 2) {
                db.execSQL(DATABASE_UPDATE_2);
            }
        }
    }
    
}
