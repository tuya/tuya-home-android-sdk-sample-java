/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tuya.appsdk.sample.home.list.adapter;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.tuya.appsdk.sample.user.R;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;

import java.util.ArrayList;
import java.util.List;


/**
 * Home List Example
 *
 * @author chaunfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 10:02 AM
 */
public class HomeListActivity extends AppCompatActivity {

    Toolbar mToolbar;
    HomeListAdapter adapter;
    RecyclerView rvList;
    MaterialToolbar topAppBar;
    public int type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_list);

        initView();
        initData();

    }

    private void initView() {
        mToolbar = findViewById(R.id.topAppBar);
        rvList = findViewById(R.id.rvList);
        topAppBar = findViewById(R.id.topAppBar);
        type = HomeListPageType.LIST;
    }

    private void initData() {
        mToolbar.setTitle((type == HomeListPageType.SWITCH) ? R.string.home_switch_home : R.string.home_home_list);

        mToolbar.setNavigationOnClickListener(v -> finish());

        // Get this page type
        type = getIntent().getIntExtra("type", HomeListPageType.LIST);

        // Set title
        topAppBar.setTitle(getString((type == HomeListPageType.SWITCH) ?
                R.string.home_switch_home :
                R.string.home_home_list));

        // Set list
        adapter = new HomeListAdapter(type);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvList.setAdapter(adapter);


    }

    @Override
    public void onResume() {
        super.onResume();

        // Query home list from server
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homeBeans) {

                adapter.data = (ArrayList<HomeBean>) homeBeans;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String error) {

            }
        });


    }


}
