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

import net.time4j.Moment;
import net.time4j.TemporalType;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.PatternType;
import net.time4j.tz.NameStyle;
import net.time4j.tz.TZID;
import net.time4j.tz.Timezone;
import net.time4j.tz.ZonalOffset;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public final class TimeZoneInfo {

    private static final String WEEKDAY_FORMAT = "EEE";

    private TimeZoneInfo() {
    }

    public static int getTimeDifference(Timezone tz, Moment moment) { // in minutes
        return tz.getOffset(moment).getIntegralAmount() / 60;
    }

    public static String formatDate(DateFormat dateFormat, TZID tzid, Moment moment) {
        if (dateFormat instanceof SimpleDateFormat) {
            String pattern = ((SimpleDateFormat) dateFormat).toPattern();
            return ChronoFormatter.ofMomentPattern(
                    pattern,
                    PatternType.CLDR,
                    Locale.getDefault(),
                    tzid
            ).format(moment);
        } else {
            dateFormat.setTimeZone(convertToJavaTimeZone(tzid, null));
            return dateFormat.format(TemporalType.JAVA_UTIL_DATE.from(moment));
        }
    }

    public static String getTimeDifferenceString(Timezone tz, Moment moment) {
        int minutesDiff = getTimeDifference(tz, moment);
        StringBuilder sb = new StringBuilder();
        sb.append("UTC");
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

    public static String getDescription(Timezone tz, Moment moment) {
        NameStyle style =
                tz.isDaylightSaving(moment)
                        ? NameStyle.LONG_DAYLIGHT_TIME
                        : NameStyle.LONG_STANDARD_TIME;
        return tz.getDisplayName(style, Locale.getDefault());
    }

    public static String showTimeWithOptionalWeekDay(TZID tzid, Moment moment, DateFormat df) {
        return formatDate(df, tzid, moment) + showDifferentWeekday(tzid, moment);
    }

    /**
     * Convert a Time4A {@link net.time4j.tz.Timezone} to an equivalent Java {@link java.util.TimeZone}.
     *
     * @param   tzid        the time zone id
     * @param   moment      usually the current time
     * @return a Java {@link java.util.TimeZone} with the same offset for the given time.
     */
    public static TimeZone convertToJavaTimeZone(TZID tzid, Moment moment) {
        TimeZone timeZone = TimeZone.getTimeZone(tzid.canonical());
        ZonalOffset offset = Timezone.of(tzid).getOffset(moment);
        int platformOffsetInSecs = timeZone.getOffset(moment.getPosixTime() * 1000L) / 1000;
        if (platformOffsetInSecs == offset.getIntegralAmount()) {
            return timeZone;
        } else {
            // let's now return the simple offset representation
            // instead of searching for a potentially wrong replacement zone with same offset
            return TimeZone.getTimeZone(offset.canonical());
        }
    }

    public static String showDifferentWeekday(TZID tzid, Moment moment) {
        ChronoFormatter<Moment> dayFormat =
                ChronoFormatter.ofMomentPattern(
                        WEEKDAY_FORMAT,
                        PatternType.CLDR,
                        Locale.getDefault(),
                        tzid);
        ChronoFormatter<Moment> localDayFormat =
                ChronoFormatter.ofMomentPattern(
                        WEEKDAY_FORMAT,
                        PatternType.CLDR,
                        Locale.getDefault(),
                        Timezone.ofSystem().getID());
        String day = dayFormat.format(moment);
        String localDay = localDayFormat.format(moment);
        if (!day.equals(localDay)) {
            return " " + day;
        }
        return "";
    }
}
