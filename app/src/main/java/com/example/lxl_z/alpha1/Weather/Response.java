package com.example.lxl_z.alpha1.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class Response {
    public final String city;
    public int rev;

    public Weather current;
    public List<Weather> forecast;
    public List<AQI> aqi;

    public Response(String c) {
        city = c;
        rev = 0;
    }

    public Response(Response r) {
        city = r.city;
        rev = r.rev;
        current = new Weather(current);
        forecast = new ArrayList<>(r.forecast);
        aqi = new ArrayList<>(r.aqi);
    }
}
