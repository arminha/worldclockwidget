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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.util.Log;
import ch.corten.aha.worldclock.weather.AbstractObservation;
import ch.corten.aha.worldclock.weather.WeatherObservation;
import ch.corten.aha.worldclock.weather.WeatherService;

import com.google.gson.stream.JsonReader;

/**
 * Open Weather Map weather service using the JSON API.
 *
 * Documentation: http://openweathermap.org/current
 *
 * Example URL: http://api.openweathermap.org/data/2.5/weather?lat=51.507222&lon=-0.1275
 */
public class OwmWeatherService implements WeatherService {

    private static final String TAG = "WeatherService";

    private static final String DEFAULT_LANG = "en";

    private String mLanguageCode;
    private String mApiKey;

    public OwmWeatherService(String apiKey) {
        mApiKey = apiKey;
    }

    @Override
    public WeatherObservation getWeather(double latitude, double longitude) {
        try {
            String query = "lat=" + latitude + "&lon=" + longitude + "&units=metric";
            if (mLanguageCode != null) {
                query += "&lang=" + mLanguageCode;
            }
            if (mApiKey != null) {
                query += "&APPID=" + mApiKey;
            }
            URI uri = new URI("http", "api.openweathermap.org", "/data/2.5/weather", query, null);
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
     * Supported language codes:
     *
     * English - en
     * Russian - ru
     * Italian - it
     * Spanish - es (or sp)
     * Ukrainian - uk (or ua)
     * German - de
     * Portuguese - pt
     * Romanian - ro
     * Polish - pl
     * Finnish - fi
     * Dutch - nl
     * French - fr
     * Bulgarian - bg
     * Swedish - sv (or se)
     * Chinese Traditional - zh_tw
     * Chinese Simplified - zh (or zh_cn)
     * Turkish - tr
     * Croatian - hr
     * Catalan - ca
     */
    @Override
    public void setLanguage(String languageCode) {
        String language = languageOnly(languageCode);
        this.mLanguageCode = isSupported(language) ? language : DEFAULT_LANG;
    }

    private boolean isSupported(String language) {
        // Only basque is not supported for now
        return !"eu".equals(language);
    }

    private String languageOnly(String languageCode) {
        int index = languageCode.indexOf('-');
        if (index > 0) {
            return languageCode.substring(0, index);
        }
        return languageCode;
    }

    private WeatherObservation readStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        Observation observation = new Observation();
        readWeatherData(reader, observation);
        return observation;
    }

    /**
     * Reads weather data as described at http://bugs.openweathermap.org/projects/api/wiki/Weather_Data.
     *
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
        while (reader.hasNext()) {
            // read first condition
            reader.beginObject();
            int id = 0;
            String condition = null;
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("id".equals(name)) {
                    id = reader.nextInt();
                } else if ("description".equals(name)) {
                    condition = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            if (condition != null && id != 0) {
                observation.addCondition(new Condition(id, condition));
            }
            reader.endObject();
        }
        reader.endArray();
    }

    private void readWind(JsonReader reader, Observation observation) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("speed".equals(name)) {
                double speed = reader.nextDouble();
                observation.setWindSpeed(speed * 3.6);
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
                double temp = reader.nextDouble();
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

    private static class Condition {
        private final String description;
        private final WeatherConditionType type;

        public Condition(int id, String description) {
            // TODO choose description based on the type
            this.description = capitalize(description);
            this.type = WeatherConditionType.fromId(id);
        }

        public String getCondition() {
            return description;
        }

        public int getCode() {
            return type.getConditionCode();
        }

        public int getPriority() {
            return type.getPriority();
        }

        private static String capitalize(String description) {
            return description.substring(0, 1).toUpperCase(Locale.ENGLISH)
                    + description.substring(1).toLowerCase(Locale.ENGLISH);
        }
    }

    private static class Observation extends AbstractObservation {
        private final List<Condition> conditions = new ArrayList<Condition>();

        public void setWindDirection(Double direction) {
            String dir = null;
            if (direction != null) {
                // CHECKSTYLE IGNORE MagicNumberCheck
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
                // CHECKSTYLE END IGNORE MagicNumberCheck
            }
            setWindDirection(dir);
        }

        public void addCondition(Condition condition) {
            conditions.add(condition);
        }

        @Override
        public int getConditionCode() {
            int maxPriority = -1;
            int code = NA;
            for (Condition condition : conditions) {
                if (condition.getPriority() > maxPriority) {
                    maxPriority = condition.getPriority();
                    code = condition.getCode();
                }
            }
            return code;
        }

        @Override
        public String getWeatherCondition() {
            int maxPriority = -1;
            for (Condition condition : conditions) {
                if (condition.getPriority() > maxPriority) {
                    maxPriority = condition.getPriority();
                }
            }
            StringBuffer result = new StringBuffer();
            for (Condition condition : conditions) {
                if (condition.getPriority() == maxPriority) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(condition.getCondition());
                }
            }
            return result.toString();
        }
    }
}
