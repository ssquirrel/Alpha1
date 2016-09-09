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
public class DetailPageFragment_beta extends Fragment {

    public DetailPageFragment_beta() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_page_beta, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<ChartView.DataPoint> data = new ArrayList<>();
        data.add(new ChartView.DataPoint(null, 32));
        data.add(new ChartView.DataPoint("6 PM", 31));
        data.add(new ChartView.DataPoint("9 PM", 24));
        data.add(new ChartView.DataPoint("12 PM", 22));
        data.add(new ChartView.DataPoint("12 PM", 20));
        data.add(new ChartView.DataPoint("3 AM", 19));
        data.add(new ChartView.DataPoint("6 AM", 23));
        data.add(new ChartView.DataPoint("9 AM", 28));



        ((ChartView) getView().findViewById(R.id.hourly_forecast)).refreshView(data);
    }
}
