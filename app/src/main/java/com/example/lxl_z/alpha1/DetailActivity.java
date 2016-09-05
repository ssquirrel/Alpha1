package com.example.lxl_z.alpha1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.lxl_z.alpha1.Weather.Weather;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    ChartView dailyForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dailyForecast = (ChartView) findViewById(R.id.daily_forecast);

        List<ChartView.DataPoint> data = new ArrayList<>();

        data.add(new ChartView.DataPoint("8 PM", 32));
        data.add(new ChartView.DataPoint("11 PM", 33));
        data.add(new ChartView.DataPoint("2 AM", 32));
        data.add(new ChartView.DataPoint("5 AM", 28));

        data.add(new ChartView.DataPoint("8 AM", 26));
        data.add(new ChartView.DataPoint("11 AM", 24));
        data.add(new ChartView.DataPoint("2 PM", 23));
        data.add(new ChartView.DataPoint("2 PM", 27));

        dailyForecast.refreshView(data);
    }
}
