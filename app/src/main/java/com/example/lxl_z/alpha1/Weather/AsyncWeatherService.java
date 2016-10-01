package com.example.lxl_z.alpha1.Weather;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class AsyncWeatherService {
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+8");


    private static AsyncWeatherService instance = null;

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private Map<String, Response> cache = new HashMap<>();
    private DatabaseService dbService;

    private AsyncWeatherService(Context context) {
        dbService = DatabaseService.getInstance(context);
    }

    public interface OnLoadDoneListener {
        void onLoadDone(Response response);
    }

    public static AsyncWeatherService getInstance(Context context) {
        if (instance == null)
            instance = new AsyncWeatherService(context);

        return instance;
    }

    public AsyncWeather getAsyncWeather(OnLoadDoneListener c) {
        return new AsyncWeather(c);
    }

    public class AsyncWeather {
        private WeakReference<OnLoadDoneListener> weakCallback;

        private AsyncWeather(OnLoadDoneListener c) {
            weakCallback = new WeakReference<>(c);
        }

        public void fetch(List<String> cities) {
            for (String city : cities)
                executor.execute(new Task(city, false));
        }

        public void force(List<String> cities) {
            for (String city : cities)
                executor.execute(new Task(city, true));
        }

        private class Task implements Runnable {
            private String city;
            private CityID id;
            private boolean isForced;

            private Task(String c, boolean i) {
                city = c;
                id = dbService.getID(city);
                isForced = i;
            }

            @Override
            public void run() {
                Response cached = cache.get(city);

                if (cached == null)
                    cached = dbService.getCached(city);

                Response update = isForced || cached == null ?
                        Response.update(city, id) : Response.update(cached, id);

                OnLoadDoneListener strong = weakCallback.get();
                if (strong != null)
                    mainThreadHandler.post(new MainThreadTask(strong, update));

                if (update != cached) {
                    cache.put(city, update);
                    dbService.putCache(update);
                }
            }
        }
    }

    private static class MainThreadTask implements Runnable {
        private OnLoadDoneListener callback;
        private Response response;

        MainThreadTask(OnLoadDoneListener cb, Response r) {
            callback = cb;
            response = r;
        }

        @Override
        public void run() {
            callback.onLoadDone(response);
        }
    }


}
