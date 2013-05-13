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

package ch.corten.aha.worldclock.weather.yahoo;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class WoeidCache {

    private static final String TABLE = "woeid_cache";
    private static final int VERSION = 3;
    private final Storage mStorage;

    public WoeidCache(Context context) {
        mStorage = new Storage(context);
    }

    public String get(double latitude, double longitude) {
        SQLiteDatabase db = mStorage.getDatabase();
        String[] selectionArgs = new String[] { Double.toString(latitude), Double.toString(longitude) };
        Cursor c = db.query(TABLE, new String[] { "woeid" }, "latitude = ? and longitude = ?", selectionArgs, null, null, null);
        try {
            if (c.moveToFirst()) {
                final String woeid = c.getString(c.getColumnIndex("woeid"));
                return PlaceFinderService.fixInvalidWoeids(latitude, longitude, woeid);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }

    public void put(double latitude, double longitude, String woeid) {
        SQLiteDatabase db = mStorage.getDatabase();
        ContentValues values = new ContentValues();
        values.put("woeid", woeid);
        if (get(latitude, longitude) != null) {
            String[] whereArgs = new String[] { Double.toString(latitude), Double.toString(longitude) };
            db.update(TABLE, values, "latitude = ? and longitude = ?", whereArgs);
        } else {
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            db.insert(TABLE, null, values);
        }
    }

    public void close() {
        mStorage.close();
    }

    private static class Storage {
        private static final String FILENAME = "woeid";

        private static final String DATABASE_CREATE =
                "create table woeid_cache (latitude real not null, "
                        + "longitude real not null, "
                        + "woeid text not null);";
        private static final String CLEAR_DATABASE = "delete from woeid_cache;";

        private SQLiteDatabase mDatabase;
        private final Context mContext;

        public Storage(Context context) {
            mContext = context;
        }

        public synchronized SQLiteDatabase getDatabase() {
            if (mDatabase != null) {
                if (!mDatabase.isOpen()) {
                    mDatabase = null;
                }
            }

            if (mDatabase == null) {
                String path = mContext.getCacheDir().getPath() + File.separator + FILENAME;
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
                if (db.getVersion() == 0) {
                    onCreate(db);
                    db.setVersion(VERSION);
                } else if (db.getVersion() < VERSION) {
                    onUpdate(db, db.getVersion(), VERSION);
                    db.setVersion(VERSION);
                }
                mDatabase = db;
            }
            return mDatabase;
        }

        public synchronized void close() {
            if (mDatabase != null && mDatabase.isOpen()) {
                mDatabase.close();
                mDatabase = null;
            }
        }

        protected void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        public void onUpdate(SQLiteDatabase db, int version2, int version3) {
            db.execSQL(CLEAR_DATABASE);
        }
    }
}
