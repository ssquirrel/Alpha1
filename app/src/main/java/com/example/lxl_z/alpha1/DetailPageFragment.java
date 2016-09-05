package com.example.lxl_z.alpha1;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;
import com.example.lxl_z.alpha1.Weather.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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

        city.setText(cc);

        task = AsyncWeatherService.getInstance(getContext()).newDetailWeatherTask(this);
        task.execute(Collections.singletonList(cc));
    }

    @Override
    public void onLoadDone(Response response) {
        Resources resources = getResources();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, h:mm a", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0800"));

        time.setText(resources.getString(R.string.time_string,
                sdf.format(new Date(response.weather.time)),
                response.weather.description));

        temp.setText(resources.getString(R.string.temp_string, (int) response.weather.temp));

    }
}
