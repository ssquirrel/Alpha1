package com.example.lxl_z.alpha1.Weather;


/**
 * Created by LXL_z on 8/27/2016.
 */
public class Weather {
    public long time;
    public double temp;
    public String description;
    public String icon;

    public Weather() {

    }

    public Weather(long t, double tp) {
        time = t;
        temp = tp;
    }


    public Weather(Weather w) {
        time = w.time;
        temp = w.temp;
        description = w.description;
        icon = w.icon;
    }

    public int getRoundedTemp() {
        return Math.round((float) temp);
    }
}
