package com.example.lxl_z.alpha1.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class Response {
    public String city;

    public Weather weather;
    public List<AQI> aqi;
    public List<Weather> forecast;


    public Response(String c) {
        city = c;
    }
}
