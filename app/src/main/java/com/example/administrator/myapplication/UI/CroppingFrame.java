package com.example.administrator.myapplication.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2018/1/23.
 */

public class CroppingFrame extends View {

    private static final int DEAFULT_SIZE = 500;
    private float oldX;
    private float oldY;
    private float newX;
    private float newY;

    public CroppingFrame(Context context) {
        super(context);
    }

    public CroppingFrame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CroppingFrame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                oldX = event.getX();
                oldY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                newX = event.getX();
                newY = event.getY();
                scrollBy((int)(oldX-newX),(int)(oldY-newY));
                oldX = newX;
                oldY = newY;
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0,0,getWidth(),getHeight(),paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getLength(widthMeasureSpec),getLength(heightMeasureSpec));
    }

    public int getLength(int measureSpec){
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode){
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return DEAFULT_SIZE;
            case MeasureSpec.UNSPECIFIED:
                return 0;
        }
        return 0;
    }



}
