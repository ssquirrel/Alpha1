package com.example.lxl_z.alpha1;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;
import com.example.lxl_z.alpha1.Weather.Response;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity
        implements AsyncWeatherService.OnLoadDoneListener {
    static final String EXTRA_CITY = "EXTRA_CITY";

    private static final DateFormat descriptionFormat =
            new SimpleDateFormat("E h:mm a", Locale.US);

    static {
        descriptionFormat.setTimeZone(AsyncWeatherService.TIME_ZONE);
    }

    List<String> cities;
    List<String> descriptions;

    ActionBar actionBar;

    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        List<String> favorites = intent.getStringArrayListExtra(FavoritesActivity.EXTRA_FAVORITES);
        int index = intent.getIntExtra(FavoritesActivity.EXTRA_INDEX, -1);

        if (favorites == null || index == -1)
            throw new RuntimeException("Intent must carry proper data");

        cities = Collections.unmodifiableList(favorites);
        descriptions = new ArrayList<>(Collections.nCopies(cities.size(), ""));


        actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new RuntimeException("Use a theme that includes action bar");
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(cities.get(index));

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return cities.size();
            }

            @Override
            public Fragment getItem(int position) {
                Bundle bundle = new Bundle();
                bundle.putString(DetailActivity.EXTRA_CITY, cities.get(position));

                Fragment fragment = new DetailPageFragment();
                fragment.setArguments(bundle);

                return fragment;
            }
        });
        pager.setCurrentItem(index);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int state) {
                actionBar.setTitle(cities.get(state));
                actionBar.setSubtitle(descriptions.get(state));
            }
        });
    }

    @Override
    public void onLoadDone(Response response) {
        int idx = cities.indexOf(response.city);

        String description = descriptionFormat.format(response.heWeather.clock.lastModified) +
                ", " + response.heWeather.cond;

        descriptions.set(idx, description);

        if (idx == pager.getCurrentItem())
            actionBar.setSubtitle(descriptions.get(idx));
    }
}
