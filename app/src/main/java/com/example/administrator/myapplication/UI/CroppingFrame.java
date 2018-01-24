package com.example.administrator.myapplication.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2018/1/23.
 */

public class CroppingFrame extends View {

    private static final int DEAFULT_SIZE = 500;
    private float oldX;
    private float oldY;
    private int location = 0;
    private static final int LOCATION_TOP = 100;
    private static final int LOCATION_RIGHTTOP = 101;
    private static final int LOCATION_RIGHT = 102;
    private static final int LOCATION_RIGHTBOTTOM = 103;
    private static final int LOCATION_BOTTOM = 104;
    private static final int LOCATION_LEFTBOTTOM = 105;
    private static final int LOCATION_LEFT = 106;
    private static final int LOCATION_LEFTTOP = 107;
    private static final int LOCATION_INSIDE = 108;
    private Context mContext;
    private static final int MODE_SINGLEPOINT = 200;
    private static final int MODE_DOUBLEPOINT = 201;
    private static final int MODE_READY = 202;
    private int mode = MODE_READY;
    private float oldLength;
    private Paint paint = new Paint();

    public CroppingFrame(Context context) {
        this(context, null);
    }

    public CroppingFrame(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CroppingFrame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public boolean performClick() {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick();
        // Handle the action for the custom click here
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mode = MODE_SINGLEPOINT;
                oldX = event.getRawX();
                oldY = event.getRawY();
                location = findLocation((int) oldX, (int) oldY - getStatusBarHeight(mContext));
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_DOUBLEPOINT;
                float oldX1 = event.getX();
                float oldY1 = event.getY();
                float oldX2 = event.getX(1);
                float oldY2 = event.getY(1);
                float disX=Math.abs(event.getX(0)-event.getX(1));
                float disY=Math.abs(event.getY(0)-event.getY(1));
                oldLength = (float) Math.sqrt(disX*disX+disY*disY);
//                oldLength = (float) Math.sqrt((oldX - oldX2) * (oldX - oldX2) + (oldY - oldY2) * (oldY - oldY2));
                Log.e(TAG, "onTouchEvent:old   "+oldX1+"    "+oldY1+"    "+ oldX2 +"    "+ oldY2);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_DOUBLEPOINT) {
                    float newX1 = event.getX();
                    float newY1 = event.getY();
                    float newX2 = event.getX(1);
                    float newY2 = event.getY(1);
                    disX=Math.abs(event.getX(0)-event.getX(1));
                    disY=Math.abs(event.getY(0)-event.getY(1));
                    float newLength = (float) Math.sqrt(disX*disX+disY*disY);
                    Log.e(TAG, "onTouchEvent:new   " + newX1 + "    " + newY1 + "    " + newX2 + "    " + newY2);
//                    float newLength = (float) Math.sqrt((newX1 - newX2) * (newX1 - newX2) + (newY1 - newY2) * (newY1- newY2));
//                    Log.e(TAG, "onTouchEvent: " + newLength + "       " + oldLength);
                    float scale = (newLength-oldLength)/2;
                    layout((int)(getLeft()-scale),(int)(getTop()-scale),(int)(getRight()+scale),(int)(getBottom()+scale));
                    oldLength = newLength;
                }else if (mode == MODE_SINGLEPOINT){
                    float newX = event.getRawX();
                    float newY = event.getRawY();
                    Log.e("x and y", "onTouchEvent: " + newX + "   " + newY);
                    int offsetX = (int) (newX - oldX);
                    int offsetY = (int) (newY - oldY);
                    switch (location) {
                        case LOCATION_TOP:
                            layout(getLeft(), getTop() + offsetY, getRight(), getBottom());
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_RIGHTTOP:
                            layout(getLeft(), getTop() + offsetY, getRight() + offsetX, getBottom());
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_RIGHT:
                            layout(getLeft(), getTop(), getRight() + offsetX, getBottom());
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_RIGHTBOTTOM:
                            layout(getLeft(), getTop(), getRight() + offsetX, getBottom() + offsetY);
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_BOTTOM:
                            layout(getLeft(), getTop(), getRight(), getBottom() + offsetY);
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_LEFTBOTTOM:
                            layout(getLeft() + offsetX, getTop(), getRight(), getBottom() + offsetY);
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_LEFT:
                            layout(getLeft() + offsetX, getTop(), getRight(), getBottom());
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_LEFTTOP:
                            layout(getLeft() + offsetX, getTop() + offsetY, getRight(), getBottom());
                            oldX = newX;
                            oldY = newY;
                            break;
                        case LOCATION_INSIDE:
                            layout(getLeft() + offsetX, getTop() + offsetY, getRight() + offsetX, getBottom() + offsetY);
                            oldX = newX;
                            oldY = newY;
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mode = MODE_READY;
                performClick();
                break;
            }
        return true;
    }

    private int getStatusBarHeight(Context context) {
        Rect outRect1=new Rect();
        ((Activity)context).getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        return outRect1.top+((Activity)context).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }


    private int findLocation(int x, int y) {
        int left = getLeft();
        int right = getRight();
        int top = getTop();
        int bottom = getBottom();
        Log.e(TAG, "findLocation: " + " left " + left + " right " + right + " top " + top + " bottom " + bottom + " x " + x + " y " + y);
        boolean isLeft = false;
        boolean isTop = false;
        boolean isRight = false;
        boolean isBottom = false;
        if (x < left + 50 && x > left - 50)
            isLeft = true;
        else if (x > right - 50 && x < right + 50)
            isRight = true;
        if (y < top + 50 && y > top - 50)
            isTop = true;
        else if (y > bottom - 50 && y < bottom + 50)
            isBottom = true;
        if (isLeft) {
            if (isTop) return LOCATION_LEFTTOP;
            else if (isBottom) return LOCATION_LEFTBOTTOM;
            else return LOCATION_LEFT;
        } else if (isRight) {
            if (isTop) return LOCATION_RIGHTTOP;
            else if (isBottom) return LOCATION_RIGHTBOTTOM;
            else return LOCATION_RIGHT;
        } else if (isTop) return LOCATION_TOP;
        else if (isBottom) return LOCATION_BOTTOM;
        else return LOCATION_INSIDE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(25);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getLength(widthMeasureSpec), getLength(heightMeasureSpec));
    }

    public int getLength(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
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
