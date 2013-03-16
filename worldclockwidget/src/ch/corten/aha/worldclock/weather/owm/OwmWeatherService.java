package ch.corten.aha.worldclock.weather.owm;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import android.util.JsonReader;
import android.util.Log;
import android.util.SparseIntArray;

import ch.corten.aha.worldclock.weather.AbstractObservation;
import ch.corten.aha.worldclock.weather.WeatherObservation;
import ch.corten.aha.worldclock.weather.WeatherService;

/**
 * Open Weather Map weather service.
 */
public class OwmWeatherService implements WeatherService {

    private static final String TAG = "WeatherService";

    @Override
    public WeatherObservation getWeather(double latitude, double longitude) {
        try {
            String query = "lat=" + latitude + "&lon=" + longitude + "&cnt=1";
            URI uri = new URI("http", "api.openweathermap.org", "/data/2.1/find/city", query, null);
            URL url = new URL(uri.toASCIIString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    return readStream(in);
                }
            } finally {
                conn.disconnect();
            }
        } catch (URISyntaxException e) {
            Log.wtf(TAG, "Invalid URI", e);
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Invalid URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrieve weather data", e);
            return null;
        }
        return null;
    }

    /**
     * Read JSON as described at http://openweathermap.org/wiki/API/2.1/JSON_API
     * @param in
     * @return
     * @throws IOException
     */
    private WeatherObservation readStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        Observation observation = new Observation();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("list".equals(name)) {
                reader.beginArray();
                // read first element only
                if (reader.hasNext()) {
                    readWeatherData(reader, observation);
                }
                while (reader.hasNext()) {
                    reader.skipValue();
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return observation;
    }

    /**
     * Reads weather data as described at http://openweathermap.org/wiki/API/2.0/Weather_Data
     * @param reader
     * @param observation
     * @throws IOException
     */
    private void readWeatherData(JsonReader reader, Observation observation) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("main".equals(name)) {
                readMain(reader, observation);
            } else if ("wind".equals(name)) {
                readWind(reader, observation);
            } else if ("weather".equals(name)) {
                readWeather(reader, observation);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private void readWeather(JsonReader reader, Observation observation) throws IOException {
        reader.beginArray();
        if (reader.hasNext()) {
            // read first condition
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("id".equals(name)) {
                    int id = reader.nextInt();
                    observation.setConditionCode(id);
                } else if ("main".equals(name)) {
                    String condition = reader.nextString();
                    observation.setWeatherCondition(condition);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        while (reader.hasNext()) {
            // TODO read weather condition
            reader.skipValue();
        }
        reader.endArray();
    }

    private void readWind(JsonReader reader, Observation observation) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("speed".equals(name)) {
                double speed = reader.nextDouble() * 3.6;
                observation.setWindSpeed(speed);
            } else if ("deg".equals(name)) {
                double direction = reader.nextDouble();
                observation.setWindDirection(direction);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private void readMain(JsonReader reader, Observation observation) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("temp".equals(name)) {
                double temp = reader.nextDouble() - 273.15;
                observation.setTemperature(temp);
            } else if ("humidity".equals(name)) {
                double humidity = reader.nextDouble();
                observation.setHumidity(humidity);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    @Override
    public void close() {
    }

    private static class Observation extends AbstractObservation {
        private static final SparseIntArray CONDITION_CODES;

        static {
            SparseIntArray map = new SparseIntArray();
            map.put(200, THUNDERSTORM); //  thunderstorm with light rain
            map.put(201, THUNDERSTORM); //  thunderstorm with rain
            map.put(202, THUNDERSTORM); //  thunderstorm with heavy rain
            map.put(210, CHANCE_OF_TSTORM); // light thunderstorm
            map.put(211, THUNDERSTORM); // thunderstorm
            map.put(212, THUNDERSTORM); // heavy thunderstorm
            map.put(221, THUNDERSTORM); // ragged thunderstorm
            map.put(230, THUNDERSTORM); // thunderstorm with light drizzle
            map.put(231, THUNDERSTORM); // thunderstorm with drizzle
            map.put(232, THUNDERSTORM); // thunderstorm with heavy drizzle
            map.put(300, DRIZZLE); // light intensity drizzle
            map.put(301, DRIZZLE); // drizzle
            map.put(302, DRIZZLE); // heavy intensity drizzle
            map.put(310, DRIZZLE); // light intensity drizzle rain
            map.put(311, DRIZZLE); // drizzle rain
            map.put(312, RAIN); // heavy intensity drizzle rain
            map.put(321, SHOWERS); // shower drizzle
            map.put(500, LIGHT_RAIN); // light rain
            map.put(501, RAIN); // moderate rain
            map.put(502, HEAVY_RAIN); // heavy intensity rain
            map.put(503, HEAVY_RAIN); // very heavy rain
            map.put(504, HEAVY_RAIN); // extreme rain
            map.put(511, FREEZING_DRIZZLE); // freezing rain
            map.put(520, LIGHT_RAIN); // light intensity shower rain
            map.put(521, SHOWERS); // shower rain
            map.put(522, RAIN); // heavy intensity shower rain
            map.put(600, LIGHT_SNOW); // light snow
            map.put(601, SNOW); // snow
            map.put(602, SNOW); // heavy snow
            map.put(611, SLEET); // sleet
            map.put(621, CHANCE_OF_SNOW); // shower snow
            map.put(701, MIST); // mist
            map.put(711, SMOKE); // smoke
            map.put(721, HAZE); // haze
            map.put(731, DUST); // Sand/Dust Whirls
            map.put(741, FOG); // Fog
            map.put(800, CLEAR); // sky is clear
            map.put(801, MOSTLY_SUNNY); // few clouds
            map.put(802, PARTLY_CLOUDY); // scattered clouds
            map.put(803, CLOUDY); // broken clouds
            map.put(804, OVERCAST); // overcast clouds
            map.put(900, STORM); // tornado
            map.put(901, HURRICANE); // tropical storm
            map.put(902, HURRICANE); // hurricane
            map.put(903, ICY); // cold
            map.put(904, HOT); // hot
            map.put(905, STORM); // windy
            map.put(906, HAIL); // hail
            CONDITION_CODES = map;
        }

        public void setWindDirection(Double direction) {
            String dir = null;
            if (direction != null) {
                if (direction < 22.5) {
                    dir = "N";
                } else if (direction < 67.5) {
                    dir = "NE";
                } else if (direction < 112.5) {
                    dir = "E";
                } else if (direction < 157.5) {
                    dir = "SE";
                } else if (direction < 202.5) {
                    dir = "S";
                } else if (direction < 247.5) {
                    dir = "SW";
                } else if (direction < 292.5) {
                    dir = "W";
                } else if (direction < 337.5) {
                    dir = "NW";
                } else {
                    dir = "N";
                }
            }
            setWindDirection(dir);
        }

        @Override
        public void setConditionCode(int id) {
            Integer code = CONDITION_CODES.get(id);
            super.setConditionCode(code);
        }
    }
}
