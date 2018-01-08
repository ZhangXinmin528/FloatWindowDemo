package com.example.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private List<String> mDataList;

    private InnerFloatWindowView mInnerFloatWindowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initParamsAndValues();

        initViews();

        initTestData();

    }

    private void initParamsAndValues() {
        mContext = this;

        mDataList = new ArrayList<>();
        mAdapter = new RecyclerAdapter(mDataList);
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        mInnerFloatWindowView = new InnerFloatWindowView(mContext);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                final String content = (String) adapter.getData().get(position);
                //弹窗：AlertDialog
                new AlertDialog.Builder(mContext)
                        .setMessage(content)
                        .show();

//                mInnerFloatWindowView = new InnerFloatWindowView(mContext);
                //PopupWindow
//                showPopupWindow(content);
            }
        });
    }

    private void initTestData() {
        for (int i = 0; i < 20; i++) {
            mDataList.add("当前时间：" + LogUtils.getCurrentTime());
        }

        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (mInnerFloatWindowView != null) {
//            mInnerFloatWindowView.stopFlyingAnim();
//        }
    }

    @Override
    protected void onDestroy() {
        if (mInnerFloatWindowView != null) {
            mInnerFloatWindowView.closeFloatView();
        }
        super.onDestroy();
    }

    /**
     * PopupWindow
     *
     * @param content
     */
    private void showPopupWindow(String content) {
        PopupWindow popupWindow = new PopupWindow(mContext);
        View layout = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_popupwindow, null);
        TextView textView = layout.findViewById(R.id.tv_content);
        if (!TextUtils.isEmpty(content)) {
            textView.setText(content);
        }
        popupWindow.setContentView(layout);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.showAtLocation(mRecyclerView, Gravity.CENTER, 0, 0);

    }
}
