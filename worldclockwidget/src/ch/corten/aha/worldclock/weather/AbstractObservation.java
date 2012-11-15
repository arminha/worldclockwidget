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
