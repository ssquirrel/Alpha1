package com.example.lxl_z.alpha1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 9/2/2016.
 */
public class ChartView extends View {
    private List<DataPoint> data = Collections.emptyList();

    private int x;
    private int y;
    private int width;
    private int height;

    private int max;
    private int min;

    public ChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public void refreshView(List<DataPoint> weather) {
        data = weather;

        max = Integer.MIN_VALUE;
        min = Integer.MAX_VALUE;

        for (DataPoint dp : data) {
            if (dp.temp > max) {
                max = dp.temp;
            }

            if (dp.temp < min)
                min = dp.temp;

        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w,
                                 int h,
                                 int oldw,
                                 int oldh) {
        x = getPaddingLeft();
        y = getPaddingTop();

        width = w - getPaddingLeft() - getPaddingRight();
        height = h - getPaddingTop() - getPaddingBottom();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //
        if (data.size() == 0)
            return;
       /*
        {
            int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setStrokeJoin(Paint.Join.ROUND);

            canvas.drawRect(x, y, width, height, p);
        }
*/
        {
            int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setStrokeJoin(Paint.Join.ROUND);

            float startX = x;
            float MIN_H = (int) (height * 0.65);

            float step = width / (data.size() - 1);

            Path path = new Path();
            path.moveTo(startX, MIN_H);
            path.lineTo(startX, heightAt(0));

            for (int i = 1; i < data.size() - 1; i++) {
                startX += step;
                path.lineTo(startX, heightAt(i));
            }

            startX += step;

            path.lineTo(width, heightAt(data.size() - 1));
            path.lineTo(width, MIN_H);
            path.lineTo(x, MIN_H);

            canvas.drawPath(path, p);
        }

        {
            int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);

            Path path = new Path();
            path.moveTo(x, 0.65f * height);
            path.lineTo(x, height * 0.8f);
            path.lineTo(width, height * 0.8f);
            path.lineTo(width, 0.65f * height);

            canvas.drawPath(path, p);
        }

        {
            int color = 0x14ffffff;

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setStrokeJoin(Paint.Join.ROUND);

            float startX = x;
            float MIN_H = (int) (height * 0.65);

            float step = width / (data.size() - 1);

            Path path = new Path();
            path.moveTo(startX, MIN_H);
            path.lineTo(startX, heightAt(0));

            for (int i = 1; i < data.size() - 1; i++) {
                startX += step;
                path.lineTo(startX, heightAt(i));
            }

            path.lineTo(width, heightAt(data.size() - 1));
            path.lineTo(width, MIN_H);
            path.lineTo(x, MIN_H);

            canvas.drawPath(path, p);
        }

        {
            int color = 0x14ffffff;

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);

            Path path = new Path();
            path.moveTo(x, 0.65f * height);
            path.lineTo(x, height * 0.8f);
            path.lineTo(width, height * 0.8f);
            path.lineTo(width, 0.65f * height);

            canvas.drawPath(path, p);
        }

        {
            int color = ContextCompat.getColor(getContext(), R.color.colorAccent);

            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setColor(color);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(5);

            float startX = x;

            float step = width / (data.size() - 1);

            Path path = new Path();
            path.moveTo(startX, heightAt(0));

            for (int i = 1; i < data.size(); i++) {
                startX += step;
                path.lineTo(startX, heightAt(i));
            }

            canvas.drawPath(path, p);
        }

        {
            final int singleDp = getResources().getDimensionPixelSize(R.dimen.dp_px_scale);

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x89000000);
            p.setTextSize(singleDp * 14);

            float startX = x;
            float startY = height;

            float step = width / (data.size() - 1);

            for (int i = 0; i < data.size() - 1; i++) {
                Rect rect = new Rect();
                String txt = String.valueOf(data.get(i).hour);
                p.getTextBounds(txt, 0, txt.length(), rect);

                canvas.drawText(txt, startX, startY - rect.bottom - 5 * singleDp, p);

                startX += step;
            }
        }

    }

    static class DataPoint {
        int hour;
        int temp;

        DataPoint(int h, int t) {
            hour = h;
            temp = t;
        }
    }


    private int heightAt(int idx) {
        float MAX_H = (0.3f * height);
        float MIN_H = (0.65f * height);


        return (int) (MIN_H - (MIN_H - MAX_H) / (max - min) * (data.get(idx).temp - min));
    }
}
