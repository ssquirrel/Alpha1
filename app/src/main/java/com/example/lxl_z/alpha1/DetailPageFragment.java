package com.example.lxl_z.alpha1;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;
import com.example.lxl_z.alpha1.Weather.Response;
import com.example.lxl_z.alpha1.Weather.Weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 9/5/2016.
 */
public class DetailPageFragment extends Fragment
        implements AsyncWeatherService.OnLoadDoneCallback {

    TextView city;
    TextView time;
    TextView temp;

    ChartView chartView;

    TableLayout table;

    AsyncWeatherService.AsyncWeatherTask task;

    public DetailPageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_page, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String cc = getArguments().getString(SearchActivity.EXTRA_CITY);

        city = (TextView) getView().findViewById(R.id.city);
        time = (TextView) getView().findViewById(R.id.time_and_description);
        temp = (TextView) getView().findViewById(R.id.temp);

        chartView = (ChartView) getView().findViewById(R.id.hourly_forecast);

        table = (TableLayout) getView().findViewById(R.id.forecast_table);

        city.setText(cc);

        task = AsyncWeatherService.getInstance(getContext()).newDetailWeatherTask(this);
        task.execute(Collections.singletonList(cc));
    }

    @Override
    public void onLoadDone(Response response) {
        Resources resources = getResources();

        TimeZone TIMEZONE_CST = TimeZone.getTimeZone("GMT+0800");

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, h:mm a", Locale.US);
        sdf.setTimeZone(TIMEZONE_CST);

        time.setText(resources.getString(R.string.time_string,
                sdf.format(response.weather.time),
                response.weather.description));

        temp.setText(resources.getString(R.string.temp_string, response.weather.getRoundedTemp()));

        List<ChartView.DataPoint> data = new ArrayList<>();
        data.add(new ChartView.DataPoint(null, response.weather.getRoundedTemp()));

        sdf.applyPattern("h:mm a");
        for (int i = 0; i < 7; i++) {
            Weather forecast = response.forecast.get(i);

            data.add(new ChartView.DataPoint(sdf.format(forecast.time), forecast.getRoundedTemp()));
        }

        chartView.refreshView(data);


        Calendar calendar = Calendar.getInstance(TIMEZONE_CST);

        calendar.setTimeInMillis(response.forecast.get(response.forecast.size() - 1).time);
        int LAST_DAY_OF_WEEK = calendar.get(Calendar.DAY_OF_WEEK);

        calendar.setTimeInMillis(response.weather.time);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        int idx = 0;

        for (; idx < response.forecast.size(); idx++) {
            calendar.setTimeInMillis(response.forecast.get(idx).time);

            if (day != calendar.get(Calendar.DAY_OF_WEEK))
                break;
        }

        day = calendar.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);

            if (idx == response.forecast.size())
                break;

            ((TextView) row.getChildAt(0)).setText(
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US));

            int temp_max = Integer.MIN_VALUE;
            int temp_min = Integer.MAX_VALUE;

            for (; idx < response.forecast.size(); idx++) {
                calendar.setTimeInMillis(response.forecast.get(idx).time);

                if (day != calendar.get(Calendar.DAY_OF_WEEK))
                    break;

                int temp = response.forecast.get(idx).getRoundedTemp();

                if (temp_max < temp) {
                    temp_max = temp;
                }

                if (temp_min > temp)
                    temp_min = temp;
            }

            ((TextView) row.getChildAt(1)).setText(String.valueOf(temp_max + " "));
            ((TextView) row.getChildAt(2)).setText(String.valueOf(temp_min));

            day = calendar.get(Calendar.DAY_OF_WEEK);

            if (day == LAST_DAY_OF_WEEK)
                break;
        }


    }
}
