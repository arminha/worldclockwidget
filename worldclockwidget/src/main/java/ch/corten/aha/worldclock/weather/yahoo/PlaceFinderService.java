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
import java.text.MessageFormat;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import android.util.Log;

class PlaceFinderService {
    private static final String TAG = "PlaceFinderService";

    private static final String YQL_QUERY = "select woeid from geo.placefinder where text=\"{0, number,0.00###},{1, number,0.00###}\" and gflags=\"R\"";
    private static final String QUERY = "q={0}&format=xml";
    private static final String PATH = "/v1/public/yql";
    private static final String SERVER = "query.yahooapis.com";

    public String reverseGeoCode(double latitude, double longitude) {
        String yql = MessageFormat.format(YQL_QUERY, latitude, longitude);

        try {
            String query = MessageFormat.format(QUERY, yql);
            java.net.URI uri = new URI("http", SERVER, PATH, query, null);
            URL url = new URL(uri.toASCIIString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    return fixInvalidWoeids(latitude, longitude, readWOEID(in));
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

    static String fixInvalidWoeids(double latitude, double longitude, String woeid) {
        // fix Macau bug of Yahoo PlaceFinder API
        if ("609135".equals(woeid) &&
                latitude > 22.0 && latitude < 22.3 &&
                longitude > 113.4 && longitude < 113.7) {
            return "20070017";
        }
        // Khulna bug: the city's woeid has no weather data.
        if ("1915118".equals(woeid) &&
                latitude > 22.75 && latitude < 22.86 &&
                longitude > 89.50 && longitude < 89.91) {
            return "2344792";
        }
        return woeid;
    }

    private static String readWOEID(InputStream in) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        InputSource source = new InputSource(in);
        try {
            String woeid = (String) xPath.evaluate("//woeid[1]/text()", source, XPathConstants.STRING);
            if (woeid != null) {
                return woeid;
            }
        } catch (XPathExpressionException e) {
            Log.e(TAG, "Failed to parse data", e);
        }
        return null;
    }
}
