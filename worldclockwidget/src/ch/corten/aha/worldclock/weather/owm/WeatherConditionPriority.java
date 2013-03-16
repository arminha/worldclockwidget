package ch.corten.aha.worldclock.weather.owm;

final class WeatherConditionPriority {

    public static final int CLOUDS_PRIORITY = 0;
    public static final int ATMOSPHERE_PRIORITY = 1;
    public static final int SNOW_PRIORITY = 2;
    public static final int RAIN_PRIORITY = 3;
    public static final int DRIZZLE_PRIORITY = 4;
    public static final int THUNDERSTORM_PRIORITY = 5;
    public static final int EXTREME_PRIORITY = 6;

    private WeatherConditionPriority() {
    }
}
