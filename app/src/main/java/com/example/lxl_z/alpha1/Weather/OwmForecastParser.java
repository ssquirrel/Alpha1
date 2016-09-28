package com.example.lxl_z.alpha1.Weather;

import android.util.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by LXL_z on 9/27/2016.
 */

final class OwmForecastParser {
    private final DateFormat dateFormat =
            new SimpleDateFormat("h:mm a", Locale.US);

    OwmForecastParser() {
        dateFormat.setTimeZone(AsyncWeatherService.TIME_ZONE);
    }

    OwmForecast parse(InputStream in) throws IOException {
        try (JsonReader reader =
                     new JsonReader(new BufferedReader(new InputStreamReader(in)))) {

            OwmForecast owmForecast = new OwmForecast();
            owmForecast.hourly = new ArrayList<>();

            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "list":
                        parseList(reader, owmForecast);
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            return owmForecast;
        }
    }

    private void parseList(JsonReader reader,
                           OwmForecast owmForecast) throws IOException {
        reader.beginArray();

        parseFirstElem(reader, owmForecast);

        while (reader.hasNext()) {
            if (owmForecast.hourly.size() >= 8) {
                reader.skipValue();
                continue;
            }

            OwmForecast.HourlyForecast forecast = new OwmForecast.HourlyForecast();

            parseListElem(reader, forecast);

            owmForecast.hourly.add(forecast);
        }
        reader.endArray();
    }

    private void parseFirstElem(JsonReader reader,
                                OwmForecast owmForecast) throws IOException {
        OwmForecast.HourlyForecast forecast = new OwmForecast.HourlyForecast();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "dt":
                    long time = TimeUnit.SECONDS.toMillis(reader.nextLong());
                    owmForecast.clock.lastModified = time;

                    forecast.time = dateFormat.format(time);
                    break;
                case "main":
                    parseMain(reader, forecast);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        owmForecast.hourly.add(forecast);
    }

    private void parseListElem(JsonReader reader,
                               OwmForecast.HourlyForecast forecast) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "dt":
                    long time = TimeUnit.SECONDS.toMillis(reader.nextLong());
                    forecast.time = dateFormat.format(time);
                    break;
                case "main":
                    parseMain(reader, forecast);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseMain(JsonReader reader,
                           OwmForecast.HourlyForecast forecast) throws IOException {
        reader.beginObject();

        while (reader.hasNext()) {

            switch (reader.nextName()) {
                case "temp":
                    forecast.temp = (int) Math.round(reader.nextDouble());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
    }
}