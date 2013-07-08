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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneInfo {
    public static int getTimeDifference(TimeZone tz) {
        int milliseconds = tz.getOffset(System.currentTimeMillis());
        return milliseconds / 60000;
    }

    public static String getTimeDifferenceString(TimeZone tz) {
        int minutesDiff = getTimeDifference(tz);
        StringBuilder sb = new StringBuilder();
        sb.append("GMT");
        if (minutesDiff != 0) {
            if (minutesDiff < 0) {
                sb.append("-");
            } else {
                sb.append("+");
            }
            minutesDiff = Math.abs(minutesDiff);
            sb.append(minutesDiff / 60);
            sb.append(":");

            int minutes = minutesDiff % 60;
            if (minutes < 10) {
                sb.append("0");
            }
            sb.append(minutes);
        }
        return sb.toString();
    }

    public static String getDescription(TimeZone tz) {
        if (tz.useDaylightTime() && tz.inDaylightTime(new Date())) {
            return tz.getDisplayName(true, TimeZone.LONG);
        }
        return tz.getDisplayName();
    }

    private static final DateFormat WEEKDAY_FORMAT = new SimpleDateFormat("EEE", Locale.US);

    public static String showTime(TimeZone tz, Date date, DateFormat df, boolean addWeekday) {
        df.setTimeZone(tz);
        String time = df.format(date);
        if (addWeekday) {
            time += showDifferenWeekday(tz, date);
        }
        return time;
    }

    public static String showDifferenWeekday(TimeZone tz, Date date) {
        DateFormat dayFormat = (DateFormat) WEEKDAY_FORMAT.clone();
        dayFormat.setTimeZone(tz);
        String day = dayFormat.format(date);
        DateFormat localDayFormat = (DateFormat) WEEKDAY_FORMAT.clone();
        if (!day.equals(localDayFormat.format(date))) {
            return " " + day;
        }
        return "";
    }
}
