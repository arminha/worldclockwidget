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

import java.text.MessageFormat;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

public class BindHelper {
    public static void bindText(View view, Cursor cursor, int resource, String column) {
        TextView textView = (TextView) view.findViewById(resource);
        textView.setText(cursor.getString(cursor.getColumnIndex(column)));
    }

    public static void setText(View view, int resource, String text) {
        TextView textView = (TextView) view.findViewById(resource);
        textView.setText(text);
    }

    public static final String CELSIUS = "C";
    public static final String FAHRENHEIT = "F";

    public static void bindTemperature(Context context, View view, Cursor cursor, int resource) {
        TextView tempText = (TextView) view.findViewById(resource);
        tempText.setText(getTemperature(context, cursor, true));
    }

    private static final String TEMPARATURE_FORMAT = "{0,number,0.0}";

    public static String getTemperature(Context context, Cursor cursor, boolean addUnit) {
        int index = cursor.getColumnIndex(Clocks.TEMPERATURE);
        StringBuffer sb = new StringBuffer(5);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = prefs.getString(context.getString(R.string.temp_unit_key), CELSIUS);
        if (cursor.isNull(index)) {
            sb.append("--");
        } else {
            final double temperature = cursor.getDouble(index);
            if (unit.equals(CELSIUS)) {
                sb.append(MessageFormat.format(TEMPARATURE_FORMAT, temperature));
            } else if (unit.equals(FAHRENHEIT)) {
                final double fTemperature = Math.round(temperature * 9.0 / 5.0 + 32);
                sb.append(MessageFormat.format(TEMPARATURE_FORMAT, fTemperature));
            } else {
                throw new RuntimeException("Invalid value: " + unit);
            }
        }
        sb.append('°');
        if (addUnit) {
            sb.append(unit);
        }
        return sb.toString();
    }

    private static final String KMH = "kmh";
    private static final String MPH = "mph";
    private static final String MS = "ms";

    private static final String KMH_FORMAT = "{0,number,#} km/h";
    private static final String MPH_FORMAT = "{0,number,#} mph";
    private static final String MS_FORMAT = "{0,number,#} m/s";

    public static String getSpeed(Context context, double windSpeed) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = prefs.getString(context.getString(R.string.wind_speed_unit_key), KMH);
        if (unit.equals(KMH)) {
            return MessageFormat.format(KMH_FORMAT, windSpeed);
        } else if (unit.equals(MPH)) {
            double mphSpeed = Math.round(windSpeed * 0.621371192);
            return MessageFormat.format(MPH_FORMAT, mphSpeed);
        } else if (unit.equals(MS)) {
            double msSpeed = Math.round(windSpeed / 3.6);
            return MessageFormat.format(MS_FORMAT, msSpeed);
        } else {
            throw new RuntimeException("Invalid value: " + unit);
        }
    }

}
