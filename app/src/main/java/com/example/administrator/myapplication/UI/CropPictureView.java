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

public class CropPictureView extends View {

    private int mMinX;//方形框在x方向最小值
    private int mMaxX;//方形框在x方向最大值
    private int mMinY;//方形框在y方向最小值
    private int mMaxY;//方形框在y方向最大值
    private static final int DEAFULT_SIZE = 250;//方形框默认边长250dp
    private static final int DEAFULT_MIN_SIZE = 100;//方形框默认最小边长50dp
    private int mMinSize;//方框最小边长
    private int mSize;//方框边长
    private int mLeft;//方形框左边位置
    private int mRight;//方形框右边位置
    private int mTop;//方形框顶边位置
    private int mBottom;//方形框底边位置
    private float mPreX;//上一个触摸点
    private float mPreY;//上一个触摸点
    private int mLocation = 0;//记录触摸点初始位置
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
    private int mMode = MODE_NONE;//默认为无
    private float mPreLength;//记录缩放时上一个两点间距
    private Paint mPaintText = new Paint();//文本画笔，全局生成，避免重复实例化造成onDraw时间过长
    private boolean mIsMeasured = false;//标志位：只在第一次测量时设置方形框位置和边长
    private Path mPath;//同文本画笔，用于把半透明区域限制在方形框外面

    public CropPictureView(Context context) {
        this(context, null);
    }

