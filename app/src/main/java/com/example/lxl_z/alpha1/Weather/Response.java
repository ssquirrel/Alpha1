package com.example.lxl_z.alpha1.Weather;

import java.util.List;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class Response {
    String city;
    int rev;

    public Weather now;
    public List<Weather> forecast;
    public List<AQI> aqi;
}
