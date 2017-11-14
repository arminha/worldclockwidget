/*
 * Copyright (C) 2014  Armin Häberling
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

package ch.corten.aha.worldclock.weather;

import android.util.Log;

import ch.corten.aha.worldclock.BuildConfig;
import ch.corten.aha.worldclock.weather.owm.OwmWeatherService;

public class AndroidWeatherServiceFactory implements WeatherServiceFactory {

    private static final String TAG = "WeatherServiceFactory";
    @Override
    public WeatherService createService(String provider,String owm_api_key) {
        if (BuildConfig.ENABLE_WEATHER) {
            Log.e(TAG, "Info:: weather service enabled. Key Value:- "+owm_api_key );
            if (owm_api_key == BuildConfig.OWM_API_KEY) {
                Log.e(TAG, "Info:: Currently using default key!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return new OwmWeatherService(owm_api_key); //Here passing the OWM_API_KEY to the OwmWeatherService class
        } else {
            Log.e(TAG, "Warning!!!::Weather service is disabled!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return new OwmWeatherService(null);
        }
    }
}
