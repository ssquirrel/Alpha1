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

        data.add(new ChartView.DataPoint(11, 36));
        data.add(new ChartView.DataPoint(14, 37));
        data.add(new ChartView.DataPoint(17, 35));
        data.add(new ChartView.DataPoint(20, 26));

        data.add(new ChartView.DataPoint(23, 22));
        data.add(new ChartView.DataPoint(2, 20));
        data.add(new ChartView.DataPoint(5, 18));
        data.add(new ChartView.DataPoint(8, 26));

        dailyForecast.refreshView(data);
    }
}
