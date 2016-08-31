package com.example.lxl_z.alpha1.Weather;


/**
 * Created by LXL_z on 8/27/2016.
 */
public class Weather {
    public long time;
    public double temp;

    public Weather() {

    }

    public Weather(Weather w) {
        time = w.time;
        temp = w.temp;
    }
}
