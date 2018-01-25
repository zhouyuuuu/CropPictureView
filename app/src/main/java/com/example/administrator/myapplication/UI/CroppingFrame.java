package com.example.administrator.myapplication.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.administrator.myapplication.Util.PixelUtil;

import static android.content.ContentValues.TAG;


//Created by Administrator on 2018/1/23.

public class CroppingFrame extends View {

    private int minX;//方形框在x方向最小值
    private int maxX;//方形框在x方向最大值
    private int minY;//方形框在y方向最小值
    private int maxY;//方形框在y方向最大值
    private static final int DEAFULT_SIZE = 250;//方形框默认边长250dp
    private static final int DEAFULT_MIN_SIZE = 100;//方形框默认最小边长50dp
    private int minSize;
    private int size;
    private int left;//方形框左边位置
    private int right;//方形框右边位置
    private int top;//方形框顶边位置
    private int bottom;//方形框底边位置
    private float preX;//上一个触摸点
    private float preY;//上一个触摸点
    private int location = 0;//记录触摸点初始位置
    private static final int LOCATION_TOP = 100;//标志：触摸点位置在方形框顶边
    private static final int LOCATION_RIGHTTOP = 101;//标志：触摸点位置在方形框右上角
    private static final int LOCATION_RIGHT = 102;//标志：触摸点位置在方形框右边
    private static final int LOCATION_RIGHTBOTTOM = 103;//标志：触摸点位置在方形框右下角
    private static final int LOCATION_BOTTOM = 104;//标志：触摸点位置在方形框底边
    private static final int LOCATION_LEFTBOTTOM = 105;//标志：触摸点位置在方形框左下角
    private static final int LOCATION_LEFT = 106;//标志：触摸点位置在方形框左边
    private static final int LOCATION_LEFTTOP = 107;//标志：触摸点位置在方形框左上角
    private static final int LOCATION_INSIDE = 108;//标志：触摸点位置在方形框内部
    private static final int MODE_SINGLEPOINT = 200;//标志：触摸模式为单指
    private static final int MODE_DOUBLEPOINT = 201;//标志：触摸模式为双指
    private static final int MODE_NONE = 202;//标志：触摸模式为无
    private int mode = MODE_NONE;//默认为无
    private float preLength;//记录缩放时上一个两点间距
    private Paint paintText = new Paint();//文本画笔，全局生成，避免重复实例化造成onDraw时间过长
    private boolean isMeasured = false;//标志位：只在第一次测量时设置方形框位置和边长
    private Path path;//同文本画笔，用于把半透明区域限制在方形框外面

    public CroppingFrame(Context context) {
        this(context, null);
    }

    public CroppingFrame(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CroppingFrame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //绘制透明预览框
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(50);
        paintText.setTextAlign(Paint.Align.CENTER);
        //创建圆形预览框
        path = new Path();
        size = PixelUtil.Dp2Px(context, DEAFULT_SIZE);
        minSize = PixelUtil.Dp2Px(context, DEAFULT_MIN_SIZE);
    }

