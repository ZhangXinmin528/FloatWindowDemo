package com.example.floatwindow;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by ZhangXinmin on 2018/1/4.
 * Copyright (c) 2018 . All rights reserved.
 * 适配器
 */

public class RecyclerAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public RecyclerAdapter(@Nullable List<String> data) {
        super(android.R.layout.simple_list_item_1, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(android.R.id.text1, item);
    }
}
