package com.example.lxl_z.alpha1.Weather;

import java.util.concurrent.TimeUnit;

/**
 * Created by LXL_z on 9/13/2016.
 */

public class Clock {
    public long lastModified;

    private long interval;
    private long time;

    Clock(long i) {
        lastModified = 0;

        interval = i;
        time = System.currentTimeMillis();
    }

    boolean isValid() {
        long now = System.currentTimeMillis();

        if (lastModified / interval == now / interval)
            return true;

        long retryInterval = interval / 6;

        return time / retryInterval == now / retryInterval;
    }
}