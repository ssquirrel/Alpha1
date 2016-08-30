package com.example.lxl_z.alpha1;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.lxl_z.alpha1.Weather.*;

import java.lang.ref.WeakReference;
import java.util.Calendar;
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

    private Map<String, Response> cache = new HashMap<>();

    private DatabaseService dbService;

    AsyncWeatherTask newCurrentWeatherTask(OnLoadDoneCallback callback) {

        return new AsyncWeatherTask(callback) {
            @Override
            protected boolean isUpdatePossible(Response response) {


                return false;
            }

            @Override
            protected void doInBackground(Response response, DatabaseService.CityID id) {

            }

            private boolean isAqiUpdatePossible(List<AQI> aqi) {
                if (aqi == null)
                    return false;

                Calendar calendar = Calendar.getInstance();
                int hourNow = calendar.get(Calendar.HOUR_OF_DAY);
                int minuteNow = calendar.get(Calendar.MINUTE);

                calendar.setTimeInMillis(aqi.get(0).time);
                int hourLastUpdate = calendar.get(Calendar.HOUR_OF_DAY);

                return hourNow != hourLastUpdate && minuteNow < 30;
            }
        };
    }

    AsyncWeatherTask newDetailWeatherTask() {
        return null;
    }

    interface OnLoadDoneCallback {
        void onLoadDone(Response response);
    }

    abstract class AsyncWeatherTask {
        private WeakReference<OnLoadDoneCallback> weakCallback;

        AsyncWeatherTask(OnLoadDoneCallback c) {
            weakCallback = new WeakReference<>(c);
        }

        void execute(List<Request> request) {
            HttpTask[] tasks = new HttpTask[request.size()];

            for (int i = 0; i < request.size(); i++) {
                Response response = cache.get(request.get(i).city);

                if (response == null) {
                    response = new Response(request.get(i).city);
                    cache.put(request.get(i).city, response);
                }

                tasks[i] = new HttpTask(response, dbService.getID(request.get(i).city));
            }

            lock.register(tasks);

            for (HttpTask task : tasks) {
                executor.execute(task);
            }
        }

        protected abstract boolean isUpdatePossible(final Response response);

        protected abstract void doInBackground(final Response response,
                                               DatabaseService.CityID id);

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
            private Response response;
            private DatabaseService.CityID id;

            HttpTask(Response r, DatabaseService.CityID i) {
                response = r;
                id = i;
            }

            @Override
            public void run() {
                lock.lock(this);

                Response result;

                try {
                    lock.lock(this);

                    if (isUpdatePossible(response))
                        doInBackground(response, id);

                    result = new Response(response);

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
                return response.city;
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
