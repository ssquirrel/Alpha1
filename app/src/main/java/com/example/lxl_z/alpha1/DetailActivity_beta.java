package com.example.lxl_z.alpha1;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity_beta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_beta);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Beijing");
        getSupportActionBar().setSubtitle("Fri, 4:16PM, overcast clouds");

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new DetailPageFragment_beta();
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int state) {
                if (state == 0) {
                    getSupportActionBar().setTitle("Beijing");
                    getSupportActionBar().setSubtitle("Fri, 4:16PM, overcast clouds");
                }
                else {
                    getSupportActionBar().setTitle("Shanghai");
                    getSupportActionBar().setSubtitle("Fri, 4:10PM, overcast clouds");
                }
            }
        });
    }
}
