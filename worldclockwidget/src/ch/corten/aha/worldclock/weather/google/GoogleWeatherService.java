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

package ch.corten.aha.worldclock.weather.google;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

import ch.corten.aha.worldclock.weather.WeatherObservation;
import ch.corten.aha.worldclock.weather.WeatherService;

public class GoogleWeatherService implements WeatherService {
    
    private static final String TAG = "WeatherService";

    @Override
    public WeatherObservation getWeather(double latitude, double longitude) {
        int lat = Math.round((float)latitude * 1000000);
        int lon = Math.round((float)longitude * 1000000);
        String query = "weather=,,," + lat + "," + lon + "&hl=en";
        try {
            java.net.URI uri = new URI("http", "www.google.com", "/ig/api", query, null);
            URL url = new URL(uri.toASCIIString());
            Log.d(TAG, uri.toASCIIString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                return readStream(in);
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve weather data", e);
            // TODO i18n message
            return new Observation("Failed to retrieve weather data");
        }
    }

    private WeatherObservation readStream(InputStream in) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        InputSource source = new InputSource(in);
        try {
            NodeList current = (NodeList) xPath.evaluate("//current_conditions", source, XPathConstants.NODESET);
            if (current.getLength() > 0) {
                Observation obs = new Observation();
                obs.setTemperature((Double) xPath.evaluate("temp_c/@data", current.item(0), XPathConstants.NUMBER));
                obs.setWeatherCondition((String) xPath.evaluate("condition/@data", current.item(0), XPathConstants.STRING));
                obs.setWindCondition((String) xPath.evaluate("wind_condition/@data", current.item(0), XPathConstants.STRING));
                obs.setHumidity((String) xPath.evaluate("humidity/@data", current.item(0), XPathConstants.STRING));
                return obs;
            } else {
                return new Observation();
            }
        } catch (XPathExpressionException e) {
            Log.e(TAG, "Failed to parse weather data", e);
            // TODO i18n message
            return new Observation("Failed to parse weather data");
        }
    }

    private static class Observation implements WeatherObservation {
        private static final Map<String, Integer> CONDITION_CODES;
        static {
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            map.put("partly sunny", PARTLY_SUNNY);
            map.put("scattered thunderstorms", SCATTERED_THUNDERSTORMS);
            map.put("showers", SHOWERS);
            map.put("scattered showers", SCATTERED_SHOWERS);
            map.put("rain and snow", RAIN_AND_SNOW);
            map.put("overcast", OVERCAST);
            map.put("light snow", LIGHT_SNOW);
            map.put("freezing drizzle", FREEZING_DRIZZLE);
            map.put("chance of rain", CHANCE_OF_RAIN);
            map.put("sunny", SUNNY);
            map.put("clear", CLEAR);
            map.put("mostly sunny", MOSTLY_SUNNY);
            map.put("partly cloudy", PARTLY_CLOUDY);
            map.put("mostly cloudy", MOSTLY_CLOUDY);
            map.put("chance of storm", CHANCE_OF_STORM);
            map.put("rain", RAIN);
            map.put("chance of snow", CHANCE_OF_SNOW);
            map.put("cloudy", CLOUDY);
            map.put("mist", MIST);
            map.put("storm", STORM);
            map.put("thunderstorm", THUNDERSTORM);
            map.put("chance of tstorm", CHANCE_OF_TSTORM);
            map.put("sleet", SLEET);
            map.put("snow", SNOW);
            map.put("icy", ICY);
            map.put("dust", DUST);
            map.put("fog", FOG);
            map.put("smoke", SMOKE);
            map.put("haze", HAZE);
            map.put("flurries", FLURRIES);
            map.put("light rain", LIGHT_RAIN);
            map.put("snow showers", SNOW_SHOWERS);
            map.put("hail", HAIL);
            map.put("drizzle", DRIZZLE);
            map.put("heavy rain", HEAVY_RAIN);
            map.put("rain showers", SHOWERS);
            CONDITION_CODES = Collections.unmodifiableMap(map);
        }
        
        private Double mTemperature;
        private String mCondition;
        private final Date mUpdateTime;
        private Double mHumidity;
        private String mWindDirection;
        private Double mWindSpeed;
        private int mConditionCode;
        
        public Observation() {
            mUpdateTime = new Date();
        }
        
        public Observation(String errorMsg) {
            mUpdateTime = new Date();
            mCondition = errorMsg;
            mConditionCode = ERROR;
        }
        
        @Override
        public Date getUpdateTime() {
            return mUpdateTime;
        }

        @Override
        public Double getTemperature() {
            return mTemperature;
        }
        
        public void setTemperature(double temperature) {
            mTemperature = temperature;
        }

        @Override
        public Double getWindSpeed() {
            return mWindSpeed;
        }

        @Override
        public String getWindDirection() {
            return mWindDirection;
        }
        
        public void setWindCondition(String windCondition) {
            Pattern pattern = Pattern.compile("^Wind: (\\w+) at (\\d+) mph$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(windCondition);
            if (matcher.find()) {
                mWindDirection = matcher.group(1);
                double mph = Double.parseDouble(matcher.group(2));
                mWindSpeed = (double)Math.round(mph * 1.609344);
            } else {
                Log.e(TAG, "Cannot match wind condition: " + windCondition);
            }
        }

        @Override
        public Double getHumidity() {
            return mHumidity;
        }
        
        public void setHumidity(String humidityString) {
            Pattern pattern = Pattern.compile("^\\D*(\\d+(?:[,.]\\d+)?)%$");
            Matcher matcher = pattern.matcher(humidityString);
            if (matcher.find()) {
                String number = matcher.group(1);
                mHumidity = Double.parseDouble(number);
            } else {
                Log.e(TAG, "Cannot match humidity: " + humidityString);
            }
        }

        @Override
        public String getWeatherCondition() {
            return mCondition;
        }

        public void setWeatherCondition(String condition) {
            mCondition = condition;
            Integer code = CONDITION_CODES.get(condition.toLowerCase());
            if (code != null) {
                mConditionCode = code;
            } else {
                Log.e(TAG, "Cannot match condition: " + condition);
            }
        }
        
        @Override
        public int getConditionCode() {
            return mConditionCode;
        }
    }
}
