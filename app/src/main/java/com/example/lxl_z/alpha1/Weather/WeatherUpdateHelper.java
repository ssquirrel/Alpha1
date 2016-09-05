package com.example.lxl_z.alpha1.Weather;

import android.util.JsonReader;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 8/31/2016.
 */
public class WeatherUpdateHelper {
    private UpdateTimer aqiTimer = new UpdateTimer(TimeUnit.HOURS.toMillis(1));
    private UpdateTimer weatherTimer = new UpdateTimer(TimeUnit.HOURS.toMillis(1));
    private UpdateTimer forecastTimer = new UpdateTimer(TimeUnit.DAYS.toMillis(1));

    public void updateAQI(Response response, String id) {
        if (id == null)
            return;

        if (aqiTimer.check()) {
            List<AQI> result = httpGetAQI(id);

            if (response.aqi == null ||
                    response.aqi.get(0).time != result.get(0).time)
                response.aqi = result;

            aqiTimer.setAttemptResult(result.get(0).time);
        }
    }

    public void updateCurrentWeather(Response response, String id) {
        if (weatherTimer.check()) {
            Weather result = httpGetWeather(id);

            if (response.weather == null ||
                    response.weather.time != result.time)
                response.weather = result;

            weatherTimer.setAttemptResult(result.time);
        }
    }

    public void updateForecast(Response response, String id) {
        if (forecastTimer.check()) {
            List<Weather> result = httpGetForecast(id);

            if (response.forecast == null ||
                    response.forecast.get(0).time != result.get(0).time)
                response.forecast = result;

            weatherTimer.setAttemptResult(result.get(0).time);
        }
    }

    public static class UpdateTimer {
        private long interval;
        private long retryInterval;

        private long lastAttempt;
        private long lastSuccess;


        UpdateTimer(long i) {
            interval = i;
            retryInterval = i / 6;

            lastAttempt = 0;
            lastSuccess = 0;

        }

        public boolean check() {
            long now = System.currentTimeMillis();

            if (lastSuccess / interval == now / interval)
                return false;

            boolean retry = lastAttempt / retryInterval != now / retryInterval;

            lastAttempt = now;

            return retry;

        }

        public void setAttemptResult(long time) {
            if (System.currentTimeMillis() / interval == time / interval)
                lastSuccess = lastAttempt;
        }

    }

    private static InputStream getHttpInputStream(String u) {
        try {
            URL url = new URL(u);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            conn.connect();

            return conn.getInputStream();
        } catch (IOException e) {
            //Log.d("InputStream", e.getMessage());
            throw new RuntimeException("getHttpInputStream error");
        }
    }

    private static List<AQI> httpGetAQI(String id) {
        Log.d("httpGetAQI", System.currentTimeMillis() + "");

        try (InputStream in =
                     getHttpInputStream("http://www.stateair.net/web/rss/1/" + id + ".xml")) {

            AQI aqi = new AQI();
            List<AQI> data = new ArrayList<>();

            int count = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss aa");


            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(new BufferedInputStream(in), "UTF-8");

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                if (parser.getName().equals("AQI")) {
                    parser.next();
                    count++;

                    aqi.aqi = Integer.parseInt(parser.getText());
                } else if (parser.getName().equals("ReadingDateTime")) {
                    parser.next();
                    count++;

                    aqi.time = sdf.parse(parser.getText()).getTime();
                }

                if (count == 2) {
                    data.add(aqi);

                    aqi = new AQI();
                    count = 0;
                }

            }

            return data;

        } catch (IOException e) {
            Log.e("RssFetcher", e.toString());
        } catch (XmlPullParserException e) {
            Log.d("XmlPullParserException", e.toString());
        } catch (ParseException e) {
            Log.e("ParseException", e.toString());
        }

