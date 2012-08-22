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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import android.util.Log;

class PlaceFinderService {
    private static final String TAG = "PlaceFinderService";
    private final String mAppId;

    public PlaceFinderService(String appId) {
        mAppId = appId;
    }

    public String reverseGeoCode(double latitude, double longitude) {
        String query = "q=" + latitude + ", " + longitude + "&gflags=R&appid=" + mAppId;
        try {
            java.net.URI uri = new URI("http", "where.yahooapis.com", "/geocode", query, null);
            URL url = new URL(uri.toASCIIString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    return readWOEID(in);
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

    private static String readWOEID(InputStream in) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        InputSource source = new InputSource(in);
        try {
            String woeid = (String) xPath.evaluate("/ResultSet/Result[1]/woeid", source, XPathConstants.STRING);
            if (woeid != null && woeid.length() > 0) {
                return woeid;
            }
        } catch (XPathExpressionException e) {
            Log.e(TAG, "Failed to parse data", e);
        }
        return null;
    }
}
