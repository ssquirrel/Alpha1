package com.example.lxl_z.alpha1.Weather;


/**
 * Created by LXL_z on 8/27/2016.
 */
public class Weather {
    public final long time;
    public final double temp;

    public Weather(long t, double tp) {
        time = t;
        temp = tp;
    }

    public Weather(Weather w) {
        time = w.time;
        temp = w.temp;
    }
}
