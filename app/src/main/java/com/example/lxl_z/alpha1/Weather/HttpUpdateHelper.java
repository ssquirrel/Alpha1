package com.example.lxl_z.alpha1.Weather;

import android.util.JsonReader;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by LXL_z on 9/20/2016.
 */

final class HttpUpdateHelper {
    private HttpUpdateHelper() {
    }

    private static final HeWeatherParser HE_WEATHER_PARSER = new HeWeatherParser();
    private static final OwmForecastParser OWM_FORECAST_PARSER = new OwmForecastParser();

    static HeWeather getHeWeather(String id) throws IOException, ParseException {
        String url = "https://api.heweather.com/x3/weather?cityid=" +
                id +
                "&key=0a16c8f4c43e4e7cb2fb1a3d6895069f";

        HttpURLConnection conn = getConnection(url);

        try (InputStream in = conn.getInputStream()) {
            return HE_WEATHER_PARSER.parse(in);
        } finally {
            conn.disconnect();
        }
    }

    static OwmForecast getOwmForecast(String id) throws IOException {
        String url = "http://api.openweathermap.org/data/2.5/forecast?id=" +
                id +
                "&units=metric&appid=a4b4bf207d40748201b495ef6528aaae";
        HttpURLConnection conn = getConnection(url);

        try (InputStream in = conn.getInputStream()) {
            return OWM_FORECAST_PARSER.parse(in);
        } finally {
            conn.disconnect();
        }
    }

    private static HttpURLConnection getConnection(String u) throws IOException {
        URL url = new URL(u);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");

        return conn;
    }
}
