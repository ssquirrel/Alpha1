package com.example.lxl_z.alpha1;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;
import com.example.lxl_z.alpha1.Weather.Response;
import com.example.lxl_z.alpha1.Weather.Weather;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
    List<String> data;

    ViewPager pager;
    PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        data = intent.getStringArrayListExtra(FavoritesActivity.EXTRA_FAVORITES);

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return data.size();
            }

            @Override
            public Fragment getItem(int position) {
                Bundle bundle = new Bundle();
                bundle.putString(SearchActivity.EXTRA_CITY, data.get(position));

                Fragment fragment = new DetailPageFragment();
                fragment.setArguments(bundle);

                return fragment;
            }
        };

        pager.setAdapter(adapter);
        pager.setCurrentItem(intent.getIntExtra(FavoritesActivity.EXTRA_INDEX, -1));
        /*
        if (savedInstanceState != null) {
            data = savedInstanceState.getStringArrayList(EXTRA_CITIES_LIST);
        }
        */
    }


}
