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

import ch.corten.aha.worldclock.weather.msn.MsnWeatherService;
import ch.corten.aha.worldclock.weather.owm.OwmWeatherService;
import ch.corten.aha.worldclock.weather.yahoo.YahooWeatherService;

public class AndroidWeatherServiceFactory implements WeatherServiceFactory {

    private final Context mContext;

    public AndroidWeatherServiceFactory(Context context) {
        mContext = context;
    }

    @Override
    public WeatherService createService(String provider) {
        if (provider.equals("msn")) {
            return new MsnWeatherService(true);
        } else if (provider.equals("owm")) {
            return new OwmWeatherService();
        } else {
            return new YahooWeatherService(mContext);
        }
    }
}
