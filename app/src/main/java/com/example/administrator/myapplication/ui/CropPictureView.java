package com.example.administrator.myapplication.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.util.BitmapUtil;
import com.example.administrator.myapplication.util.PixelUtil;

import static android.content.ContentValues.TAG;

//Created by Administrator on 2018/1/23.

public class CropPictureView extends View {

    private static final int DEFAULT_DEVIATION_RANGE = 50;//默认触摸点误差范围
    private static final float DEFAULT_BUTTON_SCALE_SMALL = 1f;//默认按钮缩小时倍数
    private static final float DEFAULT_BUTTON_SCALE_BIG = 1.3f;//默认按钮放大时倍数
    private static final int DEFAULT_SIZE = 250;//方形框默认边长250dp
    private static final int DEFAULT_MIN_SIZE = 100;//方形框默认最小边长50dp
    private static final int LOCATION_TOP = 100;//标志：触摸点位置在方形框顶边
    private static final int LOCATION_RIGHT_TOP = 101;//标志：触摸点位置在方形框右上角
    private static final int LOCATION_RIGHT = 102;//标志：触摸点位置在方形框右边
    private static final int LOCATION_RIGHT_BOTTOM = 103;//标志：触摸点位置在方形框右下角
    private static final int LOCATION_BOTTOM = 104;//标志：触摸点位置在方形框底边
    private static final int LOCATION_LEFT_BOTTOM = 105;//标志：触摸点位置在方形框左下角
    private static final int LOCATION_LEFT = 106;//标志：触摸点位置在方形框左边
    private static final int LOCATION_LEFT_TOP = 107;//标志：触摸点位置在方形框左上角
    private static final int LOCATION_INSIDE = 108;//标志：触摸点位置在方形框内部
    private static final int MODE_POINT_SINGLE = 200;//标志：触摸模式为单指
    private static final int MODE_POINT_DOUBLE = 201;//标志：触摸模式为双指
    private static final int MODE_NONE = 202;//标志：触摸模式为无
    private boolean mIsProportionFreedom = true;//标志：是否是自由比例模式
    private boolean mProportionIsChanging = false;//标志：是否正在改变比例
    private float mFrameProportion = 1f;//默认方框的比例为1：1
    private float mMinX;//方形框在x方向最小值
    private float mMaxX;//方形框在x方向最大值
    private float mMinY;//方形框在y方向最小值
    private float mMaxY;//方形框在y方向最大值
    private float mPictureProportion;//裁剪图片原比例
    private float mMinSize;//方框最小边长
    private float mSize;//方框边长
    private float mLeft;//方形框左边位置
    private float mRight;//方形框右边位置
    private float mTop;//方形框顶边位置
    private float mBottom;//方形框底边位置
    private float mPreX;//上一个触摸点
    private float mPreY;//上一个触摸点
    private int mLocation = 0;//记录触摸点初始位置
    private int mMode = MODE_NONE;//默认为无
    private float mPreLength;//记录缩放时上一个两点间距
    private Paint mPaintText = new Paint();//文本画笔，全局生成，避免重复实例化造成onDraw时间过长
    private boolean mIsMeasured = false;//标志位：只在第一次测量时设置方形框位置和边长
    private Path mPath;//同文本画笔，用于把半透明区域限制在方形框外面
    private Paint mPaint;//分割线画笔

    //各个按钮的bitmap
    private Bitmap mCropButtonHor;
    private Bitmap mCropButtonVer;
    private Bitmap mCropButtonLeftTop;
    private Bitmap mCropButtonRightTop;
    private Bitmap mCropButtonLeftBottom;
    private Bitmap mCropButtonRightBottom;

    //各个按钮的matrix，用于控制位置和缩放
    private Matrix mMatrixButtonLeftTop;
    private Matrix mMatrixButtonRightTop;
    private Matrix mMatrixButtonRightBottom;
    private Matrix mMatrixButtonLeftBottom;
    private Matrix mMatrixButtonLeft;
    private Matrix mMatrixButtonRight;
    private Matrix mMatrixButtonTop;
    private Matrix mMatrixButtonBottom;

    //各个按钮的目标缩放倍数，默认为1
    private float mButtonLeftTopTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonRightTopTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonLeftBottomTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonRightBottomTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonLeftTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonTopTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonBottomTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonRightTargetScale = DEFAULT_BUTTON_SCALE_SMALL;

    //各个按钮的当前缩放倍数，默认为1
    private float mButtonLeftTopCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonRightTopCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonLeftBottomCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonRightBottomCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonLeftCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonTopCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonBottomCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;
    private float mButtonRightCurrentScale = DEFAULT_BUTTON_SCALE_SMALL;

    //文字Y轴偏移量，用于使文字居中
    private float mTextOffsetY;

    //触摸点偏差，如裁剪框右上角为（x,y），手指无法精准触摸到该点，因此给出偏差范围，并且记录下偏差deviationX与deviationY，以在移动时获得准确的触摸点偏移offset
    private float mDeviationX;
    private float mDeviationY;

