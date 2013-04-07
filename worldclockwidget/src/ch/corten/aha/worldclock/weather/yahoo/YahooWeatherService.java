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

package ch.corten.aha.worldclock.weather.yahoo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import ch.corten.aha.worldclock.weather.AbstractObservation;
import ch.corten.aha.worldclock.weather.WeatherObservation;
import ch.corten.aha.worldclock.weather.WeatherService;

public class YahooWeatherService implements WeatherService {

    private static final String TAG = "WeatherService";
    private final PlaceFinderService mPlaceFinder;
    private final WoeidCache mCache;

    public YahooWeatherService(Context context) {
        mPlaceFinder = new PlaceFinderService();
        mCache = new WoeidCache(context);
    }

    @Override
    public void close() {
        mCache.close();
    }

    @Override
    public WeatherObservation getWeather(double latitude, double longitude) {
        String woeid = getWOEID(latitude, longitude);
        if (woeid == null) {
            return null;
        }
        if (woeid.length() == 0) {
            return new Observation();
        }

        String query = "w=" + woeid + "&u=c";
        try {
            java.net.URI uri = new URI("http", "weather.yahooapis.com", "/forecastrss", query, null);
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

    private WeatherObservation readStream(InputStream in) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new YahooNamespaces());
        InputSource source = new InputSource(in);
        try {
            NodeList current = (NodeList) xPath.evaluate("//channel", source, XPathConstants.NODESET);
            if (current.getLength() > 0) {
                Observation obs = new Observation();
                Node item = current.item(0);
                obs.setWeatherCondition((String) xPath.evaluate("item/yweather:condition/@text", item, XPathConstants.STRING));
                obs.setConditionCode((String) xPath.evaluate("item/yweather:condition/@code", item, XPathConstants.STRING));
                obs.setTemperature((Double) xPath.evaluate("item/yweather:condition/@temp", item, XPathConstants.NUMBER));
                obs.setWindSpeed((Double) xPath.evaluate("yweather:wind/@speed", item, XPathConstants.NUMBER));
                if (obs.getWindSpeed() > 0) {
                    obs.setWindDirection((Double) xPath.evaluate("yweather:wind/@direction", item, XPathConstants.NUMBER));
                }
                obs.setHumidity((Double) xPath.evaluate("yweather:atmosphere/@humidity", item, XPathConstants.NUMBER));
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

    private String getWOEID(double latitude, double longitude) {
        String woeid = mCache.get(latitude, longitude);
        if (woeid == null) {
            woeid = mPlaceFinder.reverseGeoCode(latitude, longitude);
            if (woeid != null) {
                mCache.put(latitude, longitude, woeid);
            }
        }
        return woeid;
    }

    private static class YahooNamespaces implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) throw new IllegalArgumentException("prefix must not be null");
            if ("yweather".equals(prefix)) {
                return "http://xml.weather.yahoo.com/ns/rss/1.0";
            } else if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
                return XMLConstants.XML_NS_URI;
            } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
            return XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            // not used
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Iterator getPrefixes(String namespaceURI) {
            // not used
            return null;
        }
    }

    private static class Observation extends AbstractObservation {
        private static final SparseIntArray CONDITION_CODES;
        static {
            SparseIntArray map = new SparseIntArray();
            map.put(0, STORM);
            map.put(1, HEAVY_RAIN);
            map.put(2, HEAVY_RAIN);
            map.put(3, THUNDERSTORM);
            map.put(4, THUNDERSTORM);
            map.put(5, RAIN_AND_SNOW);
            map.put(6, RAIN_AND_SNOW);
            map.put(7, RAIN_AND_SNOW);
            map.put(8, FREEZING_DRIZZLE);
            map.put(9, DRIZZLE);
            map.put(10, FREEZING_DRIZZLE);
            map.put(11, SHOWERS);
            map.put(12, SHOWERS);
            map.put(13, FLURRIES);
            map.put(14, LIGHT_SNOW);
            map.put(15, SNOW);
            map.put(16, SNOW);
            map.put(17, HAIL);
            map.put(18, SLEET);
            map.put(19, DUST);
            map.put(20, FOG);
            map.put(21, HAZE);
            map.put(22, SMOKE);
            map.put(23, STORM);
            map.put(24, STORM);
            map.put(25, ICY);
            map.put(26, CLOUDY);
            map.put(27, MOSTLY_CLOUDY);
            map.put(28, MOSTLY_CLOUDY);
            map.put(29, PARTLY_CLOUDY);
            map.put(30, PARTLY_CLOUDY);
            map.put(31, CLEAR);
            map.put(32, CLEAR);
            map.put(33, CLEAR);
            map.put(34, CLEAR);
            map.put(35, HAIL);
            map.put(36, CLEAR);
            map.put(37, CHANCE_OF_TSTORM);
            map.put(38, SCATTERED_THUNDERSTORMS);
            map.put(39, SCATTERED_THUNDERSTORMS);
            map.put(40, SCATTERED_SHOWERS);
            map.put(41, SNOW);
            map.put(42, CHANCE_OF_SNOW);
            map.put(43, SNOW);
            map.put(44, PARTLY_CLOUDY);
            map.put(45, THUNDERSTORM);
            map.put(46, SNOW_SHOWERS);
            map.put(47, CHANCE_OF_TSTORM);
            map.put(3200, NA);
            CONDITION_CODES = map;
        }

        public Observation() {
            super();
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

        public Observation(String errorMsg) {
            super(errorMsg);
        }

        public void setConditionCode(String codeAsString) {
            if (codeAsString != null && codeAsString.length() > 0) {
                int yahooCode = Integer.parseInt(codeAsString);
                Integer code = CONDITION_CODES.get(yahooCode);
                if (code != null) {
                    setConditionCode(code);
                } else {
                    setConditionCode(NA);
                }
            }
        }
    }

}
