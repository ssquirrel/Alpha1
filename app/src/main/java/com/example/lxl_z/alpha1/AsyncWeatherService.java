package com.example.lxl_z.alpha1;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.lxl_z.alpha1.Weather.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by LXL_z on 8/27/2016.
 */
public class AsyncWeatherService {
    private static AsyncWeatherService instance = null;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, CPU_COUNT);
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final BlockingQueue<Runnable> Unbounded_WORK_QUEUE = new LinkedBlockingQueue<>();

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE,
            CORE_POOL_SIZE,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            Unbounded_WORK_QUEUE);

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private OrderedTagLock lock = new OrderedTagLock();

    private Map<String, WeatherUpdateHelper> cache = new HashMap<>();

    private DatabaseService dbService;

    AsyncWeatherTask newCurrentWeatherTask(OnLoadDoneCallback callback) {

        return new AsyncWeatherTask(callback) {
            @Override
            protected Response doInBackground(WeatherUpdateHelper helper) {
                Response result = new Response(helper.city);

                if (helper.id.stateAirID != null) {
                    if (helper.aqiTimer.check())
                        helper.aqiTimer.setAttemptResult(helper.updateAQI());

                    result.aqi = Collections.singletonList(helper.aqi.get(0));
                }

                if (helper.weatherTimer.check())
                    helper.weatherTimer.setAttemptResult(helper.updateCurrentWeather());

                result.weather = new Weather(helper.weather);

                return result;
            }
        };
    }

    AsyncWeatherTask newDetailWeatherTask(OnLoadDoneCallback callback) {
        return new AsyncWeatherTask(callback) {
            @Override
            protected Response doInBackground(WeatherUpdateHelper helper) {


                Response result = new Response(helper.city);
                result.aqi = new ArrayList<>(helper.aqi);
                result.weather = new Weather(helper.weather);
                result.forecast = new ArrayList<>(helper.forecast);
                return result;
            }
        };
    }

    interface OnLoadDoneCallback {
        void onLoadDone(Response response);
    }

    abstract class AsyncWeatherTask {
        private WeakReference<OnLoadDoneCallback> weakCallback;

        AsyncWeatherTask(OnLoadDoneCallback c) {
            weakCallback = new WeakReference<>(c);
        }

        void execute(List<String> cities) {
            HttpTask[] tasks = new HttpTask[cities.size()];

            for (int i = 0; i < cities.size(); i++) {
                WeatherUpdateHelper helper = cache.get(cities.get(i));

                if (helper == null) {
                    helper = new WeatherUpdateHelper();
                    helper.city = cities.get(i);
                    helper.id = dbService.getID(cities.get(i));

                    cache.put(cities.get(i), helper);
                }

                tasks[i] = new HttpTask(helper);
            }

            lock.register(tasks);

            for (HttpTask task : tasks) {
                executor.execute(task);
            }
        }

        protected abstract Response doInBackground(WeatherUpdateHelper scheduler);

        private class MainThreadTask implements Runnable {
            private OnLoadDoneCallback callback;
            private Response response;

            MainThreadTask(OnLoadDoneCallback cb, Response r) {
                callback = cb;
                response = r;
            }

            @Override
            public void run() {
                callback.onLoadDone(response);
            }
        }

        private class HttpTask implements Runnable, OrderedTagLock.TagRunnable {
            private WeatherUpdateHelper helper;

            HttpTask(WeatherUpdateHelper rs) {
                helper = rs;
            }

            @Override
            public void run() {
                lock.lock(this);

                final Response result;

                try {
                    lock.lock(this);

                    result = doInBackground(helper);

                } finally {
                    lock.unlock(this);
                }

                OnLoadDoneCallback strong = weakCallback.get();
                if (strong == null)
                    return;

                mainThreadHandler.post(new MainThreadTask(strong, result));
            }

            @Override
            public String getTag() {
                return helper.city;
            }
        }
    }

    static AsyncWeatherService getInstance(Context context) {
        if (instance == null)
            instance = new AsyncWeatherService(context);

        return instance;
    }

    private AsyncWeatherService(Context context) {
        dbService = DatabaseService.getInstance(context);
    }

    private static class OrderedTagLock {
        private Map<String, Queue<TagRunnable>> master = new HashMap<>();

        synchronized void register(TagRunnable[] runnable) {
            for (TagRunnable tr : runnable) {
                Queue<TagRunnable> queue = master.get(tr.getTag());

                if (queue == null) {
                    queue = new LinkedList<>();
                    master.put(tr.getTag(), queue);
                }

                queue.offer(tr);
            }
        }

        synchronized void lock(TagRunnable runnable) {
            Queue<TagRunnable> queue = master.get(runnable.getTag());

            while (queue.peek() != runnable) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        synchronized void unlock(TagRunnable runnable) {
            Queue<TagRunnable> queue = master.get(runnable.getTag());

            TagRunnable polled = queue.poll();

            if (polled != runnable)
                throw new RuntimeException("OrderedTagLock UB");

            notifyAll();
        }

        interface TagRunnable {
            String getTag();
        }

    }


}
