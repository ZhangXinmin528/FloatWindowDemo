package com.example.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private List<String> mDataList;

    private FloatWindowView mFloatWindowView;

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
        mFloatWindowView = new FloatWindowView(mContext);
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
//        if (mFloatWindowView != null) {
//            mFloatWindowView.stopFlyingAnim();
//        }
    }

    @Override
    protected void onDestroy() {
        if (mFloatWindowView != null) {
            mFloatWindowView.closeFloatView();
        }
        super.onDestroy();
    }
}
