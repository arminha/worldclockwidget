package ch.corten.aha.worldclock.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WorldClockDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_CREATE =
            "create table clocks (_id integer primary key autoincrement, "
                    + "timezone_id text not null, "
                    + "city text not null, "
                    + "area text not null, "
                    + "time_diff integer not null, "
                    + "use_in_widget integer not null default 1);";
    
    private static final String DATABASE_UPDATE_2 = 
            "alter table clocks add column use_in_widget integer not null default 1";
    
    private static final String DATABASE_UPDATE_3 =
            "alter table clocks add column latitude real not null default 0; " +
            "alter table clocks add column longitude real not null default 0;";

    private static final String DATABASE_NAME = "worldclock";
    private static final int DATABASE_VERSION = 3;

    public WorldClockDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        db.execSQL(DATABASE_UPDATE_3);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL(DATABASE_UPDATE_2);
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL(DATABASE_UPDATE_3);
        }
    }
}
