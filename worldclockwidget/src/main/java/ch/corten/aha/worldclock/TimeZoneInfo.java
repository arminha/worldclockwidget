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

import android.util.Log;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public final class TimeZoneInfo {

    private static final String WEEKDAY_FORMAT = "EEE";
    private static final String TZ_ID_TAG = "TZ-IDs";

    private TimeZoneInfo() {
    }

    public static int getTimeDifference(DateTimeZone tz) {
        int milliseconds = tz.getOffset(DateTimeUtils.currentTimeMillis());
        return milliseconds / 60000;
    }

    public static String formatDate(DateFormat dateFormat, DateTimeZone tz, long time) {
        if (dateFormat instanceof SimpleDateFormat) {
            String pattern = ((SimpleDateFormat) dateFormat).toPattern();
            DateTimeFormatter format = DateTimeFormat.forPattern(pattern).withZone(tz);
            return format.print(time);
        } else {
            dateFormat.setTimeZone(convertToJavaTimeZone(tz, time));
            return dateFormat.format(new Date(time));
        }
    }

    public static String getTimeDifferenceString(DateTimeZone tz) {
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

    public static String getDescription(DateTimeZone tz) {
        // The Java TimeZones gives a better description (and knows more time zones)
        TimeZone timeZone = tz.toTimeZone();
        if (timeZone.useDaylightTime() && timeZone.inDaylightTime(new Date())) {
            return timeZone.getDisplayName(true, TimeZone.LONG);
        }
        return timeZone.getDisplayName();
    }

    public static String showTimeWithOptionalWeekDay(DateTimeZone tz, long time, DateFormat df) {
        return formatDate(df, tz, time) + showDifferentWeekday(tz, time);
    }

    /**
     * Convert a joda-time {@link org.joda.time.DateTimeZone} to an equivalent Java {@link java.util.TimeZone}.
     *
     * @param dateTimeZone a joda-time {@link org.joda.time.DateTimeZone}
     * @param time         the time when the time zones should be equivalent.
     * @return a Java {@link java.util.TimeZone} with the same offset for the given time.
     */
    public static TimeZone convertToJavaTimeZone(DateTimeZone dateTimeZone, long time) {
        TimeZone timeZone = dateTimeZone.toTimeZone();
        long offset = dateTimeZone.getOffset(time);
        if (timeZone.getOffset(time) == offset) {
            return timeZone;
        }
        String[] ids = TimeZone.getAvailableIDs((int) offset);
        Log.d(TZ_ID_TAG, dateTimeZone.getID() + ": " + Arrays.toString(ids));
        for (String id : ids) {
            TimeZone tz = TimeZone.getTimeZone(id);
            if (tz.getOffset(time) == offset) {
                timeZone = tz;
                Log.d(TZ_ID_TAG, "Found time zone " + tz.getID() + " for " + dateTimeZone.getID() + " with offset: " + offset);
                break;
            }
        }
        return timeZone;
    }

    public static String showDifferentWeekday(DateTimeZone tz, long time) {
        DateTimeFormatter dayFormat = DateTimeFormat.forPattern(WEEKDAY_FORMAT).withZone(tz);
        String day = dayFormat.print(time);
        DateTimeFormatter localDayFormat = DateTimeFormat.forPattern(WEEKDAY_FORMAT);
        if (!day.equals(localDayFormat.print(time))) {
            return " " + day;
        }
        return "";
    }
}
