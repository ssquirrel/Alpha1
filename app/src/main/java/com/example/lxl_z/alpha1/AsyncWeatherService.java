package com.example.lxl_z.alpha1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.lxl_z.alpha1.Weather.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class AsyncWeatherService {
    private static AsyncWeatherService instance = new AsyncWeatherService();

    private Map<String, Response> cache = new HashMap<>();

    static AsyncWeatherService getInstance() {
        return instance;
    }

    AsyncWeatherTask newCurrentWeatherTask() {
        return null;
    }

    AsyncWeatherTask newFullDetailTask() {
        return null;
    }

    interface OnLoadDoneCallback {
        void onLoadDone(Response response);
    }

    static abstract class AsyncWeatherTask {
        private WeakReference<OnLoadDoneCallback> weakCallback;
        private Response response;

        void execute(List<Request> requests) {

        }
    }

    private void addNewCity(String city, String id){

    }


}
