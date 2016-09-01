package com.example.lxl_z.alpha1.Weather;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

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

    private DatabaseService dbService;

    private Map<String, Integer> map = new HashMap<>();
    private List<Response> cache = new ArrayList<>();
    private List<WeatherUpdateHelper> helpers = new ArrayList<>();

    private AsyncWeatherService(Context context) {
        dbService = DatabaseService.getInstance(context);
    }

    public interface OnLoadDoneCallback {
        void onLoadDone(Response response);
    }

    public static AsyncWeatherService getInstance(Context context) {
        if (instance == null)
            instance = new AsyncWeatherService(context);

        return instance;
    }

    public AsyncWeatherTask newCurrentWeatherTask(OnLoadDoneCallback callback) {

        return new AsyncWeatherTask(callback) {
            @Override
            protected Response doInBackground(Response cached,
                                              CityID id,
                                              WeatherUpdateHelper helper) {
                if (id.stateAirID != null) {
                    List<AQI> update = helper.updateAQI(id.stateAirID,
                            cached.aqi != null ? cached.aqi.get(0).time : 0);

                    if (update != null)
                        cached.aqi = update;
                }

                {
                    Weather update = helper.updateCurrentWeather(id.owmID,
                            cached.weather != null ? cached.weather.time : 0);

                    if (update != null)
                        cached.weather = update;
                }

                Response response = new Response(cached.city);

                if (cached.aqi != null)
                    response.aqi = Collections.singletonList(cached.aqi.get(0));
                response.weather = new Weather(cached.weather);

                return response;
            }
        };
    }

    public AsyncWeatherTask newDetailWeatherTask(OnLoadDoneCallback callback) {
        return new AsyncWeatherTask(callback) {
            @Override
            protected Response doInBackground(Response response,
                                              CityID id,
                                              WeatherUpdateHelper helper) {

                /*
                Response result = new Response(helper.city);
                result.aqi = new ArrayList<>(helper.aqi);
                result.weather = new Weather(helper.weather);
                result.forecast = new ArrayList<>(helper.forecast);
                return result;
                */

                return null;
            }
        };
    }

    public abstract class AsyncWeatherTask {
        private WeakReference<OnLoadDoneCallback> weakCallback;

        AsyncWeatherTask(OnLoadDoneCallback c) {
            weakCallback = new WeakReference<>(c);
        }

        public void execute(List<String> cities) {
            HttpTask[] tasks = new HttpTask[cities.size()];

            for (int i = 0; i < cities.size(); i++) {
                Integer idx = map.get(cities.get(i));

                if (idx == null) {
                    Response response = new Response(cities.get(i));
                    WeatherUpdateHelper helper = new WeatherUpdateHelper();

                    cache.add(response);
                    helpers.add(helper);

                    idx = cache.size() - 1;

                    map.put(cities.get(i), idx);
                }

                tasks[i] = new HttpTask(cache.get(idx),
                        dbService.getID(cities.get(i)),
                        helpers.get(idx)
                );
            }

            lock.register(tasks);

            for (HttpTask task : tasks) {
                executor.execute(task);
            }
        }

        protected abstract Response doInBackground(Response cached,
                                                   CityID id,
                                                   WeatherUpdateHelper helper);

        private class HttpTask implements Runnable, OrderedTagLock.TagRunnable {
            private Response cached;
            private CityID id;
            private WeatherUpdateHelper helper;

            HttpTask(Response c, CityID i, WeatherUpdateHelper h) {
                cached = c;
                id = i;
                helper = h;
            }

            @Override
            public void run() {
                lock.lock(this);

                Response result;

                try {
                    lock.lock(this);

                    result = doInBackground(cached, id, helper);

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
                return cached.city;
            }
        }
    }

    private static class MainThreadTask implements Runnable {
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
