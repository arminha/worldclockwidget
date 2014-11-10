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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class WorldClockDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_CREATE =
            "create table clocks (_id integer primary key autoincrement, "
                    + "timezone_id text not null, "
                    + "city text not null, "
                    + "area text not null, "
                    + "time_diff integer not null, "
                    + "use_in_widget integer not null default 1);";

    private static final String DATABASE_UPDATE_2 =
            "alter table clocks add column use_in_widget integer not null default 1";

    private static final String[] DATABASE_UPDATE_3 = {
            "alter table clocks add column latitude real not null default 0;",
            "alter table clocks add column longitude real not null default 0;"
    };

    /*
     * Add columns for weather
     */
    private static final String[] DATABASE_UPDATE_4 = {
        "alter table clocks add column temperature real;",
        "alter table clocks add column wind_speed real;",
        "alter table clocks add column wind_direction text;",
        "alter table clocks add column humidity real;",
        "alter table clocks add column weather_condition text;",
        "alter table clocks add column condition_code integer;",
        "alter table clocks add column last_update integer default 0;",
    };

    /*
     * Add column for manual sort order
     */
    private static final String DATABASE_UPDATE_5 =
            "alter table clocks add column order_key integer not null default 0";
    private static final String DATABASE_UPDATE_5_INITIAL_VALUES =
            "update clocks set order_key = _id";

    private static final String DATABASE_NAME = "worldclock";
    private static final int DATABASE_VERSION = 5;

    public WorldClockDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        for (String stmt : DATABASE_UPDATE_3) {
            db.execSQL(stmt);
        }
        for (String stmt : DATABASE_UPDATE_4) {
            db.execSQL(stmt);
        }
        db.execSQL(DATABASE_UPDATE_5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL(DATABASE_UPDATE_2);
        }
        if (oldVersion < 3 && newVersion >= 3) {
            for (String stmt : DATABASE_UPDATE_3) {
                db.execSQL(stmt);
            }
        }
        if (oldVersion < 4 && newVersion >= 4) {
            for (String stmt : DATABASE_UPDATE_4) {
                db.execSQL(stmt);
            }
        }
        if (oldVersion < 5 && newVersion >= 5) {
            db.execSQL(DATABASE_UPDATE_5);
            // set initial values
            db.execSQL(DATABASE_UPDATE_5_INITIAL_VALUES);
        }
    }
}