        return null;
    }

    private static Weather httpGetWeather(String id) {
        Log.d("httpGetWeather", System.currentTimeMillis() + "");

        String url = "http://api.openweathermap.org/data/2.5/weather?id=" +
                id +
                "&units=metric&appid=a4b4bf207d40748201b495ef6528aaae";

        InputStream in = getHttpInputStream(url);

        Weather weather = new Weather();
        try (JsonReader reader =
                     new JsonReader(new BufferedReader(new InputStreamReader(in, "UTF-8")))) {

            WeatherJsonParser.parse(reader, weather);

        } catch (IOException e) {
            Log.d("httpGetWeather", e.getMessage());
        }

        return weather;
    }

    private static List<Weather> httpGetForecast(String id) {
        Log.d("httpGetForecast", System.currentTimeMillis() + "");

        String url = "http://api.openweathermap.org/data/2.5/forecast?id=" +
                id +
                "&units=metric&appid=a4b4bf207d40748201b495ef6528aaae";


        InputStream in = getHttpInputStream(url);

        List<Weather> forecast = new ArrayList<>();
        try (JsonReader reader =
                     new JsonReader(new BufferedReader(new InputStreamReader(in, "UTF-8")))) {

            ForecastJsonParser.parse(reader, forecast);

        } catch (IOException e) {
            Log.d("httpGetForecast", e.getMessage());
        }

        return forecast;
    }

    private static class WeatherJsonParser {
        static Weather parse(JsonReader reader, Weather weather) throws IOException {
            reader.beginObject();
            while (reader.hasNext()) {

                switch (reader.nextName()) {
                    case "weather":
                        parseWeather(reader, weather);
                        break;
                    case "main":
                        parseMain(reader, weather);
                        break;
                    case "dt":
                        weather.time = TimeUnit.SECONDS.toMillis(reader.nextLong());
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            return weather;
        }

        static void parseWeather(JsonReader reader, Weather weather) throws IOException {
            reader.beginArray();
            reader.beginObject();
            while (reader.hasNext()) {

                switch (reader.nextName()) {
                    case "description":
                        weather.description = reader.nextString();
                        break;
                    case "icon":
                        weather.icon = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            reader.endArray();
        }

        static void parseMain(JsonReader reader, Weather weather) throws IOException {
            reader.beginObject();

            while (reader.hasNext()) {

                switch (reader.nextName()) {
                    case "temp":
                        weather.temp = reader.nextDouble();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            reader.endObject();
        }
    }

    private static class ForecastJsonParser {
        static List<Weather> parse(JsonReader reader, List<Weather> forecast) throws IOException {
            reader.beginObject();
            while (reader.hasNext()) {

                switch (reader.nextName()) {
                    case "list":
                        parseList(reader, forecast);
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            return forecast;
        }

        static List<Weather> parseList(JsonReader reader, List<Weather> forecast) throws IOException {
            reader.beginArray();
            while (reader.hasNext()) {
                forecast.add(new Weather());

                WeatherJsonParser.parse(reader, forecast.get(forecast.size() - 1));
            }
            reader.endArray();

            return forecast;
        }
    }

    /*
     static long MOCK_UPDATE = TimeUnit.SECONDS.toMillis(60);
    static long MOCK_WAIT = MOCK_UPDATE / 2;

    static long MOCK_TIME() {
        long now = System.currentTimeMillis();

        if (now % MOCK_UPDATE >= MOCK_WAIT)
            now = now - now % MOCK_UPDATE + MOCK_WAIT;
        else
            now = now - now % MOCK_UPDATE - MOCK_UPDATE + MOCK_WAIT;

        return now;
    }

    private static List<AQI> httpGetAQI(String id) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }

        long time = MOCK_TIME();

        List<AQI> aqis = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Random r = new Random();
            int Low = 50;
            int High = 300;
            int Result = r.nextInt(High - Low) + Low;

            AQI aqi = new AQI();
            aqi.time = time;
            aqi.aqi = Result;

            aqis.add(aqi);
        }


        return aqis;
    }

    public static Weather httpGetWeather(String id) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        Random r = new Random();
        int Low = 0;
        int High = 40;
        int Result = r.nextInt(High - Low) + Low;

        Weather weather = new Weather();
        weather.time = MOCK_TIME();
        weather.temp = Result;

        return weather;
    }
*/


}
