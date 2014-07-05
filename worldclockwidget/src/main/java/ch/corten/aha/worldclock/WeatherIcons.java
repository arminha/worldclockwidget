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

import java.util.Calendar;
import java.util.TimeZone;

import ch.corten.aha.worldclock.weather.WeatherObservation;

public final class WeatherIcons {

    private WeatherIcons() {
    }

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
        case WeatherObservation.HOT:
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
        case WeatherObservation.SMOKE:
            return R.drawable.weather_mist;
        case WeatherObservation.CLOUDY:
            return R.drawable.weather_cloudy;
        case WeatherObservation.RAIN:
        case WeatherObservation.HEAVY_RAIN:
        case WeatherObservation.HURRICANE:
            return R.drawable.weather_rain;
        case WeatherObservation.HAIL:
            return R.drawable.weather_hail;
        case WeatherObservation.ICY:
            return R.drawable.weather_icy;
        case WeatherObservation.LIGHT_RAIN:
        case WeatherObservation.DRIZZLE:
        case WeatherObservation.CHANCE_OF_RAIN:
            return R.drawable.weather_showers;
        case WeatherObservation.LIGHT_SNOW:
        case WeatherObservation.FLURRIES:
        case WeatherObservation.CHANCE_OF_SNOW:
            return R.drawable.weather_light_snow;
        case WeatherObservation.SNOW:
        case WeatherObservation.SNOW_SHOWERS:
            return R.drawable.weather_snow;
        case WeatherObservation.THUNDERSTORM:
            return R.drawable.weather_thunderstorm;
        case WeatherObservation.SCATTERED_THUNDERSTORMS:
        case WeatherObservation.CHANCE_OF_TSTORM:
            return R.drawable.weather_chance_of_thunderstorm;
        case WeatherObservation.RAIN_AND_SNOW:
        case WeatherObservation.SLEET:
        case WeatherObservation.FREEZING_DRIZZLE:
            return R.drawable.weather_rain_snow;
        case WeatherObservation.CHANCE_OF_STORM:
        case WeatherObservation.STORM:
        case WeatherObservation.DUST:
        case WeatherObservation.WINDY:
            return R.drawable.weather_storm;
        default:
            return R.drawable.weather_na;
        }
    }

    public static int getIcon(int conditionCode, double longitude, double latitude) {
        return getIcon(conditionCode, isNight(longitude, latitude));
    }

    private static final long UNIX_STD_EQUINOX = 946728000;
    private static final double MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

    private static final double RAD_TO_DEG = 180 / Math.PI;
    private static final double DEG_TO_RAD = Math.PI / 180;

    private static final Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    /**
     * Compute the current coordinates of the sun at the given point
     * and return weather it is night or day.
     * Algorithm according to http://de.wikipedia.org/wiki/Sonnenstand
     * @param longitude
     * @param latitude
     * @return
     */
    public static boolean isNight(double longitude, double latitude) {
        final long currentTimeMillis = System.currentTimeMillis();
        final double n = (currentTimeMillis - UNIX_STD_EQUINOX) / MILLISECONDS_PER_DAY;
        final double l = (280.460 + (360 / 365.2422) * n) % 360;
        final double g = (357.528 + (360 / 365.2596) * n) % 360;
        final double delta = l + 1.915 * Math.sin(g * DEG_TO_RAD)
                + 0.02 * Math.sin(2 * g * DEG_TO_RAD);
        final double epsilon = 23.439 - 0.0000004 * n;
        double alpha = RAD_TO_DEG
                * Math.atan(Math.cos(epsilon * DEG_TO_RAD)
                        * Math.sin(delta * DEG_TO_RAD)
                        / Math.cos(delta * DEG_TO_RAD));
        if (Math.cos(delta * DEG_TO_RAD) < 0) {
            alpha = alpha + 180;
        }
        final double deltaRad = Math.asin(Math.sin(epsilon * DEG_TO_RAD)
                * Math.sin(delta * DEG_TO_RAD));
        Calendar cal = (Calendar) UTC_CALENDAR.clone();
        cal.setTimeInMillis(currentTimeMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final double n0 = (cal.getTimeInMillis() - UNIX_STD_EQUINOX) / MILLISECONDS_PER_DAY;
        final double t0 = n0 / 36525;
        cal.setTimeInMillis(currentTimeMillis);
        final double t = hourOfDay(cal);
        final double omegaHG = (6.697376 + 2400.05134 * t0 + 1.002738 * t) % 24;
        final double omegaG = omegaHG * 15;
        final double omega = omegaG + longitude;
        final double tau = omega - alpha;
        final double h = RAD_TO_DEG
                * Math.asin(Math.cos(deltaRad)
                        * Math.cos(tau * DEG_TO_RAD)
                        * Math.cos(latitude * DEG_TO_RAD)
                        + Math.sin(deltaRad)
                        * Math.sin(latitude * DEG_TO_RAD));
        return h < 0;
    }

    private static double hourOfDay(Calendar cal) {
        final int hours = cal.get(Calendar.HOUR_OF_DAY);
        final int minutes = cal.get(Calendar.MINUTE);
        final int seconds = cal.get(Calendar.SECOND);
        final int milliseconds = cal.get(Calendar.MILLISECOND);

        return hours + (minutes / 60.0) + (seconds / 3600.0) + (milliseconds / 3600000.0);
    }
}
