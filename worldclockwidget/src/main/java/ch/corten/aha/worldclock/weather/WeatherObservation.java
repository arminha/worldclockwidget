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
    static final int ERROR = -1;
    static final int NA = 0;
    static final int PARTLY_SUNNY = 1;
    static final int SCATTERED_THUNDERSTORMS = 2;
    static final int SHOWERS = 3;
    static final int SCATTERED_SHOWERS = 4;
    static final int RAIN_AND_SNOW = 5;
    static final int OVERCAST = 6;
    static final int LIGHT_SNOW = 7;
    static final int FREEZING_DRIZZLE = 8;
    static final int CHANCE_OF_RAIN = 9;
    static final int SUNNY = 10;
    static final int CLEAR = 11;
    static final int MOSTLY_SUNNY = 12;
    static final int PARTLY_CLOUDY = 13;
    static final int MOSTLY_CLOUDY = 14;
    static final int CHANCE_OF_STORM = 15;
    static final int RAIN = 16;
    static final int CHANCE_OF_SNOW = 17;
    static final int CLOUDY = 18;
    static final int MIST = 19;
    static final int STORM = 20;
    static final int THUNDERSTORM = 21;
    static final int CHANCE_OF_TSTORM = 22;
    static final int SLEET = 23;
    static final int SNOW = 24;
    static final int ICY = 25;
    static final int DUST = 26;
    static final int FOG = 27;
    static final int SMOKE = 28;
    static final int HAZE = 29;
    static final int FLURRIES = 30;
    static final int LIGHT_RAIN = 31;
    static final int SNOW_SHOWERS = 32;
    static final int HAIL = 33;
    static final int DRIZZLE = 34;
    static final int HEAVY_RAIN = 35;
    static final int HOT = 36;
    static final int WINDY = 37;
    static final int HURRICANE = 38;

    /**
     * Time of this update
     */
    Date getUpdateTime();

    /**
     * Current temperature in Celsius
     */
    Double getTemperature();

    /**
     * Wind speed in km/h
     */
    Double getWindSpeed();

    /**
     * Wind direction in degrees
     */
    String getWindDirection();

    /**
     * Humidity in percent
     */
    Double getHumidity();

    /**
     * Description of the weather conditions
     */
    String getWeatherCondition();

    int getConditionCode();
}
