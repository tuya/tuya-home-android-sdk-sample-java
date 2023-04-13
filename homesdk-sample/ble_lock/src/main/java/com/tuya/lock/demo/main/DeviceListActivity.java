package com.tuya.lock.demo.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.adapter.DeviceListAdapter;
import com.thingclips.smart.android.ble.builder.BleConnectBuilder;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.api.IThingHome;
import com.thingclips.smart.home.sdk.bean.HomeBean;
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.resource.HomeModel;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {

    public DeviceListAdapter adapter;
    private IThingDevice IThingDevice;
    private IThingHome IThingHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvList = findViewById(R.id.rvList);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new DeviceListAdapter();
        rvList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != IThingDevice) {
            IThingDevice.onDestroy();
        }
        if (null != IThingHome) {
            IThingHome.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        long homeId = HomeModel.getCurrentHome(this);
        if (null == IThingHome) {
            IThingHome = ThingHomeSdk.newHomeInstance(homeId);
        }
        IThingHome.getHomeDetail(new IThingHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                Log.d("HomeBean", "=====");
                List<BleConnectBuilder> builderList = new ArrayList<>();
                ArrayList<DeviceBean> lockList = new ArrayList<>();
                for (DeviceBean deviceBean : homeBean.getDeviceList()) {
                    Log.d("HomeBean", "devId = " + deviceBean.getDevId() + " / name = " + deviceBean.getName());
                    if (deviceBean.getProductBean().getCategory().contains("ms")) {
                        lockList.add(deviceBean);
                        if (null == IThingDevice) {
                            IThingDevice = ThingHomeSdk.newDeviceInstance(deviceBean.devId);
                        }
                        IThingDevice.registerDevListener(iDevListener);
                        if (deviceBean.isBluetooth()) {
                            BleConnectBuilder builder = new BleConnectBuilder();
                            builder.setDevId(deviceBean.devId);
                            builderList.add(builder);
                        }
//                        if (deviceBean.getIsOnline()) {
//                            onSyncBatchData(deviceBean.devId);
//                        }
                    }
                }

                if (builderList.size() > 0) {
                    ThingHomeSdk.getBleManager().connectBleDevice(builderList);
                }

                if (null != adapter) {
                    adapter.setData(lockList);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(DeviceListActivity.this,
                        "Activate error-->" + errorMsg,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private final IDevListener iDevListener = new IDevListener() {
        @Override
        public void onDpUpdate(String devId, String dpStr) {

        }

        @Override
        public void onRemoved(String devId) {

        }

        @Override
        public void onStatusChanged(String devId, boolean online) {
            if (adapter != null && adapter.data != null && adapter.data.size() > 0) {
                for (DeviceBean item : adapter.data) {
                    item.setIsOnline(online);
                    adapter.notifyDataSetChanged();
                }
            }
//            if (online) {
//                onSyncBatchData(devId);
//            }
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {

        }
    };

//    public void onSyncBatchData(String devId) {
//        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
//        tuyaLockManager.getBleLockV2(devId).publishSyncBatchData();
//    }
}