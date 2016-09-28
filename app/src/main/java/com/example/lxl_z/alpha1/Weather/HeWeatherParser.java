package com.example.lxl_z.alpha1.Weather;

import android.util.JsonReader;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.example.lxl_z.alpha1.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by LXL_z on 9/27/2016.
 */

final class HeWeatherParser {
    private final DateFormat lastModifiedFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    private final DateFormat forecastDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final DateFormat forecastDayFormat =
            new SimpleDateFormat("EEEE", Locale.US);


    private final SparseArray<String> condMap = new SparseArray<>();
    private final SparseIntArray iconMap = new SparseIntArray();

    HeWeatherParser() {
        lastModifiedFormat.setTimeZone(AsyncWeatherService.TIME_ZONE);
        forecastDateFormat.setTimeZone(AsyncWeatherService.TIME_ZONE);
        forecastDayFormat.setTimeZone(AsyncWeatherService.TIME_ZONE);

        condMap.put(100, "Sunny");
        condMap.put(101, "Cloudy");
        condMap.put(102, "Few Clouds");
        condMap.put(103, "Partly Cloudy");
        condMap.put(104, "Overcast");
        condMap.put(200, "Windy");
        condMap.put(201, "Calm");
        condMap.put(202, "Light Breeze");
        condMap.put(203, "Gentle Breeze");
        condMap.put(204, "Fresh Breeze");
        condMap.put(205, "Strong Breeze");
        condMap.put(206, "High Wind");
        condMap.put(207, "Gale");
        condMap.put(208, "Strong Gale");
        condMap.put(209, "Storm");
        condMap.put(210, "Violent Storm");
        condMap.put(211, "Hurricane");
        condMap.put(212, "Tornado");
        condMap.put(213, "Tropical Storm");
        condMap.put(300, "Shower Rain");
        condMap.put(301, "Heavy Shower Rain");
        condMap.put(302, "Thundershower");
        condMap.put(303, "Heavy Thunderstorm");
        condMap.put(304, "Hail");
        condMap.put(305, "Light Rain");
        condMap.put(306, "Moderate Rain");
        condMap.put(307, "Heavy Rain");
        condMap.put(308, "Extreme Rain");
        condMap.put(309, "Drizzle Rain");
        condMap.put(310, "Storm");
        condMap.put(311, "Heavy Storm");
        condMap.put(312, "Severe Storm");
        condMap.put(313, "Freezing Rain");
        condMap.put(400, "Light Snow");
        condMap.put(401, "Moderate Snow");
        condMap.put(402, "Heavy Snow");
        condMap.put(403, "Snowstorm");
        condMap.put(404, "Sleet");
        condMap.put(405, "Rain And Snow");
        condMap.put(406, "Shower Snow");
        condMap.put(407, "Snow Flurry");
        condMap.put(500, "Mist");
        condMap.put(501, "Foggy");
        condMap.put(502, "Haze");
        condMap.put(503, "Sand");
        condMap.put(504, "Dust");
        condMap.put(507, "DustStorm");
        condMap.put(508, "Sandstorm");
        condMap.put(900, "Hot");
        condMap.put(901, "Cold");
        condMap.put(999, "Unknown");

        iconMap.put(100, R.drawable.icon100);
        iconMap.put(101, R.drawable.icon101);
        iconMap.put(102, R.drawable.icon102);
        iconMap.put(103, R.drawable.icon103);
        iconMap.put(104, R.drawable.icon104);
        iconMap.put(200, R.drawable.icon200);
        iconMap.put(201, R.drawable.icon201);
        iconMap.put(202, R.drawable.icon202);
        iconMap.put(203, R.drawable.icon203);
        iconMap.put(204, R.drawable.icon204);
        iconMap.put(205, R.drawable.icon205);
        iconMap.put(206, R.drawable.icon206);
        iconMap.put(207, R.drawable.icon207);
        iconMap.put(208, R.drawable.icon208);
        iconMap.put(209, R.drawable.icon209);
        iconMap.put(210, R.drawable.icon210);
        iconMap.put(211, R.drawable.icon211);
        iconMap.put(212, R.drawable.icon212);
        iconMap.put(213, R.drawable.icon213);
        iconMap.put(300, R.drawable.icon300);
        iconMap.put(301, R.drawable.icon301);
        iconMap.put(302, R.drawable.icon302);
        iconMap.put(303, R.drawable.icon303);
        iconMap.put(304, R.drawable.icon304);
        iconMap.put(305, R.drawable.icon305);
        iconMap.put(306, R.drawable.icon306);
        iconMap.put(307, R.drawable.icon307);
        iconMap.put(308, R.drawable.icon308);
        iconMap.put(309, R.drawable.icon309);
        iconMap.put(310, R.drawable.icon310);
        iconMap.put(311, R.drawable.icon311);
        iconMap.put(312, R.drawable.icon312);
        iconMap.put(313, R.drawable.icon313);
        iconMap.put(400, R.drawable.icon400);
        iconMap.put(401, R.drawable.icon401);
        iconMap.put(402, R.drawable.icon402);
        iconMap.put(403, R.drawable.icon403);
        iconMap.put(404, R.drawable.icon404);
        iconMap.put(405, R.drawable.icon405);
        iconMap.put(406, R.drawable.icon406);
        iconMap.put(407, R.drawable.icon407);
        iconMap.put(500, R.drawable.icon500);
        iconMap.put(501, R.drawable.icon501);
        iconMap.put(502, R.drawable.icon502);
        iconMap.put(503, R.drawable.icon503);
        iconMap.put(504, R.drawable.icon504);
        iconMap.put(507, R.drawable.icon507);
        iconMap.put(508, R.drawable.icon508);
        iconMap.put(900, R.drawable.icon900);
        iconMap.put(901, R.drawable.icon901);
        iconMap.put(999, R.drawable.icon999);
    }


