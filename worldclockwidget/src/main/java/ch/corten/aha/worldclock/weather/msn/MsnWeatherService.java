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

package ch.corten.aha.worldclock.weather.msn;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;
import android.util.SparseIntArray;
import ch.corten.aha.worldclock.weather.AbstractObservation;
import ch.corten.aha.worldclock.weather.WeatherObservation;
import ch.corten.aha.worldclock.weather.WeatherService;

public class MsnWeatherService implements WeatherService {

    private static final String TAG = "WeatherService";

    private static final String HTTP_SERVER = "weather.service.msn.com";
    private static final String HTTPS_SERVER = "weather.partners.msn.com";

    private final boolean mUseHttps;

    public MsnWeatherService(boolean useHttps) {
        mUseHttps = useHttps;
    }

    @Override
    public WeatherObservation getWeather(double latitude, double longitude) {
        String query = "weadegreetype=C&culture=en-US&weasearchstr=" + latitude + "," + longitude;
        try {
            java.net.URI uri;
            if (mUseHttps) {
                uri = new URI("https", HTTPS_SERVER, "/data.aspx", query, null);
            } else {
                uri = new URI("http", HTTP_SERVER, "/data.aspx", query, null);
            }
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
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Invalid URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrieve weather data", e);
            return null;
        } catch (URISyntaxException e) {
            Log.wtf(TAG, "Invalid URI", e);
        }
        return null;
    }

    /**
     * <code>
     * <current temperature="10"
     *          skycode="32"
     *          skytext="Klar"
     *          date="2012-10-16"
     *          day="Dienstag"
     *          shortday="Di"
     *          observationtime="12:50:00"
     *          observationpoint="Dubendorf Airport"
     *          feelslike="10"
     *          humidity="66"
     *          windspeed="4"
     *          winddisplay="4 km/h"/>
     * </code>
     * 
     * @param in
     * @return
     */
    private WeatherObservation readStream(InputStream in) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        InputSource source = new InputSource(in);
        try {
            NodeList current = (NodeList) xPath.evaluate("//current", source, XPathConstants.NODESET);
            if (current.getLength() > 0) {
                Node item = current.item(0);
                Observation obs = new Observation();
                obs.setWeatherCondition((String) xPath.evaluate("@skytext", item, XPathConstants.STRING));
                obs.setConditionCode((String) xPath.evaluate("@skycode", item, XPathConstants.STRING));
                obs.setTemperature((Double) xPath.evaluate("@temperature", item, XPathConstants.NUMBER));
                obs.setHumidity((Double) xPath.evaluate("@humidity", item, XPathConstants.NUMBER));
                obs.setWindSpeed((Double) xPath.evaluate("@windspeed", item, XPathConstants.NUMBER));
                return obs;
            } else {
                return new Observation();
            }
        } catch (XPathExpressionException e) {
            Log.e(TAG, "Failed to parse weather data", e);
            return null;
        }
    }

    @Override
    public void close() {
    }

    private static class Observation extends AbstractObservation {
        private static final SparseIntArray CONDITION_CODES;

        static {
            SparseIntArray map = new SparseIntArray();
            map.put(0, THUNDERSTORM);
            map.put(1, THUNDERSTORM);
            map.put(2, THUNDERSTORM);
            map.put(3, THUNDERSTORM);
            map.put(4, THUNDERSTORM);
            map.put(5, RAIN_AND_SNOW);
            map.put(6, SLEET);
            map.put(7, RAIN_AND_SNOW);
            map.put(8, ICY);
            map.put(9, DRIZZLE); // light rain (sprinkles)
            map.put(10, RAIN_AND_SNOW);
            map.put(11, LIGHT_RAIN);
            map.put(12, RAIN);
            map.put(13, LIGHT_SNOW);
            map.put(14, SNOW);
            map.put(15, SNOW); // blizzard
            map.put(16, SNOW);
            map.put(17, THUNDERSTORM);
            map.put(18, SHOWERS);
            map.put(19, DUST);
            map.put(20, FOG);
            map.put(21, HAZE);
            map.put(22, SMOKE);
            map.put(23, WINDY); // windy
            map.put(24, WINDY); // windy
            map.put(25, ICY);
            map.put(26, CLOUDY);
            map.put(27, MOSTLY_CLOUDY);
            map.put(28, MOSTLY_CLOUDY);
            map.put(29, MOSTLY_CLOUDY);
            map.put(30, MOSTLY_CLOUDY);
            map.put(31, SUNNY);
            map.put(32, SUNNY);
            map.put(33, MOSTLY_SUNNY);
            map.put(34, MOSTLY_SUNNY);
            map.put(35, THUNDERSTORM);
            map.put(36, HOT); // hot
            map.put(37, CHANCE_OF_TSTORM);
            map.put(38, CHANCE_OF_TSTORM);
            map.put(39, CHANCE_OF_RAIN);
            map.put(40, SHOWERS);
            map.put(41, CHANCE_OF_SNOW);
            map.put(42, SNOW);
            map.put(43, SNOW);
            map.put(44, NA);
            map.put(45, CHANCE_OF_RAIN);
            map.put(46, CHANCE_OF_SNOW);
            map.put(47, CHANCE_OF_TSTORM);
            CONDITION_CODES = map;
        }

        public Observation() {
            super();
        }

        public void setConditionCode(String codeAsString) {
            if (codeAsString != null && codeAsString.length() > 0) {
                int msnCode = Integer.parseInt(codeAsString);
                Integer code = CONDITION_CODES.get(msnCode);
                if (code != null) {
                    setConditionCode(code);
                } else {
                    setConditionCode(NA);
                }
            }
        }
    }

}
