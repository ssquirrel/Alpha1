package com.example.lxl_z.alpha1.Weather;

import java.util.List;

/**
 * Created by Administrator on 8/31/2016.
 */
public class WeatherUpdateHelper {
    public String city;

    public CityID id;

    public Weather weather;
    public List<AQI> aqi;
    public List<Weather> forecast;

    public UpdateTimer aqiTimer = new UpdateTimer(HttpRequest.update);
    public UpdateTimer weatherTimer = new UpdateTimer(HttpRequest.update);


    //public UpdateTimer aqiTimer = new UpdateTimer(TimeUnit.HOURS.toMillis(1));
    //public UpdateTimer weatherTimer = new UpdateTimer(TimeUnit.HOURS.toMillis(1));
    //public UpdateTimer forecast = new UpdateTimer(TimeUnit.DAYS.toMillis(1));

    public boolean updateAQI() {
        List<AQI> result = HttpRequest.getAQI(id.stateAirID);

        if (aqi == null || aqi.get(0).time != result.get(0).time) {
            aqi = result;
            return true;
        }

        return false;
    }

    public boolean updateCurrentWeather() {
        Weather result = HttpRequest.getWeather(id.owmID);

        if (weather == null || weather.time != result.time) {
            weather = result;
            return true;
        }

        return false;
    }

    public static class UpdateTimer {
        private long interval;
        private long retryInterval;

        private long lastAttempt;
        private long lastSuccess;

        UpdateTimer(long i) {
            interval = i;
            retryInterval = i / 6;
            lastAttempt = 0;
            lastSuccess = 0;
        }


        public boolean check() {
            long now = System.currentTimeMillis();

            if (lastSuccess / interval == now / interval)
                return false;

            boolean retry = lastAttempt / retryInterval != now / retryInterval;

            lastAttempt = now;

            return retry;
        }

        public void setAttemptResult(boolean result) {
            if (result)
                lastSuccess = lastAttempt;
        }
    }
}
