package ch.corten.aha.worldclock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TimeZoneInfo {
    public static final String[] AREAS = {
        "Africa",
        "America",
        "Antarctica",
        "Arctic",
        "Asia",
        "Atlantic",
        "Australia",
        "Europe",
        "Indian",
        "Pacific"
    };
    
    public static Map<String, String[]> CITY_ALIAS = new HashMap<String, String[]>();
    static {
        CITY_ALIAS.put("Zurich", new String[] { "Bern" });
        CITY_ALIAS.put("Toronto", new String[] { "Ottawa" });
    }
    
    private String city;
    private String area;
    private String id;
    
    private TimeZoneInfo(String city, String area, String id) {
        this.city = city;
        this.area = area;
        this.id = id;
    }

    public static TimeZoneInfo[] getAllTimeZones() {
        final String[] iDs = TimeZone.getAvailableIDs();
        final ArrayList<TimeZoneInfo> timeZones = new ArrayList<TimeZoneInfo>();
        
        for (String id : iDs) {
            final TimeZoneInfo tz = getTimeZone(id);
            if (tz != null) {
                timeZones.add(tz);
                final String[] alias = CITY_ALIAS.get(tz.getCity());
                if (alias != null) {
                    for (String cityAlias : alias) {
                        final TimeZoneInfo tzAlias = new TimeZoneInfo(cityAlias, tz.getArea(), tz.getId());
                        timeZones.add(tzAlias);
                    }
                }
            }
        }
        
        return timeZones.toArray(new TimeZoneInfo[0]);
    }
    
    public static TimeZoneInfo getCity(String city) {
        TimeZoneInfo[] timeZones = getAllTimeZones();
        for (TimeZoneInfo timeZone : timeZones) {
            if (timeZone.getCity().equalsIgnoreCase(city)) {
                return timeZone;
            }
        }
        return null;
    }
    
    public static TimeZoneInfo getTimeZone(String id) {
        String[] parts = id.split("/");
        if (parts.length < 2) {
            return null;
        }
        String city = parts[parts.length - 1];
        StringBuilder area = new StringBuilder();
        for (int i = parts.length - 1; i >= 0; i--) {
            if (area.length() > 0) {
                area.append(", ");
            }
            area.append(parts[i].replace('_', ' '));
        }
        return new TimeZoneInfo(city, area.toString(), id);
    }

    public String getCity() {
        return city;
    }

    public String getArea() {
        return area;
    }

    public String getId() {
        return id;
    }
    
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(id);
    }
    
    public int getTimeDifference() {
        TimeZone tz = getTimeZone();
        int milliseconds = tz.getOffset(System.currentTimeMillis());
        return milliseconds / 60000;
    }
    
    @Override
    public String toString() {
        return "TimeZoneInfo [city=" + city + ", area=" + area + ", id=" + id
                + "]";
    }
}
