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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import ch.corten.aha.worldclock.provider.WorldClock.Cities;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class CityDatabase extends SQLiteOpenHelper {

    private static final String CITY_DATA_CSV = "city_data.csv";

    private static final String DATABASE_CREATE =
            "create table cities (_id integer primary key, "
                    + "name text not null, "
                    + "asciiname text not null, "
                    + "latitude real not null, "
                    + "longitude real not null, "
                    + "country text not null, "
                    + "timezone_id text not null);";

    private static final String DROP_TABLE = "drop table if exists cities";

    private static final String DATABASE_NAME = "cities";
    private static final int DATABASE_VERSION = 17;

    private Context mContext;
    private boolean mNeedsVacuum;

    public CityDatabase(Context context) {
        super(context, getName(context), null, DATABASE_VERSION);
        cleanupOldDataBase(context);
        mContext = context;
    }

    private static String getName(Context context) {
        return context.getCacheDir().getPath() + File.separator + DATABASE_NAME;
    }

    private static void cleanupOldDataBase(Context context) {
        File oldDb = context.getDatabasePath(DATABASE_NAME);
        if (oldDb.exists() && !oldDb.delete()) {
            Log.e("CityDatabase", "Unable to delete old database");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);

        insertData(db);
    }

    private void insertData(SQLiteDatabase db) {
        Pattern p = Pattern.compile("\t");
        InsertHelper ih = new InsertHelper(db, Cities.TABLE_NAME);
        final int idColumn = ih.getColumnIndex(Cities._ID);
        final int nameColumn = ih.getColumnIndex(Cities.NAME);
        final int asciiNameColumn = ih.getColumnIndex(Cities.ASCII_NAME);
        final int latitudeColumn = ih.getColumnIndex(Cities.LATITUDE);
        final int longitudeColumn = ih.getColumnIndex(Cities.LONGITUDE);
        final int countryColumn = ih.getColumnIndex(Cities.COUNTRY);
        final int timezoneColumn = ih.getColumnIndex(Cities.TIMEZONE_ID);

        try {
            // temporarily disable locking
            db.setLockingEnabled(false);
            AssetManager am = mContext.getAssets();
            InputStream stream = am.open(CITY_DATA_CSV, AssetManager.ACCESS_STREAMING);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line = in.readLine();
            long id = 1;
            while (line != null) {
                // insert data set
                ih.prepareForInsert();

                // CHECKSTYLE IGNORE MagicNumberCheck
                String[] rawValues = p.split(line, -1);
                ih.bind(idColumn, id++);
                ih.bind(nameColumn, rawValues[0]);
                ih.bind(asciiNameColumn, rawValues[1]);
                ih.bind(latitudeColumn, Double.parseDouble(rawValues[2]));
                ih.bind(longitudeColumn, Double.parseDouble(rawValues[3]));
                ih.bind(countryColumn, rawValues[4]);
                ih.bind(timezoneColumn, rawValues[5]);
                ih.execute();
                // CHECKSTYLE END IGNORE MagicNumberCheck

                // next data set
                line = in.readLine();
            }
            in.close();
        } catch (IOException e) {
            Log.e("CityDatabase", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            ih.close();
            // enable locking again!
            db.setLockingEnabled(true);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        db.execSQL(DATABASE_CREATE);
        insertData(db);
        mNeedsVacuum = true;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (mNeedsVacuum) {
            mNeedsVacuum = false;
            db.execSQL("VACUUM");
        }
        super.onOpen(db);
    }
}
