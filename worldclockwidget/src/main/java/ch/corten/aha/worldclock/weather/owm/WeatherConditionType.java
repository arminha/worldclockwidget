package ch.corten.aha.worldclock.weather.owm;

import android.util.SparseArray;

import ch.corten.aha.worldclock.weather.WeatherObservation;
import static ch.corten.aha.worldclock.weather.owm.WeatherConditionPriority.*;

enum WeatherConditionType {
    THUNDERSTORM_WITH_LIGHT_RAIN(200, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    THUNDERSTORM_WITH_RAIN(201, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    THUNDERSTORM_WITH_HEAVY_RAIN(202, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    LIGHT_THUNDERSTORM(210, THUNDERSTORM_PRIORITY, WeatherObservation.CHANCE_OF_TSTORM),
    THUNDERSTORM(211, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    HEAVY_THUNDERSTORM(212, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    RAGGED_THUNDERSTORM(221, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    THUNDERSTORM_WITH_LIGHT_DRIZZLE(230, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    THUNDERSTORM_WITH_DRIZZLE(231, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),
    THUNDERSTORM_WITH_HEAVY_DRIZZLE(232, THUNDERSTORM_PRIORITY, WeatherObservation.THUNDERSTORM),

    LIGHT_INTENSITY_DRIZZLE(300, DRIZZLE_PRIORITY, WeatherObservation.DRIZZLE),
    DRIZZLE(301, DRIZZLE_PRIORITY, WeatherObservation.DRIZZLE),
    HEAVY_INTENSITY_DRIZZLE(302, DRIZZLE_PRIORITY, WeatherObservation.DRIZZLE),
    LIGHT_INTENSITY_DRIZZLE_RAIN(310, DRIZZLE_PRIORITY, WeatherObservation.DRIZZLE),
    DRIZZLE_RAIN(311, DRIZZLE_PRIORITY, WeatherObservation.DRIZZLE),
    HEAVY_INTENSITY_DRIZZLE_RAIN(312, DRIZZLE_PRIORITY, WeatherObservation.RAIN),
    SHOWER_DRIZZLE(321, DRIZZLE_PRIORITY, WeatherObservation.SHOWERS),

    LIGHT_RAIN(500, RAIN_PRIORITY, WeatherObservation.LIGHT_RAIN),
    MODERATE_RAIN(501, RAIN_PRIORITY, WeatherObservation.RAIN),
    HEAVY_INTENSITY_RAIN(502, RAIN_PRIORITY, WeatherObservation.HEAVY_RAIN),
    VERY_HEAVY_RAIN(503, RAIN_PRIORITY, WeatherObservation.HEAVY_RAIN),
    EXTREME_RAIN(504, RAIN_PRIORITY, WeatherObservation.HEAVY_RAIN),
    FREEZING_RAIN(511, RAIN_PRIORITY, WeatherObservation.FREEZING_DRIZZLE),
    LIGHT_INTENSITY_SHOWER_RAIN(520, RAIN_PRIORITY, WeatherObservation.LIGHT_RAIN),
    SHOWER_RAIN(521, RAIN_PRIORITY, WeatherObservation.SHOWERS),
    HEAVY_INTENSITY_SHOWER_RAIN(522, RAIN_PRIORITY, WeatherObservation.RAIN),

    LIGHT_SNOW(600, SNOW_PRIORITY, WeatherObservation.LIGHT_SNOW),
    SNOW(601, SNOW_PRIORITY, WeatherObservation.SNOW),
    HEAVY_SNOW(602, SNOW_PRIORITY, WeatherObservation.SNOW),
    SLEET(611, SNOW_PRIORITY, WeatherObservation.SLEET),
    SHOWER_SNOW(621, SNOW_PRIORITY, WeatherObservation.CHANCE_OF_SNOW),

    MIST(701, ATMOSPHERE_PRIORITY, WeatherObservation.MIST),
    SMOKE(711, ATMOSPHERE_PRIORITY, WeatherObservation.SMOKE),
    HAZE(721, ATMOSPHERE_PRIORITY, WeatherObservation.HAZE),
    SAND_DUST_WHIRLS(731, ATMOSPHERE_PRIORITY, WeatherObservation.DUST),
    FOG(741, ATMOSPHERE_PRIORITY, WeatherObservation.FOG),

    SKY_IS_CLEAR(800, CLOUDS_PRIORITY, WeatherObservation.CLEAR),
    FEW_CLOUDS(801, CLOUDS_PRIORITY, WeatherObservation.MOSTLY_SUNNY),
    SCATTERED_CLOUDS(802, CLOUDS_PRIORITY, WeatherObservation.PARTLY_CLOUDY),
    BROKEN_CLOUDS(803, CLOUDS_PRIORITY, WeatherObservation.CLOUDY),
    OVERCAST_CLOUDS(804, CLOUDS_PRIORITY, WeatherObservation.OVERCAST),

    TORNADO(900, EXTREME_PRIORITY, WeatherObservation.STORM),
    TROPICAL_STORM(901, EXTREME_PRIORITY, WeatherObservation.HURRICANE),
    HURRICANE(902, EXTREME_PRIORITY, WeatherObservation.HURRICANE),
    COLD(903, EXTREME_PRIORITY, WeatherObservation.ICY),
    HOT(904, EXTREME_PRIORITY, WeatherObservation.HOT),
    WINDY(905, EXTREME_PRIORITY, WeatherObservation.STORM),
    HAIL(906, EXTREME_PRIORITY, WeatherObservation.HAIL),

    NONE(-1, -1, WeatherObservation.NA);

    private static final SparseArray<WeatherConditionType> ID_MAP;
    static {
        SparseArray<WeatherConditionType> map = new SparseArray<WeatherConditionType>();
        for (WeatherConditionType type : WeatherConditionType.values()) {
            map.put(type.getId(), type);
        }
        ID_MAP = map;
    }

    private final int id;
    private final int priority;
    private final int conditionCode;

    private WeatherConditionType(int id, int priority, int conditionCode) {
        this.id = id;
        this.priority = priority;
        this.conditionCode = conditionCode;
    }

    int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public int getConditionCode() {
        return conditionCode;
    }

    public static WeatherConditionType fromId(int id) {
        WeatherConditionType type = ID_MAP.get(id);
        return (type != null) ? type : NONE;
    }
}
