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
 * <p>
 * AsyncWeatherService is a singleton class that fulfills the primary duty of retrieving weather
 * data in an asynchronous manner. AsyncWeatherService offloads the heavy lifting of the job
 * (which possibly involves database access, network I/O, parsing, etc.) to a background thread
 * so as not to block the main thread causing ANR. When the retrieval is completed, the result is
 * posted via callback. Since a callback is required for retrieval of the result, AsyncWeatherService
 * delegates the responsibility to AsyncWeather. AsyncWeather instances must be obtained by calling
 * getAsyncWeather which takes an implementation of OnLoadDoneListener.
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
        /*
        *  The Callback is usually implemented by activities or fragments which could be destroyed at
        *  any time. Using a WeakReference here would allow garbage collector recycle any destroyed
        *  activity or fragment normally.
        * */
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

        /*
        *  Task class encapsulate all network retrieval & caching logic that is meant to be executed
        *  on a background thread. Task implementation it a bit tricky because as an inner class, it
        *  has complete access to fields of the enclosing class which are created on the main thread
        *  and may not be safe to access from a separate thread.
        * */
        private class Task implements Runnable {
            private String city;
            private CityID id;
            private boolean isForced;

            /*
            * It should be noted that Task construction still takes place on the main thread. So, it
            * is perfectly safe to call getID here. (getID is backed by a hashMap which is not thread
            * safe.)
            * */
            private Task(String c, boolean i) {
                city = c;
                id = dbService.getID(city);
                isForced = i;
            }

            @Override
            public void run() {
                /*
                * Despite the fact that cache is a field of AsyncWeatherService, it is still safe to
                * use it here because the executor guarantees actions prior to the task submission
                * happens-before its execution. Executors.newSingleThreadExecutor() further ensures
                * that tasks would be executed in a sequential manner. Therefore cache would be in a
                * consistent and valid state for all Tasks. It also follows that cache can't be
                * observed in a valid state on the main thread afterwards.
                * */
                Response cached = cache.get(city);

                /*
                * getCached and corresponding putCache are thread safe.
                * */
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
