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

package ch.corten.aha.worldclock.weather;

import java.util.Date;

public interface WeatherObservation {
    int ERROR = -1;
    int NA = 0;
    int PARTLY_SUNNY = 1;
    int SCATTERED_THUNDERSTORMS = 2;
    int SHOWERS = 3;
    int SCATTERED_SHOWERS = 4;
    int RAIN_AND_SNOW = 5;
    int OVERCAST = 6;
    int LIGHT_SNOW = 7;
    int FREEZING_DRIZZLE = 8;
    int CHANCE_OF_RAIN = 9;
    int SUNNY = 10;
    int CLEAR = 11;
    int MOSTLY_SUNNY = 12;
    int PARTLY_CLOUDY = 13;
    int MOSTLY_CLOUDY = 14;
    int CHANCE_OF_STORM = 15;
    int RAIN = 16;
    int CHANCE_OF_SNOW = 17;
    int CLOUDY = 18;
    int MIST = 19;
    int STORM = 20;
    int THUNDERSTORM = 21;
    int CHANCE_OF_TSTORM = 22;
    int SLEET = 23;
    int SNOW = 24;
    int ICY = 25;
    int DUST = 26;
    int FOG = 27;
    int SMOKE = 28;
    int HAZE = 29;
    int FLURRIES = 30;
    int LIGHT_RAIN = 31;
    int SNOW_SHOWERS = 32;
    int HAIL = 33;
    int DRIZZLE = 34;
    int HEAVY_RAIN = 35;
    int HOT = 36;
    int WINDY = 37;
    int HURRICANE = 38;

    /**
     * Time of this update.
     */
    Date getUpdateTime();

    /**
     * Current temperature in Celsius.
     */
    Double getTemperature();

    /**
     * Wind speed in km/h.
     */
    Double getWindSpeed();

    /**
     * Wind direction in degrees.
     */
    String getWindDirection();

    /**
     * Humidity in percent.
     */
    Double getHumidity();

    /**
     * Description of the weather conditions.
     */
    String getWeatherCondition();

    int getConditionCode();
}
