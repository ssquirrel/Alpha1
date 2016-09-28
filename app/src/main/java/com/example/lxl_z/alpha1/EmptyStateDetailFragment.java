package com.example.lxl_z.alpha1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;

import java.util.Calendar;

/**
 * Created by LXL_z on 9/28/2016.
 */

public abstract class EmptyStateDetailFragment extends Fragment {
    protected FrameLayout loading;
    protected FrameLayout error;
    protected ScrollView ok;

    protected static final int LOADING = 1;
    protected static final int ERROR = 1;
    protected static final int OK = 1;

    private int state = LOADING;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final View view = getView();

        loading = (FrameLayout) view.findViewById(R.id.loading);
        error = (FrameLayout) view.findViewById(R.id.error);
        ok = (ScrollView) view.findViewById(R.id.ok);
    }

    int getState() {
        return state;
    }

    void loadingState() {
        loading.setVisibility(View.VISIBLE);
        error.setVisibility(View.INVISIBLE);
        ok.setVisibility(View.INVISIBLE);

        state = LOADING;
    }

    void errorState() {
        error.setVisibility(View.VISIBLE);
        loading.setVisibility(View.INVISIBLE);
        ok.setVisibility(View.INVISIBLE);

        state = ERROR;
    }

    void okState() {
        ok.setVisibility(View.VISIBLE);
        error.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.INVISIBLE);

        state = OK;
    }
}
