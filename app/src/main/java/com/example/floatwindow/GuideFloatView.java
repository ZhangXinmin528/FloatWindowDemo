package com.example.floatwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by ZhangXinmin on 2017/12/26.
 * Copyright (c) 2017 . All rights reserved.
 * 首页引导悬浮图标
 */

@SuppressLint("ViewConstructor")
public final class GuideFloatView extends View {
    private static final String TAG = GuideFloatView.class.getSimpleName();

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mRootView;//根布局

    private ImageView mIconImageView;

    private int mTag = 0;
    //初始位置
    private int mPositionX;
    private int mPositionY;
    private int mOldOffsetX;
    private int mOldOffsetY;

    private static int mFinalX; //销毁前X
    private static int mFinalY; //销毁前Y

    private AnimationDrawable mFlyingAnimation;

    public GuideFloatView(@NonNull Context context) {
        super(context);
        mContext = context;
        initParamsAndViews();
    }

    //init params
    @SuppressLint("WrongConstant")
    private void initParamsAndViews() {
        // 设置载入view WindowManager参数
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            mWindowManager = activity.getWindowManager();
        } else {
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        mPositionX = mWindowManager.getDefaultDisplay().getWidth() / 2;
        mPositionY = mWindowManager.getDefaultDisplay().getHeight() / 2;
        //init views
//        mRootView = LayoutInflater.from(mContext).inflate(R.layout.layout_guide_float_view, null);
//        mIconImageView = (ImageView) mRootView.findViewById(R.id.iv_guide_float_view);

        mRootView.setBackgroundColor(Color.TRANSPARENT);

        mRootView.setOnTouchListener(mTouchListener);
        mLayoutParams = new WindowManager.LayoutParams();
        if (mFinalX != 0 && mFinalY != 0) {
            mLayoutParams.x = mFinalX;
            mLayoutParams.y = mFinalY;
        } else {//初始位置
            mLayoutParams.x = mPositionX * 3 / 4;
            mLayoutParams.y = mPositionY * 3 / 4;
        }
        //适配小米、魅族等手机需要悬浮框权限的问题
        if (mContext instanceof Activity) {
            //类似dialog，寄托在activity的windows上,activity关闭时需要关闭当前float
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Build.VERSION.SDK_INT > 23) {
                    //在android7.1以上系统需要使用TYPE_PHONE类型 配合运行时权限
                    mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                } else {
                    mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                }
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
        }
        /**
         *这里的flags也很关键
         *代码实际是wmParams.flags |= FLAG_NOT_FOCUSABLE;
         *40的由来是wmParams的默认属性（32）+ FLAG_NOT_FOCUSABLE（8）
         */
        mLayoutParams.flags = 40;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.format = -3; // 透明
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
            Log.e(TAG, "stopFlyingAnim exception:" + e.getMessage());
        }
    }

    /**
     * 事件分发
     */
    private OnTouchListener mTouchListener = new OnTouchListener() {
        float lastX, lastY;
        int paramX, paramY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();

            float x = event.getRawX();
            float y = event.getRawY();

            if (mTag == 0) {
                mOldOffsetX = mLayoutParams.x; // 偏移量
                mOldOffsetY = mLayoutParams.y; // 偏移量
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    motionActionDownEvent(x, y);
                    break;

                case MotionEvent.ACTION_MOVE:
                    motionActionMoveEvent(x, y);
                    break;

                case MotionEvent.ACTION_UP:
                    motionActionUpEvent(x, y);
                    break;

                default:
                    break;
            }

            return true;
        }

        /**
         * For action down
         * @param x
         * @param y
         */
        private void motionActionDownEvent(float x, float y) {
            lastX = x;
            lastY = y;
            paramX = mLayoutParams.x;
            paramY = mLayoutParams.y;
        }

        /**
         * For action move
         * @param x
         * @param y
         */
        private void motionActionMoveEvent(float x, float y) {
            int dx = (int) (x - lastX);
            int dy = (int) (y - lastY);
            mLayoutParams.x = paramX + dx;
            mLayoutParams.y = paramY + dy;
            mTag = 1;

            // 更新悬浮窗位置
            mWindowManager.updateViewLayout(mRootView, mLayoutParams);
        }

        /**
         * For action move
         * @param x
         * @param y
         */
        private void motionActionUpEvent(float x, float y) {
            int newOffsetX = mLayoutParams.x;
            int newOffsetY = mLayoutParams.y;
            if (Math.abs(mOldOffsetX - newOffsetX) <= 5
                    && Math.abs(mOldOffsetY - newOffsetY) <= 5) {
                if (Math.abs(mOldOffsetX) > mPositionX) {
                    if (mOldOffsetX > 0) {
                        mOldOffsetX = mPositionX;
                    } else {
                        mOldOffsetX = -mPositionX;
                    }
                }

                if (Math.abs(mOldOffsetY) > mPositionY) {
                    if (mOldOffsetY > 0) {
                        mOldOffsetY = mPositionY;
                    } else {
                        mOldOffsetY = -mPositionY;
                    }
                }
                //跳转网页

            } else {
                //吸边
                mTag = 0;
                if (newOffsetX >= 0) { //吸右边
                    mLayoutParams.x = mPositionX;
                    mLayoutParams.y = newOffsetY;
                } else {
                    mLayoutParams.x = -mPositionX;
                    mLayoutParams.y = newOffsetY;
                }
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);
                mFinalX = mLayoutParams.x;
                mFinalY = mLayoutParams.y;
            }
        }
    };

    /**
     * 开启动画
     *
     * @hide
     */
    private void startRedFlyingAnim() {
        if (mRootView.isShown()) {
            mIconImageView.setImageResource(R.drawable.anim_red_flying);
            mFlyingAnimation = (AnimationDrawable) mIconImageView.getDrawable();
            mFlyingAnimation.start();
        }
    }

}
