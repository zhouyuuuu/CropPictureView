package com.example.administrator.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.myapplication.ui.CropPictureView;

public class CropPictureActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int COLOR_UNCHECKED_DEFAULT = 0XFF6C7074;
    private static final int COLOR_CHECKED_DEFAULT = 0XFF578FFF;
    private CropPictureView mCropPictureView;
    private TextView tvProportion_freedom;
    private TextView tvProportion_1_1;
    private TextView tvProportion_2_3;
    private TextView tvProportion_3_2;
    private TextView tvProportion_3_4;
    private TextView tvProportion_4_3;
    private TextView tvProportion_9_16;
    private TextView tvProportion_16_9;
    private View tvProportion_freedom_bg;
    private View tvProportion_1_1_bg;
    private View tvProportion_2_3_bg;
    private View tvProportion_3_2_bg;
    private View tvProportion_3_4_bg;
    private View tvProportion_4_3_bg;
    private View tvProportion_9_16_bg;
    private View tvProportion_16_9_bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCropPictureView = findViewById(R.id.cpv);
        findViewById(R.id.ll_proportion_freedom).setOnClickListener(this);
        findViewById(R.id.ll_proportion_1_1).setOnClickListener(this);
        findViewById(R.id.ll_proportion_2_3).setOnClickListener(this);
        findViewById(R.id.ll_proportion_3_2).setOnClickListener(this);
        findViewById(R.id.ll_proportion_3_4).setOnClickListener(this);
        findViewById(R.id.ll_proportion_4_3).setOnClickListener(this);
        findViewById(R.id.ll_proportion_9_16).setOnClickListener(this);
        findViewById(R.id.ll_proportion_16_9).setOnClickListener(this);
        tvProportion_freedom = findViewById(R.id.tv_proportion_freedom);
        tvProportion_1_1 = findViewById(R.id.tv_proportion_1_1);
        tvProportion_2_3 = findViewById(R.id.tv_proportion_2_3);
        tvProportion_3_2 = findViewById(R.id.tv_proportion_3_2);
        tvProportion_3_4 = findViewById(R.id.tv_proportion_3_4);
        tvProportion_4_3 = findViewById(R.id.tv_proportion_4_3);
        tvProportion_9_16 = findViewById(R.id.tv_proportion_9_16);
        tvProportion_16_9 = findViewById(R.id.tv_proportion_16_9);
        tvProportion_freedom_bg = findViewById(R.id.tv_proportion_freedom_bg);
        tvProportion_1_1_bg = findViewById(R.id.tv_proportion_1_1_bg);
        tvProportion_2_3_bg = findViewById(R.id.tv_proportion_2_3_bg);
        tvProportion_3_2_bg = findViewById(R.id.tv_proportion_3_2_bg);
        tvProportion_3_4_bg = findViewById(R.id.tv_proportion_3_4_bg);
        tvProportion_4_3_bg = findViewById(R.id.tv_proportion_4_3_bg);
        tvProportion_9_16_bg = findViewById(R.id.tv_proportion_9_16_bg);
        tvProportion_16_9_bg = findViewById(R.id.tv_proportion_16_9_bg);
    }

    @Override
    public void onClick(View v) {
        resetProportionButtonState();
        switch (v.getId()) {
            case R.id.ll_proportion_freedom:
                mCropPictureView.setProportionFreedom(true);
                tvProportion_freedom.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_freedom_bg.setBackgroundResource(R.drawable.edit_cut_crop_freedom_b);
                break;
            case R.id.ll_proportion_1_1:
                mCropPictureView.setProportionFreedom(false);
                tvProportion_1_1.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_1_1_bg.setBackgroundResource(R.drawable.edit_cut_crop_1_1_b);
                mCropPictureView.setProportion(1, 1);
                break;
            case R.id.ll_proportion_2_3:
                mCropPictureView.setProportionFreedom(false);
                tvProportion_2_3.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_2_3_bg.setBackgroundResource(R.drawable.edit_cut_crop_2_3_b);
                mCropPictureView.setProportion(2, 3);
                break;
            case R.id.ll_proportion_3_2:
                mCropPictureView.setProportionFreedom(false);
                tvProportion_3_2.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_3_2_bg.setBackgroundResource(R.drawable.edit_cut_crop_3_2_b);
                mCropPictureView.setProportion(3, 2);
                break;
            case R.id.ll_proportion_3_4:
                mCropPictureView.setProportionFreedom(false);
                tvProportion_3_4.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_3_4_bg.setBackgroundResource(R.drawable.edit_cut_crop_3_4_b);
                mCropPictureView.setProportion(3, 4);
                break;
            case R.id.ll_proportion_4_3:
                mCropPictureView.setProportionFreedom(false);
                tvProportion_4_3.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_4_3_bg.setBackgroundResource(R.drawable.edit_cut_crop_4_3_b);
                mCropPictureView.setProportion(4, 3);
                break;
            case R.id.ll_proportion_9_16:
                mCropPictureView.setProportionFreedom(false);
                tvProportion_9_16.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_9_16_bg.setBackgroundResource(R.drawable.edit_cut_crop_9_16_b);
                mCropPictureView.setProportion(9, 16);
                break;
            case R.id.ll_proportion_16_9:
                mCropPictureView.setProportionFreedom(false);
                tvProportion_16_9.setTextColor(COLOR_CHECKED_DEFAULT);
                tvProportion_16_9_bg.setBackgroundResource(R.drawable.edit_cut_crop_16_9_b);
                mCropPictureView.setProportion(16, 9);
                break;
        }
    }

    private void resetProportionButtonState() {
        tvProportion_freedom.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_1_1.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_2_3.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_3_2.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_3_4.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_4_3.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_9_16.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_16_9.setTextColor(COLOR_UNCHECKED_DEFAULT);
        tvProportion_freedom_bg.setBackgroundResource(R.drawable.edit_cut_crop_freedom_a);
        tvProportion_1_1_bg.setBackgroundResource(R.drawable.edit_cut_crop_1_1_a);
        tvProportion_2_3_bg.setBackgroundResource(R.drawable.edit_cut_crop_2_3_a);
        tvProportion_3_2_bg.setBackgroundResource(R.drawable.edit_cut_crop_3_2_a);
        tvProportion_3_4_bg.setBackgroundResource(R.drawable.edit_cut_crop_3_4_a);
        tvProportion_4_3_bg.setBackgroundResource(R.drawable.edit_cut_crop_4_3_a);
        tvProportion_9_16_bg.setBackgroundResource(R.drawable.edit_cut_crop_9_16_a);
        tvProportion_16_9_bg.setBackgroundResource(R.drawable.edit_cut_crop_16_9_a);
    }
}
