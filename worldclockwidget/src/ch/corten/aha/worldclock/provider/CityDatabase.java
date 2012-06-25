package ch.corten.aha.worldclock.provider;

import java.io.BufferedReader;
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

public class CityDatabase extends SQLiteOpenHelper {

    private static final String CITY_DATA_CSV = "city_data.csv";

    private static final String DATABASE_CREATE =
            "create table cities (_id integer primary key, "
                    + "name text not null, "
                    + "asciiname text not null, "
                    + "latitude real not null, "
                    + "longitude real not null, "
                    + "country_code text not null, "
                    + "timezone_id text not null);";
    
    private static final String DATABASE_NAME = "cities";
    private static final int DATABASE_VERSION = 1;

    private Context mContext;
    
    public CityDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
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
        final int countryColumn = ih.getColumnIndex(Cities.COUNTRY_CODE);
        final int timezoneColumn = ih.getColumnIndex(Cities.TIMEZONE_ID);

        try {
            // temporarily disable locking 
            db.setLockingEnabled(false);
            AssetManager am = mContext.getAssets();
            InputStream stream = am.open(CITY_DATA_CSV, AssetManager.ACCESS_STREAMING);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String line = in.readLine();
            while (line != null) {
                // insert data set
                ih.prepareForInsert();
                
                String[] rawValues = p.split(line, -1);
                ih.bind(idColumn, Long.parseLong(rawValues[0]));
                ih.bind(nameColumn, rawValues[1]);
                ih.bind(asciiNameColumn, rawValues[2]);
                ih.bind(latitudeColumn, Float.parseFloat(rawValues[3]));
                ih.bind(longitudeColumn, Float.parseFloat(rawValues[4]));
                ih.bind(countryColumn, rawValues[5]);
                ih.bind(timezoneColumn,rawValues[6]);
                ih.execute();
                
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
        // no upgrades yet
    }
}
