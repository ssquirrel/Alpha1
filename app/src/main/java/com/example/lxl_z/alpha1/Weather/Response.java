package com.example.lxl_z.alpha1.Weather;


import java.io.IOException;
import java.text.ParseException;

/**
 * Created by LXL_z on 8/27/2016.
 * <p>
 * Response is an abstraction of all data obtained from data sources. It is used both internally
 * and exposed externally. Given the requirement, this class is designed to immutable so as to
 * allow safe publication without additional synchronization. It is also suspected that Android
 * handler itself provided sufficient synchronization for shared variables. Unfortunately, I have
 * not yet encounter a single definitive source to confirm the speculation.
 * <p>
 * Two of the most important fields of Response are heWeather and owmForecast. They are abstractions
 * of their respective data sources. Each also carries a timestamp which specifies the update policy.
 * <p>
 * Current policy expects data sources to be updated on an hourly basis. To handle any delay, the
 * policy further specifies a recheck interval of 1/6 of an hour if the data returned is same as
 * that of last hour. For timestamps to function properly, their continuity must be ensured.
 * In other words, a new Response must be created to store data that have different timestamps.
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

    /*
    * This method attempts to create a brand new Response by making http requests and parsing http
    * responses.
    *
    * Should any exception raised during the above process, a new response will still be created
    * but with isValid field set to false. This is done to ensure the continuity of timestamps held
    * by its fields, namely, heWeather and owmForecast.
    * */

    static Response update(String city, CityID id) {
        try {
            return new Response(city,
                    HttpUpdateHelper.getHeWeather(id.hwID),
                    HttpUpdateHelper.getOwmForecast(id.owmID));
        } catch (IOException | ParseException e) {
            return new Response(city);
        }
    }

    /*
    * This method attempt to update the Response by checking timestamps of its fields. Should any
    * of them becomes invalid, a new Response will be created. It should be noted that an invalid
    * timestamp doesn't necessarily invalidate its data (usually there is a significant delay
    * between the excepted time and the actual update time). However, a new Response must still be
    * created to ensure the continuity of timestamps.
    *
    * Should any exception raised during the above process, a new response will still be created
    * but with isValid field set to false. This is done to ensure the continuity of timestamps held
    * by its fields, namely, heWeather and owmForecast.
    * */

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
