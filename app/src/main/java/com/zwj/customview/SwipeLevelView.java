package com.zwj.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SwipeLevelView extends View {

    private Paint colorRingPaint;
    private Paint grayRingPaint;
    private float radius;
    private RectF outsideRectF;
    private RectF insideRectF;
    private RectF ringRectF;
    private float outsideRectWidth;
    private float insideRectWidth;
    private int partNum = 7;
    private int level = 1;
    private final float swipeAngle = 180;
    private float partSwipeAngle;
    private float gapWidth = 2;
    private Paint textPaint;
    private String textStr = "Level:1";
    private Path grayPath;
    private Path colorPath;
    private Region totalRegion;
    private float[] startAngles;
    private float ringStroke;
    private ArrayList<Region> regions;

    public SwipeLevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        grayRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grayRingPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        grayRingPaint.setColor(Color.argb(255, 186, 186, 186));

        colorRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorRingPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        radius = measuredWidth / 2;
        if (measuredHeight * 2 < measuredWidth) {
            radius = measuredHeight;
        }

        ringStroke = radius / 3;
        outsideRectWidth = radius * 2;
        insideRectWidth = (radius - ringStroke) * 2;

        outsideRectF = new RectF(0, 0, outsideRectWidth, outsideRectWidth);
        insideRectF = new RectF(ringStroke, ringStroke, ringStroke + insideRectWidth, ringStroke + insideRectWidth);
        ringRectF = new RectF(ringStroke / 2, ringStroke / 2, outsideRectWidth - ringStroke / 2, outsideRectWidth - ringStroke / 2);

        totalRegion = new Region(0, 0, (int) outsideRectWidth, (int) outsideRectWidth);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        colorRingPaint.setStrokeWidth(ringStroke);
        //设置圆环渐变色渲染
        Shader shader = new SweepGradient(radius, radius, Color.RED, Color.YELLOW);
        colorRingPaint.setShader(shader);

        initPathData();
    }

    private void initPathData() {
        grayPath = new Path();
        colorPath = new Path();
        regions = new ArrayList<>();
        startAngles = new float[partNum];
        partSwipeAngle = (swipeAngle - gapWidth * (partNum - 1)) / partNum;
        for (int i = 0; i < partNum; i++) {
            float startAngle = (180 - swipeAngle) / 2 + 180 + i * (partSwipeAngle + gapWidth);
            startAngles[i] = startAngle;
            grayPath.addArc(outsideRectF, startAngles[i], partSwipeAngle);
            grayPath.lineTo(radius, radius);
            grayPath.close();
            colorPath.addArc(insideRectF, startAngles[i], partSwipeAngle);
            colorPath.lineTo(radius, radius);
            colorPath.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                grayPath.op(colorPath, Path.Op.DIFFERENCE);
            }
            Region region = new Region();
            region.setPath(grayPath, totalRegion);
            regions.add(region);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < partNum; i++) {
            canvas.drawPath(grayPath, grayRingPaint);
        }

        for (int i = 0; i < level; i++) {
            canvas.drawArc(ringRectF, startAngles[i], partSwipeAngle, false, colorRingPaint);
        }
        canvas.drawText(textStr, radius, radius - textPaint.getFontMetrics().bottom, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x;
        int y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                x = (int) event.getX();
                y = (int) event.getY();
                for (int i = 0; i < regions.size(); i++) {
                    boolean contains = regions.get(i).contains(x, y);
                    if (contains) {
                        level = i + 1;
                        textStr = "Level:" + (i + 1);
                        break;
                    }
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }
}
