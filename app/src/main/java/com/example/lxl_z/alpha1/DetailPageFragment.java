package com.example.lxl_z.alpha1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;
import com.example.lxl_z.alpha1.Weather.HeWeather;
import com.example.lxl_z.alpha1.Weather.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 9/5/2016.
 */
public class DetailPageFragment extends EmptyStateDetailFragment
        implements AsyncWeatherService.OnLoadDoneListener {
    String city;

    TextView temp;
    ImageView icon;
    TextView relativeTemp;
    TextView aqi;
    TextView precipitation;
    TextView humidity;

    ChartView chartView;

    private TextView daysOfWeak[];
    private ImageView forecastIcons[];
    private TextView maxTemps[];
    private TextView minTemps[];

    AsyncWeatherService.AsyncWeather asyncWeather;

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

        final View view = getView();

        temp = (TextView) view.findViewById(R.id.temp);
        icon = (ImageView) view.findViewById(R.id.icon);
        relativeTemp = (TextView) view.findViewById(R.id.relative_temp);
        aqi = (TextView) view.findViewById(R.id.aqi);
        precipitation = (TextView) view.findViewById(R.id.precipitation);
        humidity = (TextView) view.findViewById(R.id.humidity);

        chartView = (ChartView) view.findViewById(R.id.hourly_chart);


        LinearLayout forecastContainer = (LinearLayout) view.findViewById(R.id.daily_forecast);
        final int LENGTH = forecastContainer.getChildCount();


        daysOfWeak = new TextView[LENGTH];
        forecastIcons = new ImageView[LENGTH];
        maxTemps = new TextView[LENGTH];
        minTemps = new TextView[LENGTH];


        for (int i = 0; i < LENGTH; i++) {
            LinearLayout ll = (LinearLayout) forecastContainer.getChildAt(i);

            daysOfWeak[i] = (TextView) ll.getChildAt(0);
            forecastIcons[i] = (ImageView) ll.getChildAt(1);
            maxTemps[i] = (TextView) ll.getChildAt(2);
            minTemps[i] = (TextView) ll.getChildAt(3);
        }

        city = getArguments().getString(DetailActivity.EXTRA_CITY);

        asyncWeather =
                AsyncWeatherService.getInstance(getContext()).getAsyncWeather(this);

        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingState();
                asyncWeather.force(Collections.singletonList(city));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getState() == EmptyStateDetailFragment.ERROR) {
            loadingState();
        }

        asyncWeather.fetch(Collections.singletonList(city));
    }

    @Override
    public void onLoadDone(Response response) {
        if (!isAdded())
            return;

        if (!response.isValid) {
            errorState();
            return;
        }

        okState();

        AsyncWeatherService.OnLoadDoneListener listener =
                (AsyncWeatherService.OnLoadDoneListener) getActivity();

        listener.onLoadDone(response);


        List<ChartView.DataPoint> dataPoints = new ArrayList<>();
        dataPoints.add(new ChartView.DataPoint(null, response.heWeather.temp));

        for (int i = 0; i < 7; i++) {
            ChartView.DataPoint dp =
                    new ChartView.DataPoint(response.owmForecast.hourly.get(i).time,
                            response.owmForecast.hourly.get(i).temp);
            dataPoints.add(dp);
        }

        chartView.refreshView(dataPoints);

        temp.setText(getString(R.string.temp_string, response.heWeather.temp));
        icon.setImageResource(response.heWeather.iconResId);
        relativeTemp.setText(getString(R.string.temp_string, response.heWeather.relativeTemp));
        aqi.setText(String.valueOf(response.heWeather.aqi));
        precipitation.setText(getString(R.string.percent_string, response.heWeather.precipitation));
        humidity.setText(getString(R.string.percent_string, response.heWeather.humidity));

        for (int i = 0; i < forecastIcons.length; i++) {
            HeWeather.DailyForecast forecast = response.heWeather.daily.get(i);

            daysOfWeak[i].setText(forecast.dayOfWeak);
            forecastIcons[i].setImageResource(forecast.iconResId);
            maxTemps[i].setText(getString(R.string.temp_string, forecast.max));
            minTemps[i].setText(getString(R.string.temp_string, forecast.min));
        }
    }

}
