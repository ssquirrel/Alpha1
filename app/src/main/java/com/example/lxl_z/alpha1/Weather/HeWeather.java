package com.example.lxl_z.alpha1.Weather;


import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class HeWeather {
    public Clock clock;

    public int temp;
    public String cond;
    public int iconResId;
    public int relativeTemp;
    public int aqi;
    public int precipitation;
    public int humidity;
    public List<DailyForecast> daily;

    HeWeather() {
        clock = new Clock(TimeUnit.HOURS.toMillis(1));
    }

    public static class DailyForecast {
        public String dayOfWeak;
        public int iconResId;
        public int max;
        public int min;
    }
}
