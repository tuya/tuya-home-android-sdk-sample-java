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

package com.tuya.appsdk.sample.home.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tuya.appsdk.sample.home.list.adapter.HomeListActivity;
import com.tuya.appsdk.sample.home.list.adapter.HomeListPageType;
import com.tuya.appsdk.sample.home.newHome.NewHomeActivity;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.appsdk.sample.user.R;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;

/**
 * Home Management Widget
 *
 * @author aiwen <a href="mailto:developer@tuya.com"/>
 * @since 2/19/21 10:04 AM
 */
public class HomeFuncWidget implements View.OnClickListener {

    private TextView mTvNewHome, mTvCurrentHome, mTvCurrentHomeName, mTvHomeList;
    private Context mContext;

    public View render(Context context) {
        mContext = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.home_view_func, null, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mTvNewHome = rootView.findViewById(R.id.tvNewHome);
        mTvCurrentHome = rootView.findViewById(R.id.tvCurrentHome);
        mTvCurrentHomeName = rootView.findViewById(R.id.tvCurrentHomeName);
        mTvHomeList = rootView.findViewById(R.id.tvHomeList);

        mTvNewHome.setOnClickListener(this);
        mTvCurrentHome.setOnClickListener(this);
        mTvHomeList.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvCurrentHome) {// Switch Home
            Intent intent = new Intent(mContext, HomeListActivity.class);
            intent.putExtra("type", HomeListPageType.SWITCH);
            mContext.startActivity(intent);
        } else if (id == R.id.tvHomeList) {// Get Home List And Home Detail
            Intent intent = new Intent(mContext, HomeListActivity.class);
            intent.putExtra("type", HomeListPageType.LIST);
            mContext.startActivity(intent);
        } else if (id == R.id.tvNewHome) {// Create Home
            mContext.startActivity(new Intent(mContext, NewHomeActivity.class));
        }
    }


    public void refresh() {
        long currentHomeId = HomeModel.getCurrentHome(mContext);
        if (currentHomeId != 0L) {
            TuyaHomeSdk.newHomeInstance(currentHomeId).getHomeDetail(new ITuyaHomeResultCallback() {
                @Override
                public void onSuccess(HomeBean bean) {
                    mTvCurrentHomeName.setText(bean.getName());
                }

                @Override
                public void onError(String errorCode, String errorMsg) {

                }
            });

        }

    }
}