    public CropPictureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 初始化一些成员变量
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CropPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //绘制透明预览框
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextSize(50);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        //创建圆形预览框
        mPath = new Path();
        mSize = PixelUtil.Dp2Px(context, DEAFULT_SIZE);
        mMinSize = PixelUtil.Dp2Px(context, DEAFULT_MIN_SIZE);
    }

    /**
     * 谷歌要求重写，具体原因还未探究
     */
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    /**
     * 触摸事件分两种，一种单指触摸一种双指触摸，触发前提是落点在方框内或边缘
     * 单指时落点在方框内则进行方框平移，落在边缘或角落时进行放大缩小操作，如落在左边时调整左边位置，落在左上角调整左边和顶边位置
     * 双指时两个落点都在方框内时，通过两指间距来调整方框大小，方框以框中心点为锚点进行缩放
     * 当方框边缘将超过限制区域时将停止放大，低于最小边长时停止缩小
     * @param event
     * @return true 处理点击事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if (isInsideFrame((int) event.getX(), (int) event.getY())) {
                    mMode = MODE_SINGLEPOINT;
                    mPreX = event.getX();
                    mPreY = event.getY();
                    //由于getRawX()返回的是原点在屏幕左上角的触摸点坐标，而top和bottom的坐标原点在标题栏之下，因此将preY减去标题栏+状态栏高度
                    mLocation = findLocation((int) mPreX, (int) mPreY);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (isInsideFrame((int) event.getX(), (int) event.getY()) && isInsideFrame((int) event.getX(1), (int) event.getY(1))) {
                    mMode = MODE_DOUBLEPOINT;
                    float disX = Math.abs(event.getX(0) - event.getX(1));
                    float disY = Math.abs(event.getY(0) - event.getY(1));
                    mPreLength = (float) Math.sqrt(disX * disX + disY * disY);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mMode == MODE_DOUBLEPOINT) {//双点触控
                    //计算两点间距
                    float disX = Math.abs(event.getX(0) - event.getX(1));
                    float disY = Math.abs(event.getY(0) - event.getY(1));
                    float curLength = (float) Math.sqrt(disX * disX + disY * disY);
                    //当前间距和之前间距相减除以2，因为left+scale和right+scale，宽度扩大2*scale=间距之差
                    float scale = (curLength - mPreLength) / 2;
                    int newLeft = (int) (mLeft - scale + 0.5f);//注意四舍五入
                    int newTop = (int) (mTop - scale + 0.5f);
                    int newRight = (int) (mRight + scale + 0.5f);
                    int newBottom = (int) (mBottom + scale + 0.5f);
                    Log.e(TAG, "onTouchEvent: " + (newLeft - mLeft) + "  " + (newRight - mRight) + "  " + (newTop - mTop) + "  " + (newBottom - mBottom));
                    if ((scale > 0 && newLeft > mMinX && newRight < mMaxX && newTop > mMinY && newBottom < mMaxY) || (scale <= 0 && newLeft < newRight - mMinSize && newTop < newBottom - mMinSize)) {
                        mLeft = newLeft;
                        mTop = newTop;
                        mRight = newRight;
                        mBottom = newBottom;
                        mPreLength = curLength;//当前间距已是之前间距
                    }
                } else if (mMode == MODE_SINGLEPOINT) {//单点触控
                    float newX = event.getX();
                    float newY = event.getY();
                    int offsetX = (int) (newX - mPreX);
                    int offsetY = (int) (newY - mPreY);
                    switch (mLocation) {//根据落点在方框位置有不同操作，如在顶边，则top加上触点移动偏移量

                        case LOCATION_TOP:
                            int newTop = mTop + offsetY;
                            if (newTop > mMinY && newTop < mBottom - mMinSize) {
                                mTop = newTop;
                                mPreY = newY;
                            } else if (newTop <= mMinY) {
                                mTop = mMinY;
                                mPreY = mMinY;
                            } else {
                                mTop = mBottom - mMinSize;
                                mPreY = mBottom - mMinSize;
                            }
                            mPreX = newX;
                            break;

                        case LOCATION_RIGHTTOP:
                            int newRight = mRight + offsetX;
                            if (newRight > mLeft + mMinSize && newRight < mMaxX) {
                                mRight = newRight;
                                mPreX = newX;
                            } else if (newRight <= mLeft + mMinSize) {
                                mRight = mLeft + mMinSize;
                                mPreX = mLeft + mMinSize;
                            } else {
                                mRight = mMaxX;
                                mPreX = mMaxX;
                            }
                            newTop = mTop + offsetY;
                            if (newTop > mMinY && newTop < mBottom - mMinSize) {
                                mTop = newTop;
                                mPreY = newY;
                            } else if (newTop <= mMinY) {
                                mTop = mMinY;
                                mPreY = mMinY;
                            } else {
                                mTop = mBottom - mMinSize;
                                mPreY = mBottom - mMinSize;
                            }
                            break;

                        case LOCATION_RIGHT:
                            newRight = mRight + offsetX;
                            if (newRight > mLeft + mMinSize && newRight < mMaxX) {
                                mRight = newRight;
                                mPreX = newX;
                            } else if (newRight <= mLeft + mMinSize) {
                                mRight = mLeft + mMinSize;
                                mPreX = mLeft + mMinSize;
                            } else {
                                mRight = mMaxX;
                                mPreX = mMaxX;
                            }
                            mPreY = newY;
                            break;

                        case LOCATION_RIGHTBOTTOM:
                            newRight = mRight + offsetX;
                            if (newRight > mLeft + mMinSize && newRight < mMaxX) {
                                mRight = newRight;
                                mPreX = newX;
                            } else if (newRight <= mLeft + mMinSize) {
                                mRight = mLeft + mMinSize;
                                mPreX = mLeft + mMinSize;
                            } else {
                                mRight = mMaxX;
                                mPreX = mMaxX;
                            }
                            int newBottom = mBottom + offsetY;
                            if (newBottom > mTop + mMinSize && newBottom < mMaxY) {
                                mBottom = newBottom;
                                mPreY = newY;
                            } else if (newBottom <= mTop + mMinSize) {
                                mBottom = mTop + mMinSize;
                                mPreY = mTop + mMinSize;
                            } else {
                                mBottom = mMaxY;
                                mPreY = mMaxY;
                            }
                            break;

                        case LOCATION_BOTTOM:
                            newBottom = mBottom + offsetY;
                            if (newBottom > mTop + mMinSize && newBottom < mMaxY) {
                                mBottom = newBottom;
                                mPreY = newY;
                            } else if (newBottom <= mTop + mMinSize) {
                                mBottom = mTop + mMinSize;
                                mPreY = mTop + mMinSize;
                            } else {
                                mBottom = mMaxY;
                                mPreY = mMaxY;
                            }
                            mPreX = newX;
                            break;

                        case LOCATION_LEFTBOTTOM:
                            int newLeft = mLeft + offsetX;
                            if (newLeft > mMinX && newLeft < mRight - mMinSize) {
                                mLeft = newLeft;
                                mPreX = newX;
                            } else if (newLeft <= mMinX) {
                                mLeft = mMinX;
                                mPreX = mMinX;
                            } else {
                                mLeft = mRight - mMinSize;
                                mPreX = mRight - mMinSize;
                            }
                            newBottom = mBottom + offsetY;
                            if (newBottom > mTop + mMinSize && newBottom < mMaxY) {
                                mBottom = newBottom;
                                mPreY = newY;
                            } else if (newBottom <= mTop + mMinSize) {
                                mBottom = mTop + mMinSize;
                                mPreY = mTop + mMinSize;
                            } else {
                                mBottom = mMaxY;
                                mPreY = mMaxY;
                            }
                            break;

                        case LOCATION_LEFT:
                            newLeft = mLeft + offsetX;
                            if (newLeft > mMinX && newLeft < mRight - mMinSize) {
                                mLeft = newLeft;
                                mPreX = newX;
                            } else if (newLeft <= mMinX) {
                                mLeft = mMinX;
                                mPreX = mMinX;
                            } else {
                                mLeft = mRight - mMinSize;
                                mPreX = mRight - mMinSize;
                            }
                            mPreY = newY;
                            break;

                        case LOCATION_LEFTTOP:
                            newLeft = mLeft + offsetX;
                            if (newLeft > mMinX && newLeft < mRight - mMinSize) {
                                mLeft = newLeft;
                                mPreX = newX;
                            } else if (newLeft <= mMinX) {
                                mLeft = mMinX;
                                mPreX = mMinX;
                            } else {
                                mLeft = mRight - mMinSize;
                                mPreX = mRight - mMinSize;
                            }
                            newTop = mTop + offsetY;
                            if (newTop > mMinY && newTop < mBottom - mMinSize) {
                                mTop = newTop;
                                mPreY = newY;
                            } else if (newTop <= mBottom - mMinSize) {
                                mTop = mMinY;
                                mPreY = mMinY;
                            } else {
                                mTop = mBottom - mMinSize;
                                mPreY = mBottom - mMinSize;
                            }
                            break;

                        case LOCATION_INSIDE:
                            newTop = mTop + offsetY;
                            newLeft = mLeft + offsetX;
                            newBottom = mBottom + offsetY;
                            newRight = mRight + offsetX;
                            if (newLeft > mMinX && newRight < mMaxX) {
                                mLeft = newLeft;
                                mRight = newRight;
                                mPreX = newX;
                            } else if (newLeft <= mMinX) {
                                mRight = mRight + mMinX - mLeft;
                                mLeft = mMinX;
                                if (newX <= mMinX) newX = mMinX;
                                mPreX = newX;
                            } else if (newRight >= mMaxX) {
                                mLeft = mLeft + mMaxX - mRight;
                                mRight = mMaxX;
                                if (newX >= mMaxX) newX = mMaxX;
                                mPreX = newX;
                            }
                            if (newTop > mMinY && newBottom < mMaxY) {
                                mTop = newTop;
                                mBottom = newBottom;
                                mPreY = newY;
                            } else if (newTop <= mMinY) {
                                mBottom = mBottom + mMinY - mTop;
                                mTop = mMinY;
                                if (newY <= mMinY) newY = mMinY;
                                mPreY = newY;
                            } else if (newBottom >= mMaxY) {
                                mTop = mTop + mMaxY - mBottom;
                                mBottom = mMaxY;
                                if (newY >= mMaxY) newY = mMaxY;
                                mPreY = newY;
                            }
                            break;
                    }
                }
                invalidate();//调draw方法刷新
                break;

            //抬起时触发点击，暂无需实现
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mMode = MODE_NONE;
                performClick();
                break;
        }
        return true;
    }

    /**
     * 判断落点（x,y）是否在方框内
     * @param x
     * @param y
     * @return
     */
    private boolean isInsideFrame(int x, int y) {
        return x > mLeft && x < mRight && y > mTop && y < mBottom;
    }

    /**
     * 找到点（x,y）在方形框中的位置，例如：左上角
     */
    private int findLocation(int x, int y) {
        boolean isLeft = false;
        boolean isTop = false;
        boolean isRight = false;
        boolean isBottom = false;
        if (x < mLeft + 50 && x > mLeft - 50)//给定-50到50的误差范围，以免过于难触发
            isLeft = true;
        else if (x > mRight - 50 && x < mRight + 50)
            isRight = true;
        if (y < mTop + 50 && y > mTop - 50)
            isTop = true;
        else if (y > mBottom - 50 && y < mBottom + 50)
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

    /**
     * 利用Path来规定canvas绘画时将要忽略的区域，规定好方框位置后即可画半透明区域
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mIsMeasured) {//第一次测量完毕时，getWidth才有值，因此在这里初始化方框的四条边
            mLeft = (getWidth() - mSize) / 2;
            mRight = (getWidth() + mSize) / 2;
            mTop = (getHeight() - mSize) / 2;
            mBottom = (getHeight() + mSize) / 2;
            mMinX = 0;
            mMaxX = getWidth();
            mMinY = 0;
            mMaxY = getHeight();
            mIsMeasured = true;
        }
        mPath.reset();
        mPath.addRect(mLeft, mTop, mRight, mBottom, Path.Direction.CW);
        //保存当前canvas 状态
        canvas.save();
        //将当前画布可以绘画区域限制死为预览框外的区域
        canvas.clipPath(mPath, Region.Op.DIFFERENCE);
        //绘画半透明遮罩
        canvas.drawColor(Color.parseColor("#90000000"));
        //还原画布状态
        canvas.restore();
        //显示像素
        canvas.drawText((mRight - mLeft) + "x" + (mBottom - mTop), (mLeft + mRight) / 2, (mTop + mBottom) / 2, mPaintText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getLength(widthMeasureSpec), getLength(heightMeasureSpec));
    }

    /**
     * wrapcontent和matchparent都填充满父布局，后面有需求可以再改
     */
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