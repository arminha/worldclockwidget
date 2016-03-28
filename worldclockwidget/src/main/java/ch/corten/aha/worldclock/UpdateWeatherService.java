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

package ch.corten.aha.worldclock;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import ch.corten.aha.worldclock.weather.AndroidWeatherServiceFactory;
import ch.corten.aha.worldclock.weather.WeatherObservation;
import ch.corten.aha.worldclock.weather.WeatherService;

public class UpdateWeatherService extends IntentService {

    private static final String TAG = "UpdateWeatherService";

    public static final String WEATHER_DATA_PURGE_AFTER = "purgeAfter";
    public static final int DEFAULT_WEATHER_DATA_PURGE_AFTER = 7200000; // 2 hour

    public static final String WEATHER_DATA_UPDATE_INTERVAL = "updateInterval";
    public static final int DEFAULT_WEATHER_DATA_UPDATE_INTERVAL = 900000; // 15 minutes

    public static final String BACKGROUND_UPDATE = "backgroundUpdate";

    private static final String[] UPDATE_PROJECTION = new String[] {
        Clocks._ID,
        Clocks.LATITUDE,
        Clocks.LONGITUDE,
        Clocks.LAST_UPDATE
    };

    public UpdateWeatherService() {
        super("UpdateWeather-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean backgroundUpdate = intent.getBooleanExtra(BACKGROUND_UPDATE, false);
        if (backgroundUpdate) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean disableUpdate = prefs.getBoolean(getString(R.string.disable_automatic_update), false);
            if (disableUpdate) {
                return;
            }
        }

        int purgeAfter = intent.getIntExtra(WEATHER_DATA_PURGE_AFTER,
                getPurgeAfterPreference());
        int updateInterval = intent.getIntExtra(WEATHER_DATA_UPDATE_INTERVAL,
                DEFAULT_WEATHER_DATA_UPDATE_INTERVAL);
        final long currentTime = System.currentTimeMillis();

        // TODO check connectivity before update
        updateData(updateInterval, currentTime);

        if (purgeAfter >= 0) {
            purgeOldData(purgeAfter, currentTime);
        }

        sendWidgetRefresh();
    }

    private int getPurgeAfterPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String purgeAfterString = prefs.getString(getString(R.string.weather_purge_time_key), null);
        int purgeAfter = DEFAULT_WEATHER_DATA_PURGE_AFTER;
        if (purgeAfterString != null) {
            purgeAfter = Integer.parseInt(purgeAfterString);
        }
        return purgeAfter;
    }

    private int updateData(int updateInterval, long currentTime) {
        Context context = getApplicationContext();
        String query = null;
        if (updateInterval > 0) {
            query = Clocks.LAST_UPDATE + " < " + (currentTime - updateInterval);
        }

        WeatherService service = new AndroidWeatherServiceFactory().createService("owm");
        service.setLanguage(context.getString(R.string.weather_service_language));

        try {
            return updateDatabase(context, service, query);
        } finally {
            service.close();
        }
    }

    private int updateDatabase(Context context, WeatherService service, String query) {
        int count = 0;
        final ContentResolver resolver = context.getContentResolver();
        final Cursor c = resolver.query(Clocks.CONTENT_URI, UPDATE_PROJECTION, query, null, null);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    double lat = c.getDouble(c.getColumnIndex(Clocks.LATITUDE));
                    double lon = c.getDouble(c.getColumnIndex(Clocks.LONGITUDE));
                    long id = c.getLong(c.getColumnIndex(Clocks._ID));
                    try {
                        WeatherObservation observation = service.getWeather(lat, lon);

                        if (observation != null && Clocks.updateWeather(context, id, observation)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "failed to retrieve/update weather for " + lat + ", " + lon, e);
                    }
                }
            } finally {
                c.close();
            }
        } else {
            Log.e(TAG, "failed to update database: cursor was null.");
        }
        return count;
    }

    private int purgeOldData(int purgeAfter, long currentTime) {
        Context context = getApplicationContext();
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {Clocks._ID};
        String query = Clocks.LAST_UPDATE + " < " + (currentTime - purgeAfter);
        int count = 0;
        WeatherObservation obs = new EmptyObservation(getResources());

        Cursor c = resolver.query(Clocks.CONTENT_URI, projection, query, null, null);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    long id = c.getLong(c.getColumnIndex(Clocks._ID));
                    if (Clocks.updateWeather(context, id, obs)) {
                        count++;
                    }
                }
            } finally {
                c.close();
            }
        } else {
            Log.e(TAG, "failed to purge old data: cursor was null.");
        }
        return count;
    }

    private void sendWidgetRefresh() {
        Intent broadcast = new Intent(ClockWidgetProvider.WIDGET_DATA_CHANGED_ACTION);
        getApplicationContext().sendBroadcast(broadcast);
    }

    private static class EmptyObservation implements WeatherObservation {

        private Resources mRes;

        public EmptyObservation(Resources res) {
            mRes = res;
        }

        @Override
        public Date getUpdateTime() {
            return new Date(0);
        }

        @Override
        public Double getTemperature() {
            return null;
        }

        @Override
        public Double getWindSpeed() {
            return null;
        }

        @Override
        public String getWindDirection() {
            return null;
        }

        @Override
        public Double getHumidity() {
            return null;
        }

        @Override
        public String getWeatherCondition() {
            return mRes.getString(R.string.no_data_available);
        }

        @Override
        public int getConditionCode() {
            return NA;
        }
    }
}
