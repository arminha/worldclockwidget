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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

import ch.corten.aha.worldclock.BuildConfig;
import ch.corten.aha.worldclock.R;
import ch.corten.aha.worldclock.weather.owm.OwmWeatherService;

import ch.corten.aha.preference.WeatherApiPreference;

public class AndroidWeatherServiceFactory implements WeatherServiceFactory {

    private static final String TAG = "WeatherServiceFactory";
    private boolean no_key = false;

    @Override
    public WeatherService createService(String provider,String owm_api_key,Context mContext) {

        no_key = false;
        String api_key_value="";

        if (BuildConfig.ENABLE_WEATHER) {
            Log.e(TAG,"Default key from default_owm_api_key file: "+BuildConfig.OWM_API_KEY); //This is value from file "default_owm_api_key"
            Log.e(TAG, "Info:: weather service enabled.");
            //if no key/firsttime then get the default one.
            if(owm_api_key == null)
            {
                Log.e(TAG, "Info:: performance key null detected.");
                no_key = true;
            }
            else if (owm_api_key.equals("Please enter new open weather map API key"))
            {
                Log.e(TAG, "Info:: performance value is: Please enter new open weather map API.");
                no_key = true;
            }
            else if (owm_api_key.trim().length()!=32)
            {
                Log.e(TAG, "Info:: performance value is not 32 digit.");
                no_key = true;
            }

            if (no_key ){
                Log.e(TAG, "Info:: No key set!!!!!!!!!!!!!!!!!!!!!!!!!! ");
                //Trying to get the key from Buildconfig.
                String[] OWM_API_KEY_array = BuildConfig.OWM_API_KEY.trim().split(",");
                Log.e(TAG, "Info:: Total No of keys:- " + OWM_API_KEY_array.length );
                String new_api_key_from_file = (OWM_API_KEY_array[new Random().nextInt(OWM_API_KEY_array.length)]);
                Log.e(TAG, "Info:: New Key:" + new_api_key_from_file+".");
                //Here setting the OWM_API_KEY to the OwmWeatherService class
                api_key_value=new_api_key_from_file;
            }
            else
            {
                //always chose from performance key
                Log.e(TAG, "Info:: Working key found from performance. Key Value:- " + owm_api_key );
                api_key_value=owm_api_key;
            }

            //Finally update all clocks
            return new OwmWeatherService(api_key_value);
        } else {
            Log.e(TAG, "Warning!!!::Weather service is disabled!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return new OwmWeatherService(null);
        }
    }
}
