package com.example.lxl_z.alpha1.Weather;


import java.io.IOException;
import java.text.ParseException;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class Response {
    public final String city;
    public final boolean isValid;
    public final HeWeather heWeather;
    public final OwmForecast owmForecast;

    private Response(String c) {
        city = c;
        isValid = false;
        heWeather = new HeWeather();
        owmForecast = new OwmForecast();
    }

    private Response(String c, HeWeather hw, OwmForecast of) {
        city = c;
        isValid = true;
        heWeather = hw;
        owmForecast = of;
    }

    static Response update(String city, CityID id) {
        try {
            return new Response(city,
                    HttpUpdateHelper.getHeWeather(id.hwID),
                    HttpUpdateHelper.getOwmForecast(id.owmID));
        } catch (IOException | ParseException e) {
            return new Response(city);
        }
    }

    static Response update(Response re, CityID id) {
        try {
            HeWeather hw = re.heWeather;
            OwmForecast of = re.owmForecast;

            if (!hw.clock.isValid())
                hw = HttpUpdateHelper.getHeWeather(id.hwID);

            if (!of.clock.isValid())
                of = HttpUpdateHelper.getOwmForecast(id.owmID);

            if (hw == re.heWeather && of == re.owmForecast)
                return re;

            return new Response(re.city, hw, of);
        } catch (IOException | ParseException e) {
            return new Response(re.city);
        }
    }

}