    //这些成员变量用于改变裁剪框比例时的动画效果，如mTargetLeft是改变比例后的最终Left值，则mPageLeft为每一帧mLeft的偏移量，默认mPageLeft为(mTargetLeft-mLeft)/10,动画时间为10帧
    private float mTargetLeft;
    private float mTargetRight;
    private float mTargetTop;
    private float mTargetBottom;
    private float mPageLeft;
    private float mPageRight;
    private float mPageTop;
    private float mPageBottom;


    public CropPictureView(Context context) {
        this(context, null);
    }

    public CropPictureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 初始化一些成员变量
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CropPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //绘制透明预览框
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextSize(50);
        mPaintText.setAntiAlias(true);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        //创建圆形预览框
        mPath = new Path();
        mSize = PixelUtil.Dp2Px(context, DEFAULT_SIZE);
        mMinSize = PixelUtil.Dp2Px(context, DEFAULT_MIN_SIZE);
        //分隔线
        mPaint = new Paint();
        mPaint.setARGB(100, 255, 255, 255);
        mPaint.setStrokeWidth(2);
        //加载按钮背景
        mCropButtonHor = BitmapUtil.ScaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crop_button_h), 0.5f);
        mCropButtonVer = BitmapUtil.ScaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crop_button_v), 0.5f);
        mCropButtonLeftTop = BitmapUtil.ScaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crop_button_left_top), 0.5f);
        mCropButtonRightTop = BitmapUtil.ScaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crop_button_right_top), 0.5f);
        mCropButtonLeftBottom = BitmapUtil.ScaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crop_button_left_bottom), 0.5f);
        mCropButtonRightBottom = BitmapUtil.ScaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crop_button_right_bottom), 0.5f);
        //实例化按钮Matrix
        mMatrixButtonLeftTop = new Matrix();
        mMatrixButtonRightTop = new Matrix();
        mMatrixButtonRightBottom = new Matrix();
        mMatrixButtonLeftBottom = new Matrix();
        mMatrixButtonLeft = new Matrix();
        mMatrixButtonRight = new Matrix();
        mMatrixButtonTop = new Matrix();
        mMatrixButtonBottom = new Matrix();
        //计算Y轴文字偏移
        Paint.FontMetrics fm = mPaintText.getFontMetrics();
        mTextOffsetY = (fm.descent - fm.ascent) / 2 - fm.descent;
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
     *
     * @param event
     * @return true 处理点击事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if (isInsideFrame(event.getX(), event.getY())) {
                    mMode = MODE_POINT_SINGLE;
                    mPreX = event.getX();
                    mPreY = event.getY();
                    mLocation = findLocation(mPreX, mPreY);//根据按下时触摸点坐标找到其在方框中的位置
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (isInsideFrame(event.getX(), event.getY()) && isInsideFrame(event.getX(1), event.getY(1))) {
                    mMode = MODE_POINT_DOUBLE;
                    float disX = Math.abs(event.getX(0) - event.getX(1));
                    float disY = Math.abs(event.getY(0) - event.getY(1));
                    mPreLength = (float) Math.sqrt(disX * disX + disY * disY);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mMode == MODE_POINT_DOUBLE) {//双点触控
                    //计算两点间距
                    float disX = Math.abs(event.getX(0) - event.getX(1));
                    float disY = Math.abs(event.getY(0) - event.getY(1));
                    float curLength = (float) Math.sqrt(disX * disX + disY * disY);
                    //当前间距和之前间距相减除以2，因为left+scale和right+scale，宽度扩大2*scale=间距之差
                    float scaleX = (curLength - mPreLength) / 2;
                    float scaleY = scaleX / mFrameProportion;
                    float newLeft = mLeft - scaleX;//注意四舍五入
                    float newTop = mTop - scaleY;
                    float newRight = mRight + scaleX;
                    float newBottom = mBottom + scaleY;
                    if ((scaleX > 0 && newLeft >= mMinX && newRight <= mMaxX && newTop >= mMinY && newBottom <= mMaxY) || (scaleX <= 0 && newLeft <= newRight - mMinSize && newTop <= newBottom - mMinSize)) {
                        mLeft = newLeft;
                        mTop = newTop;
                        mRight = newRight;
                        mBottom = newBottom;
                        Log.e(TAG, "onTouchEvent1: " + scaleX + "  " + scaleY);
                        mPreLength = curLength;//当前间距已是之前间距
                    } else {
                        if ((newTop < mMinY || newBottom > mMaxY || newTop > newBottom - mMinSize) && (newLeft < mMinX || newRight > mMaxX || newLeft > newRight - mMinSize)) {
                            if ((scaleX<0&&mFrameProportion<1)||(scaleX>=0&&mFrameProportion>=1)){
                                if (newLeft < mMinX&&newRight>mMaxX){
                                    scaleX = Math.min(mLeft - mMinX,mMaxX - mRight);
                                    scaleY = scaleX / mFrameProportion;
                                } else if (newLeft < mMinX) {
                                    scaleX = mLeft - mMinX;
                                    scaleY = scaleX / mFrameProportion;
                                } else if (newLeft > newRight - mMinSize) {
                                    scaleX = -(mRight - mLeft - mMinSize) / 2;
                                    scaleY = scaleX / mFrameProportion;
                                } else if (newRight > mMaxX) {
                                    scaleX = mMaxX - mRight;
                                    scaleY = scaleX / mFrameProportion;
                                }
                            }else {
                                if (newTop < mMinY&&newBottom>mMaxY){
                                    scaleY = Math.min(mTop - mMinY,mMaxY - mBottom);
                                    scaleX = scaleY * mFrameProportion;
                                } else if (newTop < mMinY) {
                                    scaleY = mTop - mMinY;
                                    scaleX = scaleY * mFrameProportion;
                                } else if (newBottom > mMaxY) {
                                    scaleY = mMaxY - mBottom;
                                    scaleX = scaleY * mFrameProportion;
                                } else if (newTop > newBottom - mMinSize) {
                                    scaleY = -(mBottom - mTop - mMinSize) / 2;
                                    scaleX = scaleY * mFrameProportion;
                                }
                            }
                        }else if (newTop < mMinY || newBottom > mMaxY || newTop > newBottom - mMinSize) {
                            if (newTop < mMinY&&newBottom>mMaxY){
                                scaleY = Math.min(mTop - mMinY,mMaxY - mBottom);
                                scaleX = scaleY * mFrameProportion;
                            } else if (newTop < mMinY) {
                                scaleY = mTop - mMinY;
                                scaleX = scaleY * mFrameProportion;
                            } else if (newBottom > mMaxY) {
                                scaleY = mMaxY - mBottom;
                                scaleX = scaleY * mFrameProportion;
                            } else if (newTop > newBottom - mMinSize) {
                                scaleY = -(mBottom - mTop - mMinSize) / 2;
                                scaleX = scaleY * mFrameProportion;
                            }
                        } else if(newLeft < mMinX || newRight > mMaxX || newLeft > newRight - mMinSize){
                            if (newLeft < mMinX&&newRight>mMaxX){
                                scaleX = Math.min(mLeft - mMinX,mMaxX - mRight);
                                scaleY = scaleX / mFrameProportion;
                            } else if (newLeft < mMinX) {
                                scaleX = mLeft - mMinX;
                                scaleY = scaleX / mFrameProportion;
                            } else if (newLeft > newRight - mMinSize) {
                                scaleX = -(mRight - mLeft - mMinSize) / 2;
                                scaleY = scaleX / mFrameProportion;
                            } else if (newRight > mMaxX) {
                                scaleX = mMaxX - mRight;
                                scaleY = scaleX / mFrameProportion;
                            }
                        }
                        mRight += scaleX;
                        mLeft -= scaleX;
                        mTop -= scaleY;
                        mBottom += scaleY;
                        Log.e(TAG, "onTouchEvent: " + scaleX + "  " + scaleY);
                        mPreLength = curLength;//当前间距已是之前间距
                    }
                } else if (mMode == MODE_POINT_SINGLE) {//单点触控
                    float newX = event.getX();
                    float newY = event.getY();
                    float offsetX = newX - mPreX;
                    float offsetY = newY - mPreY;
                    float newRight;
                    float newBottom;
                    float newLeft;
                    float newTop;
                    float offsetToBorderX;
                    float offsetToBorderY;
                    switch (mLocation) {//根据落点在方框位置有不同操作，如在顶边，则top加上触点移动偏移量

                        case LOCATION_TOP:
                            newTop = mTop + offsetY;
                            float[] values = calculateBorderAxisValue(LOCATION_TOP, newTop, newY);//计算newTop和mPreY，接近限制值（mMinY、mMaxY）时,newTop需要进行限制，且mPreY需要进行判断赋值以获得更好的用户体验，其他边界同理。
                            mTop = values[0];
                            mPreY = values[1];
                            mPreX = newX;
                            break;

                        case LOCATION_RIGHT:
                            newRight = mRight + offsetX;
                            values = calculateBorderAxisValue(LOCATION_RIGHT, newRight, newX);
                            mRight = values[0];
                            mPreX = values[1];
                            mPreY = newY;
                            break;

                        case LOCATION_BOTTOM:
                            newBottom = mBottom + offsetY;
                            values = calculateBorderAxisValue(LOCATION_BOTTOM, newBottom, newY);
                            mBottom = values[0];
                            mPreY = values[1];
                            mPreX = newX;
                            break;

                        case LOCATION_LEFT:
                            newLeft = mLeft + offsetX;
                            values = calculateBorderAxisValue(LOCATION_LEFT, newLeft, newX);
                            mLeft = values[0];
                            mPreX = values[1];
                            mPreY = newY;
                            break;

                        case LOCATION_RIGHT_TOP:
                            if (mIsProportionFreedom) {//自由模式时角落跟随触摸点移动即可
                                newRight = mRight + offsetX;
                                values = calculateBorderAxisValue(LOCATION_RIGHT, newRight, newX);
                                mRight = values[0];
                                mPreX = values[1];
                                newTop = mTop + offsetY;
                                values = calculateBorderAxisValue(LOCATION_TOP, newTop, newY);
                                mTop = values[0];
                                mPreY = values[1];
                            } else {//比例模式时触摸点需与边界值比较后按比例进行缩放，如比例1：1，右下角（100，100），触摸点（101，102），则裁剪框右上角变为（101，101），即以X偏移量和Y偏移量中最小值作为裁剪框边界偏移量
                                offsetToBorderX = newX - (mRight + mDeviationX);
                                offsetToBorderY = newY - (mTop + mDeviationY);
                                if (offsetToBorderX * offsetToBorderY < 0) {
                                    if (Math.abs(offsetToBorderY) < Math.abs(offsetToBorderX)) {
                                        mTop = mTop + offsetToBorderY;
                                        //这里是为了使裁剪框边界值不超过限制并能够保持比例
                                        mTop = limitAxisValues(LOCATION_TOP, mTop);//约束mTop
                                        mRight = mLeft + ((mBottom - mTop) * mFrameProportion);//根据mTop按比例算出mRight
                                        mRight = limitAxisValues(LOCATION_RIGHT, mRight);//约束mRight
                                        mTop = mBottom - ((mRight - mLeft) / mFrameProportion);//根据mRight按比例算出mTop
                                    } else {
                                        mRight = mRight + offsetToBorderX;
                                        mRight = limitAxisValues(LOCATION_RIGHT, mRight);
                                        mTop = mBottom - ((mRight - mLeft) / mFrameProportion);
                                        mTop = limitAxisValues(LOCATION_TOP, mTop);
                                        mRight = mLeft + ((mBottom - mTop) * mFrameProportion);
                                    }
                                }
                            }
                            break;


                        case LOCATION_RIGHT_BOTTOM:
                            if (mIsProportionFreedom) {
                                newRight = mRight + offsetX;
                                values = calculateBorderAxisValue(LOCATION_RIGHT, newRight, newX);
                                mRight = values[0];
                                mPreX = values[1];
                                newBottom = mBottom + offsetY;
                                values = calculateBorderAxisValue(LOCATION_BOTTOM, newBottom, newY);
                                mBottom = values[0];
                                mPreY = values[1];
                            } else {
                                offsetToBorderX = newX - (mRight + mDeviationX);
                                offsetToBorderY = newY - (mBottom + mDeviationY);
                                if (offsetToBorderX * offsetToBorderY > 0) {
                                    if (Math.abs(offsetToBorderY) < Math.abs(offsetToBorderX)) {
                                        mBottom = mBottom + offsetToBorderY;
                                        mBottom = limitAxisValues(LOCATION_BOTTOM, mBottom);
                                        mRight = mLeft + (mBottom - mTop) * mFrameProportion;
                                        mRight = limitAxisValues(LOCATION_RIGHT, mRight);
                                        mBottom = mTop + (mRight - mLeft) / mFrameProportion;
                                    } else {
                                        mRight = mRight + offsetToBorderX;
                                        mRight = limitAxisValues(LOCATION_RIGHT, mRight);
                                        mBottom = mTop + (mRight - mLeft) / mFrameProportion;
                                        mBottom = limitAxisValues(LOCATION_BOTTOM, mBottom);
                                        mRight = mLeft + (mBottom - mTop) * mFrameProportion;
                                    }
                                }
                            }
                            break;


                        case LOCATION_LEFT_BOTTOM:
                            if (mIsProportionFreedom) {
                                newLeft = mLeft + offsetX;
                                values = calculateBorderAxisValue(LOCATION_LEFT, newLeft, newX);
                                mLeft = values[0];
                                mPreX = values[1];
                                newBottom = mBottom + offsetY;
                                values = calculateBorderAxisValue(LOCATION_BOTTOM, newBottom, newY);
                                mBottom = values[0];
                                mPreY = values[1];
                            } else {
                                offsetToBorderX = newX - (mLeft + mDeviationX);
                                offsetToBorderY = newY - (mBottom + mDeviationY);
                                if (offsetToBorderX * offsetToBorderY < 0) {
                                    if (Math.abs(offsetToBorderY) < Math.abs(offsetToBorderX)) {
                                        mBottom = mBottom + offsetToBorderY;
                                        mBottom = limitAxisValues(LOCATION_BOTTOM, mBottom);
                                        mLeft = mRight - (mBottom - mTop) * mFrameProportion;
                                        mLeft = limitAxisValues(LOCATION_LEFT, mLeft);
                                        mBottom = mTop + (mRight - mLeft) / mFrameProportion;
                                    } else {
                                        mLeft = mLeft + offsetToBorderX;
                                        mLeft = limitAxisValues(LOCATION_LEFT, mLeft);
                                        mBottom = mTop + (mRight - mLeft) / mFrameProportion;
                                        mBottom = limitAxisValues(LOCATION_BOTTOM, mBottom);
                                        mLeft = mRight - (mBottom - mTop) * mFrameProportion;
                                    }
                                }
                            }
                            break;

                        case LOCATION_LEFT_TOP:
                            if (mIsProportionFreedom) {
                                newLeft = mLeft + offsetX;
                                values = calculateBorderAxisValue(LOCATION_LEFT, newLeft, newX);
                                mLeft = values[0];
                                mPreX = values[1];
                                newTop = mTop + offsetY;
                                values = calculateBorderAxisValue(LOCATION_TOP, newTop, newY);
                                mTop = values[0];
                                mPreY = values[1];
                            } else {
                                offsetToBorderX = newX - (mLeft + mDeviationX);
                                offsetToBorderY = newY - (mTop + mDeviationY);
                                if (offsetToBorderX * offsetToBorderY > 0) {
                                    if (Math.abs(offsetToBorderY) < Math.abs(offsetToBorderX)) {
                                        mTop += offsetToBorderY;
                                        mTop = limitAxisValues(LOCATION_TOP, mTop);
                                        mLeft = mRight - ((mBottom - mTop) * mFrameProportion);
                                        mLeft = limitAxisValues(LOCATION_LEFT, mLeft);
                                        mTop = mBottom - ((mRight - mLeft) / mFrameProportion);
                                    } else {
                                        mLeft += offsetToBorderX;
                                        mLeft = limitAxisValues(LOCATION_LEFT, mLeft);
                                        mTop = mBottom - ((mRight - mLeft) / mFrameProportion);
                                        mTop = limitAxisValues(LOCATION_TOP, mTop);
                                        mLeft = mRight - ((mBottom - mTop) * mFrameProportion);
                                    }
                                }
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

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mMode = MODE_NONE;
                performClick();//抬起时触发点击，暂无需实现
                resetButtonScale();
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 利用Path来规定canvas绘画时将要忽略的区域，规定好方框位置后即可画半透明区域
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //第一次测量完毕时，getWidth才有值，因此在这里初始化方框的四条边
        if (!mIsMeasured) {
            mLeft = (getWidth() - mSize) / 2;
            mRight = (getWidth() + mSize) / 2;
            mTop = (getHeight() - mSize) / 2;
            mBottom = (getHeight() + mSize) / 2;
            mMinX = 0;
            mMaxX = getWidth();
            mMinY = 0;
            mMaxY = getHeight();
            mPictureProportion = (mMaxX - mMinX) * 1.0f / (mMaxY - mMinY);
            mIsMeasured = true;
        }

        //判断是否正在变换比例，如果是，则调用比例动画函数
        if (mProportionIsChanging) {
            mProportionIsChanging = proportionAnimator();
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
        canvas.drawText((int) (mRight - mLeft + 0.5f) + "x" + (int) (mBottom - mTop + 0.5f), (mLeft + mRight) / 2, (mTop + mBottom) / 2 + mTextOffsetY, mPaintText);

        //画分割线
        canvas.drawLine(mLeft, mTop, mLeft, mBottom, mPaint);
        canvas.drawLine(mLeft + (mRight - mLeft) / 3 + 2, mTop, mLeft + (mRight - mLeft) / 3 + 2, mBottom, mPaint);
        canvas.drawLine(mLeft + (mRight - mLeft) * 2 / 3, mTop, mLeft + (mRight - mLeft) * 2 / 3, mBottom, mPaint);
        canvas.drawLine(mRight, mTop, mRight, mBottom, mPaint);
        canvas.drawLine(mLeft, mTop, mRight, mTop, mPaint);
        canvas.drawLine(mLeft, mTop + (mBottom - mTop) / 3, mRight, mTop + (mBottom - mTop) / 3, mPaint);
        canvas.drawLine(mLeft, mTop + (mBottom - mTop) * 2 / 3, mRight, mTop + (mBottom - mTop) * 2 / 3, mPaint);
        canvas.drawLine(mLeft, mBottom, mRight, mBottom, mPaint);

        //四个角落的按钮Matrix设置偏移和缩放，这里先由ScaleAnimator函数计算出此次刷新的当前倍数，
        mButtonLeftTopCurrentScale = scaleAnimator(mButtonLeftTopTargetScale, mButtonLeftTopCurrentScale);
        mMatrixButtonLeftTop.setTranslate(mLeft - mCropButtonLeftTop.getWidth() * mButtonLeftTopCurrentScale / 2, mTop - mCropButtonLeftTop.getHeight() * mButtonLeftTopCurrentScale / 2);
        mMatrixButtonLeftTop.preScale(mButtonLeftTopCurrentScale, mButtonLeftTopCurrentScale);

        mButtonRightTopCurrentScale = scaleAnimator(mButtonRightTopTargetScale, mButtonRightTopCurrentScale);
        mMatrixButtonRightTop.setTranslate(mRight - mCropButtonRightTop.getWidth() * mButtonRightTopCurrentScale / 2, mTop - mCropButtonRightTop.getHeight() * mButtonRightTopCurrentScale / 2);
        mMatrixButtonRightTop.preScale(mButtonRightTopCurrentScale, mButtonRightTopCurrentScale);

        mButtonLeftBottomCurrentScale = scaleAnimator(mButtonLeftBottomTargetScale, mButtonLeftBottomCurrentScale);
        mMatrixButtonLeftBottom.setTranslate(mLeft - mCropButtonLeftBottom.getWidth() * mButtonLeftBottomCurrentScale / 2, mBottom - mCropButtonLeftBottom.getHeight() * mButtonLeftBottomCurrentScale / 2);
        mMatrixButtonLeftBottom.preScale(mButtonLeftBottomCurrentScale, mButtonLeftBottomCurrentScale);

        mButtonRightBottomCurrentScale = scaleAnimator(mButtonRightBottomTargetScale, mButtonRightBottomCurrentScale);
        mMatrixButtonRightBottom.setTranslate(mRight - mCropButtonRightBottom.getWidth() * mButtonRightBottomCurrentScale / 2, mBottom - mCropButtonRightBottom.getHeight() * mButtonRightBottomCurrentScale / 2);
        mMatrixButtonRightBottom.preScale(mButtonRightBottomCurrentScale, mButtonRightBottomCurrentScale);

        canvas.drawBitmap(mCropButtonLeftTop, mMatrixButtonLeftTop, null);
        canvas.drawBitmap(mCropButtonRightTop, mMatrixButtonRightTop, null);
        canvas.drawBitmap(mCropButtonLeftBottom, mMatrixButtonLeftBottom, null);
        canvas.drawBitmap(mCropButtonRightBottom, mMatrixButtonRightBottom, null);

        //四条边上的按钮Matrix偏移和缩放
        if (mIsProportionFreedom) {
            mButtonLeftCurrentScale = scaleAnimator(mButtonLeftTargetScale, mButtonLeftCurrentScale);
            mButtonRightCurrentScale = scaleAnimator(mButtonRightTargetScale, mButtonRightCurrentScale);
            mButtonTopCurrentScale = scaleAnimator(mButtonTopTargetScale, mButtonTopCurrentScale);
            mButtonBottomCurrentScale = scaleAnimator(mButtonBottomTargetScale, mButtonBottomCurrentScale);

            mMatrixButtonLeft.setTranslate(mLeft - mCropButtonVer.getWidth() * mButtonLeftCurrentScale / 2, mTop - mCropButtonVer.getHeight() * mButtonLeftCurrentScale / 2 + (mBottom - mTop) / 2);
            mMatrixButtonRight.setTranslate(mRight - mCropButtonVer.getWidth() * mButtonRightCurrentScale / 2, mTop - mCropButtonVer.getHeight() * mButtonRightCurrentScale / 2 + (mBottom - mTop) / 2);
            mMatrixButtonTop.setTranslate(mLeft - mCropButtonHor.getWidth() * mButtonTopCurrentScale / 2 + (mRight - mLeft) / 2, mTop - mCropButtonHor.getHeight() * mButtonTopCurrentScale / 2);
            mMatrixButtonBottom.setTranslate(mLeft - mCropButtonHor.getWidth() * mButtonBottomCurrentScale / 2 + (mRight - mLeft) / 2, mBottom - mCropButtonHor.getHeight() * mButtonBottomCurrentScale / 2);

            mMatrixButtonLeft.preScale(mButtonLeftCurrentScale, mButtonLeftCurrentScale);
            mMatrixButtonRight.preScale(mButtonRightCurrentScale, mButtonRightCurrentScale);
            mMatrixButtonTop.preScale(mButtonTopCurrentScale, mButtonTopCurrentScale);
            mMatrixButtonBottom.preScale(mButtonBottomCurrentScale, mButtonBottomCurrentScale);

            canvas.drawBitmap(mCropButtonVer, mMatrixButtonLeft, null);
            canvas.drawBitmap(mCropButtonVer, mMatrixButtonRight, null);
            canvas.drawBitmap(mCropButtonHor, mMatrixButtonTop, null);
            canvas.drawBitmap(mCropButtonHor, mMatrixButtonBottom, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getLength(widthMeasureSpec), getLength(heightMeasureSpec));
    }

    /**
     * wrap_content和match_parent都填充满父布局，后面有需求可以再改
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

    /**
     * 判断落点（x,y）是否在方框内
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInsideFrame(float x, float y) {
        return x >= mLeft - DEFAULT_DEVIATION_RANGE
                && x <= mRight + DEFAULT_DEVIATION_RANGE
                && y >= mTop - DEFAULT_DEVIATION_RANGE
                && y <= mBottom + DEFAULT_DEVIATION_RANGE;
    }

    /**
     * 找到点（x,y）在方形框中的位置，例如：左上角。算完时改变对应位置的按钮缩放倍数
     * 触摸点在四个角落时顺便计算触摸点的偏差量，用于比例约束时放大缩小裁剪框的计算
     */
    private int findLocation(float x, float y) {
        if (x <= mLeft + DEFAULT_DEVIATION_RANGE) {
            if (y <= mTop + DEFAULT_DEVIATION_RANGE) {
                mButtonLeftTopTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                mDeviationX = mPreX - mLeft;
                mDeviationY = mPreY - mTop;
                return LOCATION_LEFT_TOP;
            } else if (y >= mBottom - DEFAULT_DEVIATION_RANGE) {
                mButtonLeftBottomTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                mDeviationX = mPreX - mLeft;
                mDeviationY = mPreY - mBottom;
                return LOCATION_LEFT_BOTTOM;
            } else if (y >= (mBottom + mTop) / 2 - DEFAULT_DEVIATION_RANGE && y <= (mBottom + mTop) / 2 + DEFAULT_DEVIATION_RANGE) {
                mButtonLeftTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                if (mIsProportionFreedom) {
                    mDeviationX = mPreX - mLeft;
                    mDeviationY = mPreY - (mTop + mBottom) / 2;
                    return LOCATION_LEFT;
                } else {
                    return LOCATION_INSIDE;
                }
            } else return LOCATION_INSIDE;
        } else if (x >= mRight - DEFAULT_DEVIATION_RANGE) {
            if (y <= mTop + DEFAULT_DEVIATION_RANGE) {
                mButtonRightTopTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                mDeviationX = mPreX - mRight;
                mDeviationY = mPreY - mTop;
                return LOCATION_RIGHT_TOP;
            } else if (y >= mBottom - DEFAULT_DEVIATION_RANGE) {
                mButtonRightBottomTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                mDeviationX = mPreX - mRight;
                mDeviationY = mPreY - mBottom;
                return LOCATION_RIGHT_BOTTOM;
            } else if (y >= (mBottom + mTop) / 2 - DEFAULT_DEVIATION_RANGE && y <= (mBottom + mTop) / 2 + DEFAULT_DEVIATION_RANGE) {
                mButtonRightTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                if (mIsProportionFreedom) {
                    mDeviationX = mPreX - mRight;
                    mDeviationY = mPreY - (mTop + mBottom) / 2;
                    return LOCATION_RIGHT;
                } else {
                    return LOCATION_INSIDE;
                }
            } else return LOCATION_INSIDE;
        } else if (y <= mTop + DEFAULT_DEVIATION_RANGE) {
            if (x <= (mLeft + mRight) / 2 + DEFAULT_DEVIATION_RANGE && x >= (mLeft + mRight) / 2 - DEFAULT_DEVIATION_RANGE) {
                mButtonTopTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                if (mIsProportionFreedom) {
                    mDeviationX = mPreX - (mLeft + mRight) / 2;
                    mDeviationY = mPreY - mTop;
                    return LOCATION_TOP;
                } else {
                    return LOCATION_INSIDE;
                }
            } else return LOCATION_INSIDE;
        } else if (y >= mBottom - DEFAULT_DEVIATION_RANGE) {
            if (x <= (mLeft + mRight) / 2 + DEFAULT_DEVIATION_RANGE && x >= (mLeft + mRight) / 2 - DEFAULT_DEVIATION_RANGE) {
                mButtonBottomTargetScale = DEFAULT_BUTTON_SCALE_BIG;
                if (mIsProportionFreedom) {
                    mDeviationX = mPreX - (mLeft + mRight) / 2;
                    mDeviationY = mPreY - mBottom;
                    return LOCATION_BOTTOM;
                } else {
                    return LOCATION_INSIDE;
                }
            } else return LOCATION_INSIDE;
        } else return LOCATION_INSIDE;
    }

    /**
     * 限制裁剪框各边界的坐标值，如右边界不大于mMaxX，不小于mLeft+mMinSize，如果超过，则赋最大值或最小值
     *
     * @param location
     * @param value
     * @return
     */
    private float limitAxisValues(int location, float value) {
        float minValue = 0;
        float maxValue = 0;
        switch (location) {
            case LOCATION_LEFT:
                minValue = mMinX;
                maxValue = mRight - mMinSize;
                break;
            case LOCATION_TOP:
                minValue = mMinY;
                maxValue = mBottom - mMinSize;
                break;
            case LOCATION_RIGHT:
                minValue = mLeft + mMinSize;
                maxValue = mMaxX;
                break;
            case LOCATION_BOTTOM:
                minValue = mTop + mMinSize;
                maxValue = mMaxY;
                break;
        }
        if (value < minValue) return minValue;
        else if (value > maxValue) return maxValue;
        else return value;
    }

    /**
     * 计算边界坐标值，用于限制方框不会被移出被剪裁的图片外面，如当location为left，则left边的位移范围为mMinX到（mRight-mMinSize），超出这个范围则返回边界值即可
     * 由于触摸点与边界值有误差，需要在计算方框边位置的时候顺便计算前一个触摸点位置并返回，因此返回一个int数组
     *
     * @param location
     * @param borderAxisValue
     * @param fingerAxisValue
     * @return
     */
    private float[] calculateBorderAxisValue(int location, float borderAxisValue, float fingerAxisValue) {
        float[] result = new float[2];
        float minValue = 0;
        float maxValue = 0;
        switch (location) {
            case LOCATION_LEFT:
                minValue = mMinX;
                maxValue = mRight - mMinSize;
                break;
            case LOCATION_TOP:
                minValue = mMinY;
                maxValue = mBottom - mMinSize;
                break;
            case LOCATION_RIGHT:
                minValue = mLeft + mMinSize;
                maxValue = mMaxX;
                break;
            case LOCATION_BOTTOM:
                minValue = mTop + mMinSize;
                maxValue = mMaxY;
                break;
        }
        if (borderAxisValue >= minValue && borderAxisValue <= maxValue) {
            result[0] = borderAxisValue;
            result[1] = fingerAxisValue;
        } else if (borderAxisValue < minValue) {
            result[0] = minValue;
            if (fingerAxisValue < minValue) {
                result[1] = minValue;
            } else {
                result[1] = fingerAxisValue;
            }
        } else {
            result[0] = maxValue;
            if (fingerAxisValue > maxValue) {
                result[1] = maxValue;
            } else {
                result[1] = fingerAxisValue;
            }
        }
        return result;
    }

    /**
     * 重置按钮缩放倍数
     */
    private void resetButtonScale() {
        mButtonRightBottomTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
        mButtonLeftBottomTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
        mButtonRightTopTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
        mButtonLeftTopTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
        mButtonLeftTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
        mButtonTopTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
        mButtonBottomTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
        mButtonRightTargetScale = DEFAULT_BUTTON_SCALE_SMALL;
    }

    /**
     * 如缩放倍数从1.0到2.0，该函数将该变化分成10次返回给当前倍数，连续刷新后可达到一个动画效果，设置完成后ScaleAnimator会再次发出刷新信号，
     * 直到当前倍数等于目标倍数，ScaleAnimator就不会发出刷新信号，避免占用系统资源。
     *
     * @param targetScale
     * @param curScale
     * @return
     */
    public float scaleAnimator(float targetScale, float curScale) {
        double page = (DEFAULT_BUTTON_SCALE_BIG - DEFAULT_BUTTON_SCALE_SMALL) / 10;
        if (targetScale == DEFAULT_BUTTON_SCALE_BIG) {
            if (curScale + page >= targetScale) return targetScale;
            else curScale += page;
        } else if (targetScale == DEFAULT_BUTTON_SCALE_SMALL) {
            if (curScale - page <= targetScale) return targetScale;
            else curScale -= page;
        }
        invalidate();
        return curScale;
    }

    /**
     * 设置裁剪框是否比例自由
     *
     * @param isProportionFreedom
     */
    public void setProportionFreedom(boolean isProportionFreedom) {
        mIsProportionFreedom = isProportionFreedom;
        if (isProportionFreedom) mFrameProportion = 1f;
        invalidate();
    }

    /**
     * 设置方框比例
     *
     * @param proportionX
     * @param proportionY
     */
    public void setProportion(int proportionX, int proportionY) {
        mFrameProportion = proportionX * 1.0f / proportionY;
        float newProportion = proportionX * 1.0f / proportionY;
        if (newProportion > mPictureProportion) {
            mTargetLeft = mMinX;
            mTargetRight = mMaxX;
            float DistanceY = (mMaxX - mMinX) * proportionY / proportionX;
            mTargetTop = (mMaxY - mMinY) / 2 - DistanceY / 2;
            mTargetBottom = (mMaxY - mMinY) / 2 + DistanceY / 2;
        } else {
            mTargetTop = mMinY;
            mTargetBottom = mMaxY;
            float DistanceX = (mMaxY - mMinY) * proportionX / proportionY;
            mTargetLeft = (mMaxX - mMinX) / 2 - DistanceX / 2;
            mTargetRight = (mMaxX - mMinX) / 2 + DistanceX / 2;
        }
        mProportionIsChanging = true;
        mPageLeft = (mTargetLeft - mLeft) / 10;
        mPageRight = (mTargetRight - mRight) / 10;
        mPageTop = (mTargetTop - mTop) / 10;
        mPageBottom = (mTargetBottom - mBottom) / 10;
        invalidate();
    }

    /**
     * 比例改变时的动画效果，在onDraw中调用该方法，改变边界坐标值后又调用invalidate()再次进行onDraw以达到动画效果，如果达到目标坐标值，则mProportionIsChanging被设置为false，动画结束
     *
     * @return
     */
    private boolean proportionAnimator() {
        if (mLeft != mTargetLeft || mRight != mTargetRight || mTop != mTargetTop || mBottom != mTargetBottom) {
            mLeft += mPageLeft;
            if ((mPageLeft > 0 && mLeft > mTargetLeft) || (mPageLeft <= 0 && mLeft < mTargetLeft)) {
                mLeft = mTargetLeft;
            }
            mRight += mPageRight;
            if ((mPageRight > 0 && mRight > mTargetRight) || (mPageRight <= 0 && mRight < mTargetRight)) {
                mRight = mTargetRight;
            }
            mTop += mPageTop;
            if ((mPageTop > 0 && mTop > mTargetTop) || (mPageTop <= 0 && mTop < mTargetTop)) {
                mTop = mTargetTop;
            }
            mBottom += mPageBottom;
            if ((mPageBottom > 0 && mBottom > mTargetBottom) || (mPageBottom <= 0 && mBottom < mTargetBottom)) {
                mBottom = mTargetBottom;
            }
            invalidate();
            return true;
        } else {
            return false;
        }
    }

}
