package com.example.floatwindow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by ZhangXinmin on 2018/1/4.
 * Copyright (c) 2018 . All rights reserved.
 * 悬浮窗控件
 */

public final class FloatWindowView extends View implements
        View.OnTouchListener {

    private static final float SCALE_SIZE = 0.75f;

    //bounce Animator duration
    private static final long BOUNCE_DURATION = 300;

    private Context mContext;

    //root view
    private View mRootView;

    private ImageView mImageView;

    //ACTION_DOWN, the position x coordinate parent view
    private float lastX;
    //ACTION_DOWN, the position y coordinate parent view
    private float lastY;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private int mScreenWidth;
    private int mScreenHeight;

    private ValueAnimator mAnimator;
    private BounceInterpolator mBounceInterpolator;

    public FloatWindowView(Context context) {
        super(context);

        initParamsAndViews(context);
    }

    //init params and views
    @SuppressLint({"ClickableViewAccessibility", "RtlHardcoded"})
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
        final Display mDisplay = mWindowManager.getDefaultDisplay();
        mLayoutParams = new WindowManager.LayoutParams();
        //get screen size
        final Point mOutSize = new Point();
        mDisplay.getSize(mOutSize);
        mScreenWidth = mOutSize.x;
        mScreenHeight = mOutSize.y;
        LogUtils.logI("屏幕尺寸..Width:" + mScreenWidth + "..Height:" + mScreenHeight);
        //calculation initial position
        final int mInitialX = (int) (mScreenWidth * SCALE_SIZE);
        final int mInitialY = (int) (mScreenHeight * SCALE_SIZE);
        LogUtils.logI("初始位置..X:" + mInitialX + "..Y:" + mInitialY);
        //init initial position
        mLayoutParams.x = mInitialX;
        mLayoutParams.y = mInitialY;
        //set gravity left|top
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        //set flag
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //set window size
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //The desired bitmap format
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //set window type
        /*if (mContext instanceof Activity) {
            //类似dialog，寄托在activity的windows上,activity关闭时需要关闭当前float
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;//2005
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;//2002
            }

        }*/
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        mBounceInterpolator = new BounceInterpolator();
        //on touch
        mRootView.setOnTouchListener(this);
        mWindowManager.addView(mRootView, mLayoutParams);

    }

    /**
     * close float view
     */
    public void closeFloatView() {
        try {
            cancelBounceAnimator();
            mWindowManager.removeViewImmediate(mRootView);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.logE("stopFlyingAnim exception:" + e.getMessage());
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                //cancle bounce animator
                cancelBounceAnimator();
                break;
            case MotionEvent.ACTION_MOVE:
                final float changeX = event.getRawX() - lastX;
                final float changeY = event.getRawY() - getStatusBarHeight() - lastY;
                mLayoutParams.x = (int) changeX;
                mLayoutParams.y = (int) changeY;
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                int startX = mLayoutParams.x;
                LogUtils.logIWithTime("ACTION_UP：" + startX);
                int endX = (startX * 2 + v.getWidth() > mScreenWidth ?
                        mScreenWidth - v.getWidth()
                        :
                        0);
                mAnimator = ObjectAnimator.ofInt(startX, endX);
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mLayoutParams.x = (int) animation.getAnimatedValue();
                        mWindowManager.updateViewLayout(mRootView, mLayoutParams);
                    }
                });
                //start bounce animator
                startBounceAnimator();
                break;
            default:

                break;
        }
        return true;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     * @hide
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 开始Bounce动画
     *
     * @hide
     */
    private void startBounceAnimator() {
        mAnimator.setInterpolator(mBounceInterpolator);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator.removeAllUpdateListeners();
                mAnimator.removeAllListeners();
                mAnimator = null;
            }
        });
        mAnimator.setDuration(BOUNCE_DURATION).start();
    }

    /**
     * 关闭Bounce动画
     *
     * @hide
     */
    private void cancelBounceAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

}
