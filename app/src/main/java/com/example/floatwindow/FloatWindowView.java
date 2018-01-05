package com.example.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by ZhangXinmin on 2018/1/4.
 * Copyright (c) 2018 . All rights reserved.
 * 悬浮窗控件
 */

public final class FloatWindowView extends View {
    private static final String TAG = FloatWindowView.class.getSimpleName();

    private static final float SCALE_SIZE = 0.75f;

    private Context mContext;

    //root view
    private LinearLayout mRootView;

    private ImageView mImageView;

    // is able to move
    private boolean mIsMoveable;

    //the initial position in x direction
    private int mInitialX;

    //the initial position in y direction
    private int mInitialY;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private Display mDisplay;
    private int mScreenWidth;
    private int mScreenHeight;

    private AnimationDrawable mFlyingAnimation;

    public FloatWindowView(Context context) {
        super(context);

        initParamsAndViews(context);
    }

    //init params and views
    private void initParamsAndViews(Context context) {
        mContext = context;

        //init view
        mRootView = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.layout_float_view, null);

        mImageView = (ImageView) mRootView.findViewById(R.id.iv_float_view);

        //init windowmanager
        if (context instanceof Activity) {
            mWindowManager = ((Activity) context).getWindowManager();
        } else {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

        if (mWindowManager == null) {
            LogUtils.logE("windowmanager is null");
            return;
        }
        //get default screen information
        mDisplay = mWindowManager.getDefaultDisplay();
        mLayoutParams = new WindowManager.LayoutParams();
        //get screen size
        final Point outSize = new Point();
        mDisplay.getSize(outSize);
        mScreenWidth = outSize.x;
        mScreenHeight = outSize.y;
        LogUtils.logI("屏幕尺寸..Width:" + mScreenWidth + "..Height:" + mScreenHeight);
        //calculation initial position
        mInitialX = (int) (mScreenWidth * SCALE_SIZE);
        mInitialY = (int) (mScreenHeight * SCALE_SIZE);
        LogUtils.logI("初始位置..X:" + mInitialX + "..Y:" + mInitialY);
        //init initial position
        mLayoutParams.x = mInitialX;
        mLayoutParams.y = mInitialY;
        //set gravity left|top
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        //set flag
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //set window size
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //The desired bitmap format
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //set window type
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;

        mWindowManager.addView(mRootView, mLayoutParams);

    }

    /**
     * stop flying animation
     */
    public void stopFlyingAnim() {
        if (mFlyingAnimation != null) {
            mFlyingAnimation.stop();
        }
    }

    /**
     * close float view
     */
    public void closeFloatView() {
        try {
            stopFlyingAnim();
            mWindowManager.removeViewImmediate(mRootView);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.logE("stopFlyingAnim exception:" + e.getMessage());
        }
    }

    /**
     * 开启动画
     *
     * @hide
     */
    public void startFlyingAnim() {
        if (mRootView.isShown()) {
            mImageView.setImageResource(R.drawable.anim_red_flying);
            if (mFlyingAnimation == null) {
                mFlyingAnimation = (AnimationDrawable) mImageView.getDrawable();
            }
            mFlyingAnimation.start();
        }
    }

}
