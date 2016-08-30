package com.example.lxl_z.alpha1.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class Response {
    public String city;
    public int rev;

    public List<Weather> weather;
    public List<AQI> aqi;

    public Response(String c) {
        city = c;
        rev = 0;
    }

    public Response(Response r) {
        city = r.city;
        rev = r.rev;
        weather = r.weather != null ? new ArrayList<>(r.weather) : null;
        aqi = r.aqi != null ? new ArrayList<>(r.aqi) : null;
    }
}
