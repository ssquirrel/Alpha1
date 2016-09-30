package com.example.lxl_z.alpha1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.lxl_z.alpha1.Weather.HeWeather;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 9/2/2016.
 */
public class ChartView extends View {
    private List<DataPoint> data = Collections.emptyList();
    private int data_max;
    private int data_min;

    private Rect dimension = new Rect();

    private float curveUpperBound = 0;
    private float curveLowerBound = 0;
    private float graphLowerBound = 0;

    public ChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public void refreshView(List<DataPoint> weather) {
        data = weather;

        data_max = Integer.MIN_VALUE;
        data_min = Integer.MAX_VALUE;

        for (DataPoint dp : data) {
            if (data_max < dp.temp) {
                data_max = dp.temp;
            }

            if (data_min > dp.temp)
                data_min = dp.temp;
        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w,
                                 int h,
                                 int oldw,
                                 int oldh) {

        dimension.left = getPaddingLeft();
        dimension.top = getPaddingTop();
        dimension.right = w - getPaddingRight();
        dimension.bottom = h - getPaddingBottom();

        curveUpperBound = 0.2f * dimension.height();
        curveLowerBound = 0.5f * dimension.height();
        graphLowerBound = 0.75f * dimension.height();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //
        if (data.size() == 0)
            return;

        float step = dimension.width() / (data.size() - 2f);

        {
            int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setStrokeJoin(Paint.Join.ROUND);

            float startX = dimension.left;

            Path curve = new Path();
            curve.moveTo(startX, (heightAt(0) + heightAt(1)) / 2);
            curve.lineTo(startX += step / 2, heightAt(1));

            for (int i = 2; i < data.size() - 1; i++) {
                startX += step;
                curve.lineTo(startX, heightAt(i));
            }

            float midY = (heightAt(data.size() - 2) + heightAt(data.size() - 1)) / 2;

            curve.lineTo(dimension.right, midY);
            curve.lineTo(dimension.right, graphLowerBound);
            curve.lineTo(dimension.left, graphLowerBound);
            curve.close();

            canvas.drawPath(curve, p);
        }


        {
            final int singleDp = getResources().getDimensionPixelSize(R.dimen.dp_px_scale);

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(0x89000000);
            p.setTextSize(singleDp * 12);

            Rect rect = new Rect();
            p.getTextBounds("PM", 0, "PM".length(), rect);


            float startX = dimension.left;
            float startY = graphLowerBound;

            canvas.drawText(data.get(1).time,
                    (int) (startX += step / 2), (int) (startY + rect.height() + 5 * singleDp), p);

            canvas.drawText(data.get(1).temp + "°",
                    (int) (startX), (int) (heightAt(1) - 5 * singleDp), p);

            for (int i = 2; i < data.size() - 1; i++) {
                startX += step;

                String time = data.get(i).time;

                canvas.drawText(time, (int) startX, (int) (startY + rect.height() + 5 * singleDp), p);

                String temp = data.get(i).temp + "°";

                canvas.drawText(temp, (int) startX, (int) (heightAt(i) - 5 * singleDp), p);
            }
        }

    }

    static class DataPoint {
        String time;
        int temp;

        DataPoint(String ti, int t) {
            time = ti;
            temp = t;
        }
    }

    // ub = (max - min)a + lb
    //(ub-1b)/(max - min)(x-min) + lb
    float heightAt(int idx) {
        return (curveUpperBound - curveLowerBound) / (data_max - data_min) * (data.get(idx).temp - data_min) + curveLowerBound;
    }

}
