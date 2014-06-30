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

package ch.corten.aha.worldclock.weather;

import java.util.Date;

public abstract class AbstractObservation implements WeatherObservation {

    private Date mUpdateTime;
    private Double mTemperature;
    private String mCondition;
    private int mConditionCode;
    private Double mWindSpeed;
    private String mWindDirection;
    private Double mHumidity;

    protected AbstractObservation() {
        mUpdateTime = new Date();
    }

    protected AbstractObservation(String errorMsg) {
        mUpdateTime = new Date();
        mCondition = errorMsg;
        mConditionCode = ERROR;
    }

    @Override
    public Double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public void setWeatherCondition(String condition) {
        mCondition = condition;
    }

    public void setWindSpeed(Double speed) {
        mWindSpeed = speed;
    }

    @Override
    public int getConditionCode() {
        return mConditionCode;
    }

    protected void setConditionCode(int code) {
        mConditionCode = code;
    }

    @Override
    public Double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(Double humidity) {
        mHumidity = humidity;
    }

    @Override
    public Date getUpdateTime() {
        return mUpdateTime;
    }

    @Override
    public String getWeatherCondition() {
        return mCondition;
    }

    @Override
    public String getWindDirection() {
        return mWindDirection;
    }

    public void setWindDirection(String direction) {
        mWindDirection = direction;
    }

    @Override
    public Double getWindSpeed() {
        return mWindSpeed;
    }

}
