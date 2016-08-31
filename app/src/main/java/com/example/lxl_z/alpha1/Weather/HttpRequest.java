package com.example.lxl_z.alpha1.Weather;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class HttpRequest {
    private static InputStream getHttpInputStream(String url) {
        return null;
    }

    static long update = TimeUnit.SECONDS.toMillis(60);
    static long MOCK_TIME = System.currentTimeMillis();

    public static List<AQI> getAQI(String id) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }

        long now = System.currentTimeMillis();
        if (MOCK_TIME / update != now / update)
            MOCK_TIME =  System.currentTimeMillis();;

        List<AQI> aqis = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Random r = new Random();
            int Low = 50;
            int High = 300;
            int Result = r.nextInt(High - Low) + Low;

            AQI aqi = new AQI();
            aqi.time = MOCK_TIME;
            aqi.aqi = Result;

            aqis.add(aqi);
        }


        return aqis;
    }

    public static Weather getWeather(String id) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }

        long now = System.currentTimeMillis();
        if (MOCK_TIME / update != now / update)
            MOCK_TIME = System.currentTimeMillis();

        Random r = new Random();
        int Low = 0;
        int High = 40;
        int Result = r.nextInt(High - Low) + Low;

        Weather weather = new Weather();
        weather.time = MOCK_TIME;
        weather.temp = Result;

        return weather;
    }

    public static List<Weather> getForecast(String id) {
        return null;
    }
}