    HeWeather parse(InputStream in) throws IOException, ParseException {
        try (JsonReader reader =
                     new JsonReader(new BufferedReader(new InputStreamReader(in)))) {

            HeWeather heWeather = new HeWeather();
            heWeather.daily = new ArrayList<>();

            reader.beginObject();
            reader.nextName();
            reader.beginArray();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "basic":
                        parseBasic(reader, heWeather.clock);
                        break;
                    case "now":
                        parseNow(reader, heWeather);
                        break;
                    case "aqi":
                        parseAQI(reader, heWeather);
                        break;
                    case "daily_forecast":
                        parseDailyForecast(reader, heWeather);
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            reader.endArray();
            reader.endObject();

            return heWeather;
        }
    }

    private void parseBasic(JsonReader reader,
                            Clock clock) throws IOException, ParseException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "update":
                    parseUpdate(reader, clock);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseUpdate(JsonReader reader,
                             Clock clock) throws IOException, ParseException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "loc":
                    clock.lastModified =
                            lastModifiedFormat.parse(reader.nextString()).getTime();

                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseNow(JsonReader reader,
                          HeWeather hw) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "tmp":
                    hw.temp = reader.nextInt();
                    break;
                case "fl":
                    hw.relativeTemp = reader.nextInt();
                    break;
                case "hum":
                    hw.humidity = reader.nextInt();
                    break;
                case "cond":
                    parseCond(reader, hw);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseCond(JsonReader reader,
                           HeWeather hw) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "code":
                    int cond = reader.nextInt();
                    hw.cond = condMap.get(cond);
                    hw.iconResId = iconMap.get(cond);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseAQI(JsonReader reader,
                          HeWeather hw) throws IOException {
        reader.beginObject();
        reader.nextName();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "aqi":
                    hw.aqi = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        reader.endObject();
    }

    private void parseDailyForecast(JsonReader reader,
                                    HeWeather hw) throws IOException, ParseException {
        reader.beginArray();

        parseFirstForecast(reader, hw);

        while (reader.hasNext()) {
            HeWeather.DailyForecast forecast = new HeWeather.DailyForecast();
            parseOtherForecast(reader, forecast);
            hw.daily.add(forecast);
        }
        reader.endArray();
    }

    private void parseFirstForecast(JsonReader reader,
                                    HeWeather hw) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "pop":
                    hw.precipitation = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseOtherForecast(JsonReader reader,
                                    HeWeather.DailyForecast forecast)
            throws IOException, ParseException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "cond":
                    parseForecastCond(reader, forecast);
                    break;
                case "tmp":
                    parseForecastTmp(reader, forecast);
                    break;
                case "date":
                    Date d = forecastDateFormat.parse(reader.nextString());
                    forecast.dayOfWeak = forecastDayFormat.format(d);

                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseForecastTmp(JsonReader reader,
                                  HeWeather.DailyForecast forecast) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "max":
                    forecast.max = reader.nextInt();
                    break;
                case "min":
                    forecast.min = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private void parseForecastCond(JsonReader reader,
                                   HeWeather.DailyForecast forecast) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "code_d":
                    forecast.iconResId = iconMap.get(reader.nextInt());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }
}
