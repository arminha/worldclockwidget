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

package ch.corten.aha.worldclock;

import ch.corten.aha.worldclock.weather.WeatherObservation;

public class WeatherIcons {
    public static int getIcon(int conditionCode) {
        return getIcon(conditionCode, false);
    }
    
    public static int getIcon(int conditionCode, boolean night) {
        switch (conditionCode) {
        case WeatherObservation.ERROR:
        case WeatherObservation.NA:
            return R.drawable.weather_na;
        case WeatherObservation.SUNNY:
        case WeatherObservation.CLEAR:
            return night ? R.drawable.weather_clear_night
                    : R.drawable.weather_clear;
        case WeatherObservation.PARTLY_SUNNY:
        case WeatherObservation.MOSTLY_SUNNY:
        case WeatherObservation.PARTLY_CLOUDY:
        case WeatherObservation.MOSTLY_CLOUDY:
            return night ? R.drawable.weather_partly_cloudy_night
                    : R.drawable.weather_partly_cloudy;
        case WeatherObservation.OVERCAST:
            return R.drawable.weather_overcast;
        case WeatherObservation.SHOWERS:
        case WeatherObservation.SCATTERED_SHOWERS:
            return R.drawable.weather_showers;
        case WeatherObservation.MIST:
        case WeatherObservation.FOG:
        case WeatherObservation.HAZE:
            return R.drawable.weather_mist;
        case WeatherObservation.CLOUDY:
            return R.drawable.weather_cloudy;
        case WeatherObservation.RAIN:
        case WeatherObservation.HEAVY_RAIN:
            return R.drawable.weather_rain;
        case WeatherObservation.HAIL:
            return R.drawable.weather_hail;
        case WeatherObservation.ICY:
            return R.drawable.weather_icy;
        case WeatherObservation.LIGHT_RAIN:
        case WeatherObservation.DRIZZLE:
            return R.drawable.weather_showers;
        case WeatherObservation.LIGHT_SNOW:
        case WeatherObservation.FLURRIES:
            return R.drawable.weather_light_snow;
        case WeatherObservation.SNOW:
            return R.drawable.weather_snow;
        case WeatherObservation.THUNDERSTORM:
            return R.drawable.weather_thunderstorm;
        case WeatherObservation.SCATTERED_THUNDERSTORMS:
        case WeatherObservation.CHANCE_OF_TSTORM:
            return R.drawable.weather_chance_of_thunderstorm;
        case WeatherObservation.RAIN_AND_SNOW:
        case WeatherObservation.FREEZING_DRIZZLE:
        case WeatherObservation.CHANCE_OF_RAIN:
        case WeatherObservation.CHANCE_OF_STORM:
        case WeatherObservation.CHANCE_OF_SNOW:
        case WeatherObservation.STORM:
        case WeatherObservation.SLEET:
        case WeatherObservation.DUST:
        case WeatherObservation.SMOKE:
        case WeatherObservation.SNOW_SHOWERS:
        default:
            return R.drawable.weather_na;
        }
    }
}
