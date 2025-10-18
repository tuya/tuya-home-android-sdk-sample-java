package com.tuya.lock.demo.ble.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.tuya.lock.demo.R;
import com.tuya.appsdk.sample.resource.HomeModel;

public class LockFuncWidget {

    private Context mContext;

    public final View render(Context context) {
        mContext = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.lock_view_func_layout, null, false);
        this.initView(rootView);
        return rootView;
    }

    private void initView(final View rootView) {
        View lockList = rootView.findViewById(R.id.lockList);
        lockList.setOnClickListener(view -> {
            if ((HomeModel.getCurrentHome(rootView.getContext()) == 0)) {
                Toast.makeText(
                        rootView.getContext(),
                        rootView.getContext().getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Intent intent = new Intent(mContext, DeviceListActivity.class);
                mContext.startActivity(intent);
            }
        });
    }
}