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

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class TimeZoneInfo {

    private static final DateFormat WEEKDAY_FORMAT = new SimpleDateFormat("EEE", Locale.US);

    private TimeZoneInfo() {
    }

    public static int getTimeDifference(DateTimeZone tz) {
        int milliseconds = tz.getOffset(DateTimeUtils.currentTimeMillis());
        return milliseconds / 60000;
    }

    public static String formatDate(DateFormat dateFormat, DateTimeZone tz) {
        if (dateFormat instanceof SimpleDateFormat) {
            String pattern = ((SimpleDateFormat) dateFormat).toPattern();
            DateTimeFormatter format = DateTimeFormat.forPattern(pattern).withZone(tz);
            return format.print(DateTimeUtils.currentTimeMillis());
        } else {
            // TODO might return the wrong date
            dateFormat.setTimeZone(tz.toTimeZone());
            return dateFormat.format(new Date());
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

    public static String showTimeWithOptionalWeekDay(TimeZone tz, Date date, DateFormat df) {
        df.setTimeZone(tz);
        String time = df.format(date);
        time += showDifferentWeekday(tz, date);
        return time;
    }

    public static String showDifferentWeekday(TimeZone tz, Date date) {
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