    //谷歌要求重写，具体原因还未探究
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if (isInsideFrame((int) event.getX(), (int) event.getY())) {
                    mode = MODE_SINGLEPOINT;
                    preX = event.getX();
                    preY = event.getY();
                    //由于getRawX()返回的是原点在屏幕左上角的触摸点坐标，而top和bottom的坐标原点在标题栏之下，因此将preY减去标题栏+状态栏高度
                    location = findLocation((int) preX, (int) preY);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (isInsideFrame((int) event.getX(), (int) event.getY()) && isInsideFrame((int) event.getX(1), (int) event.getY(1))) {
                    mode = MODE_DOUBLEPOINT;
                    float disX = Math.abs(event.getX(0) - event.getX(1));
                    float disY = Math.abs(event.getY(0) - event.getY(1));
                    preLength = (float) Math.sqrt(disX * disX + disY * disY);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_DOUBLEPOINT) {//双点触控
                    //计算两点间距
                    float disX = Math.abs(event.getX(0) - event.getX(1));
                    float disY = Math.abs(event.getY(0) - event.getY(1));
                    float curLength = (float) Math.sqrt(disX * disX + disY * disY);
                    //当前间距和之前间距相减除以2，因为left+scale和right+scale，宽度扩大2*scale=间距之差
                    float scale = (curLength - preLength) / 2;
                    int newLeft = (int) (left - scale + 0.5f);//注意四舍五入
                    int newTop = (int) (top - scale + 0.5f);
                    int newRight = (int) (right + scale + 0.5f);
                    int newBottom = (int) (bottom + scale + 0.5f);
                    Log.e(TAG, "onTouchEvent: " + (newLeft - left) + "  " + (newRight - right) + "  " + (newTop - top) + "  " + (newBottom - bottom));
                    if ((scale > 0 && newLeft > minX && newRight < maxX && newTop > minY && newBottom < maxY) || (scale <= 0 && newLeft < newRight - minSize && newTop < newBottom - minSize)) {
                        left = newLeft;
                        top = newTop;
                        right = newRight;
                        bottom = newBottom;
                        preLength = curLength;//当前间距已是之前间距
                    }
                } else if (mode == MODE_SINGLEPOINT) {//单点触控
                    float newX = event.getX();
                    float newY = event.getY();
                    int offsetX = (int) (newX - preX);
                    int offsetY = (int) (newY - preY);
                    switch (location) {//根据落点在方框位置有不同操作，如在顶边，则top加上触点移动偏移量

                        case LOCATION_TOP:
                            int newTop = top + offsetY;
                            if (newTop > minY && newTop < bottom - minSize) {
                                top = newTop;
                                preY = newY;
                            } else if (newTop <= minY) {
                                top = minY;
                                preY = minY;
                            } else {
                                top = bottom - minSize;
                                preY = bottom - minSize;
                            }
                            preX = newX;
                            break;

                        case LOCATION_RIGHTTOP:
                            int newRight = right + offsetX;
                            if (newRight > left + minSize && newRight < maxX) {
                                right = newRight;
                                preX = newX;
                            } else if (newRight <= left + minSize) {
                                right = left + minSize;
                                preX = left + minSize;
                            } else {
                                right = maxX;
                                preX = maxX;
                            }
                            newTop = top + offsetY;
                            if (newTop > minY && newTop < bottom - minSize) {
                                top = newTop;
                                preY = newY;
                            } else if (newTop <= minY) {
                                top = minY;
                                preY = minY;
                            } else {
                                top = bottom - minSize;
                                preY = bottom - minSize;
                            }
                            break;

                        case LOCATION_RIGHT:
                            newRight = right + offsetX;
                            if (newRight > left + minSize && newRight < maxX) {
                                right = newRight;
                                preX = newX;
                            } else if (newRight <= left + minSize) {
                                right = left + minSize;
                                preX = left + minSize;
                            } else {
                                right = maxX;
                                preX = maxX;
                            }
                            preY = newY;
                            break;

                        case LOCATION_RIGHTBOTTOM:
                            newRight = right + offsetX;
                            if (newRight > left + minSize && newRight < maxX) {
                                right = newRight;
                                preX = newX;
                            } else if (newRight <= left + minSize) {
                                right = left + minSize;
                                preX = left + minSize;
                            } else {
                                right = maxX;
                                preX = maxX;
                            }
                            int newBottom = bottom + offsetY;
                            if (newBottom > top + minSize && newBottom < maxY) {
                                bottom = newBottom;
                                preY = newY;
                            } else if (newBottom <= top + minSize) {
                                bottom = top + minSize;
                                preY = top + minSize;
                            } else {
                                bottom = maxY;
                                preY = maxY;
                            }
                            break;

                        case LOCATION_BOTTOM:
                            newBottom = bottom + offsetY;
                            if (newBottom > top + minSize && newBottom < maxY) {
                                bottom = newBottom;
                                preY = newY;
                            } else if (newBottom <= top + minSize) {
                                bottom = top + minSize;
                                preY = top + minSize;
                            } else {
                                bottom = maxY;
                                preY = maxY;
                            }
                            preX = newX;
                            break;

                        case LOCATION_LEFTBOTTOM:
                            int newLeft = left + offsetX;
                            if (newLeft > minX && newLeft < right - minSize) {
                                left = newLeft;
                                preX = newX;
                            } else if (newLeft <= minX) {
                                left = minX;
                                preX = minX;
                            } else {
                                left = right - minSize;
                                preX = right - minSize;
                            }
                            newBottom = bottom + offsetY;
                            if (newBottom > top + minSize && newBottom < maxY) {
                                bottom = newBottom;
                                preY = newY;
                            } else if (newBottom <= top + minSize) {
                                bottom = top + minSize;
                                preY = top + minSize;
                            } else {
                                bottom = maxY;
                                preY = maxY;
                            }
                            break;

                        case LOCATION_LEFT:
                            newLeft = left + offsetX;
                            if (newLeft > minX && newLeft < right - minSize) {
                                left = newLeft;
                                preX = newX;
                            } else if (newLeft <= minX) {
                                left = minX;
                                preX = minX;
                            } else {
                                left = right - minSize;
                                preX = right - minSize;
                            }
                            preY = newY;
                            break;

                        case LOCATION_LEFTTOP:
                            newLeft = left + offsetX;
                            if (newLeft > minX && newLeft < right - minSize) {
                                left = newLeft;
                                preX = newX;
                            } else if (newLeft <= minX) {
                                left = minX;
                                preX = minX;
                            } else {
                                left = right - minSize;
                                preX = right - minSize;
                            }
                            newTop = top + offsetY;
                            if (newTop > minY && newTop < bottom - minSize) {
                                top = newTop;
                                preY = newY;
                            } else if (newTop <= bottom - minSize) {
                                top = minY;
                                preY = minY;
                            } else {
                                top = bottom - minSize;
                                preY = bottom - minSize;
                            }
                            break;

                        case LOCATION_INSIDE:
                            newTop = top + offsetY;
                            newLeft = left + offsetX;
                            newBottom = bottom + offsetY;
                            newRight = right + offsetX;
                            if (newLeft > minX && newRight < maxX) {
                                left = newLeft;
                                right = newRight;
                                preX = newX;
                            } else if (newLeft <= minX) {
                                right = right + minX - left;
                                left = minX;
                                if (newX <= minX) newX = minX;
                                preX = newX;
                            } else if (newRight >= maxX) {
                                left = left + maxX - right;
                                right = maxX;
                                if (newX >= maxX) newX = maxX;
                                preX = newX;
                            }
                            if (newTop > minY && newBottom < maxY) {
                                top = newTop;
                                bottom = newBottom;
                                preY = newY;
                            } else if (newTop <= minY) {
                                bottom = bottom + minY - top;
                                top = minY;
                                if (newY <= minY) newY = minY;
                                preY = newY;
                            } else if (newBottom >= maxY) {
                                top = top + maxY - bottom;
                                bottom = maxY;
                                if (newY >= maxY) newY = maxY;
                                preY = newY;
                            }
                            break;
                    }
                }
                invalidate();//调draw方法刷新
                break;

            //抬起时触发点击，暂无需实现
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mode = MODE_NONE;
                performClick();
                break;
        }
        return true;
    }

    private boolean isInsideFrame(int x, int y) {
        return x > left && x < right && y > top && y < bottom;
    }

    //找到点（x,y）在方形框中的位置
    private int findLocation(int x, int y) {
        boolean isLeft = false;
        boolean isTop = false;
        boolean isRight = false;
        boolean isBottom = false;
        if (x < left + 50 && x > left - 50)//给定-50到50的误差范围，以免过于难触发
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
        if (!isMeasured) {//第一次测量完毕时，getWidth才有值，因此在这里初始化方框的四条边
            left = (getWidth() - size) / 2;
            right = (getWidth() + size) / 2;
            top = (getHeight() - size) / 2;
            bottom = (getHeight() + size) / 2;
            minX = 0;
            maxX = getWidth();
            minY = 0;
            maxY = getHeight();
            isMeasured = true;
        }
        path.reset();
        path.addRect(left, top, right, bottom, Path.Direction.CW);
        //保存当前canvas 状态
        canvas.save();
        //将当前画布可以绘画区域限制死为预览框外的区域
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        //绘画半透明遮罩
        canvas.drawColor(Color.parseColor("#90000000"));
        //还原画布状态
        canvas.restore();
        //显示像素
        canvas.drawText((right - left) + "x" + (bottom - top), (left + right) / 2, (top + bottom) / 2, paintText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getLength(widthMeasureSpec), getLength(heightMeasureSpec));
    }

    //wrapcontent和matchparent都填充满父布局，后面有需求可以再改
    public int getLength(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.UNSPECIFIED:
                return 0;
        }
        return 0;
    }


}
