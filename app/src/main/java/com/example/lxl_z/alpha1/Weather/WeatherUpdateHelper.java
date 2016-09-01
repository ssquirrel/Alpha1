package com.example.lxl_z.alpha1.Weather;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 8/31/2016.
 */
public class WeatherUpdateHelper {
    private UpdateTimer aqiTimer = new UpdateTimer(MOCK_UPDATE);
    private UpdateTimer weatherTimer = new UpdateTimer(MOCK_UPDATE);

    //public UpdateTimer aqiTimer = new UpdateTimer(TimeUnit.HOURS.toMillis(1));
    //public UpdateTimer weatherTimer = new UpdateTimer(TimeUnit.HOURS.toMillis(1));
    //public UpdateTimer forecast = new UpdateTimer(TimeUnit.DAYS.toMillis(1));

    public List<AQI> updateAQI(String id, long time) {

        if (aqiTimer.check()) {
            List<AQI> result = httpGetAQI(id);

            boolean success = time != result.get(0).time;

            aqiTimer.setAttemptResult(result.get(0).time);

            if (success) {
                return result;
            }
        }
        return null;
    }

    public Weather updateCurrentWeather(String id, long time) {
        if (weatherTimer.check()) {
            Weather result = httpGetWeather(id);

            boolean success = time != result.time;

            weatherTimer.setAttemptResult(result.time);

            if (success)
                return result;
        }

        return null;
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

        public void setAttemptResult(long time) {
            if (System.currentTimeMillis() / interval == time / interval)
                lastSuccess = lastAttempt;
        }

    }

    private static InputStream getHttpInputStream(String url) {
        return null;
    }

    static long MOCK_UPDATE = TimeUnit.SECONDS.toMillis(60);
    static long MOCK_WAIT = MOCK_UPDATE / 2;

    static long MOCK_TIME() {
        long now = System.currentTimeMillis();

        if (now % MOCK_UPDATE >= MOCK_WAIT)
            now = now - now % MOCK_UPDATE + MOCK_WAIT;
        else
            now = now - now % MOCK_UPDATE - MOCK_UPDATE + MOCK_WAIT;

        return now;
    }

    private static List<AQI> httpGetAQI(String id) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }

        long time = MOCK_TIME();

        List<AQI> aqis = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Random r = new Random();
            int Low = 50;
            int High = 300;
            int Result = r.nextInt(High - Low) + Low;

            AQI aqi = new AQI();
            aqi.time = time;
            aqi.aqi = Result;

            aqis.add(aqi);
        }


        return aqis;
    }

    public static Weather httpGetWeather(String id) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        Random r = new Random();
        int Low = 0;
        int High = 40;
        int Result = r.nextInt(High - Low) + Low;

        Weather weather = new Weather();
        weather.time = MOCK_TIME();
        weather.temp = Result;

        return weather;
    }

    public static List<Weather> httpGetForecast(String id) {
        return null;
    }
}
