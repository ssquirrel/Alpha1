package com.example.lxl_z.alpha1.Weather;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by LXL_z on 9/12/2016.
 */
public class OwmForecast {
    public Clock clock;
    public List<HourlyForecast> hourly;

    OwmForecast() {
        clock = new Clock(TimeUnit.HOURS.toMillis(1));
    }

    public static class HourlyForecast {
        public String time;
        public int temp;
    }

}
