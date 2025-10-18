package com.tuya.lock.demo.zigbee.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.android.device.bean.BoolSchemaBean;
import com.thingclips.smart.android.device.bean.EnumSchemaBean;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.android.device.enums.DataTypeEnum;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ZigBeeDatePoint;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.zigbee.dpItem.DpBooleanItem;
import com.tuya.lock.demo.zigbee.dpItem.DpEnumItem;
import com.tuya.lock.demo.zigbee.utils.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 门锁设置
 */
public class DpSettingActivity extends AppCompatActivity {


    private IThingDevice ITuyaDevice;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, DpSettingActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zigbee_dp_setting);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);

        ITuyaDevice = ThingHomeSdk.newDeviceInstance(mDevId);

        LinearLayout dpMain = findViewById(R.id.demo_dp_main);

        Map<String, SchemaBean> map = ThingHomeSdk.getDataInstance().getSchema(mDevId);
        DeviceBean deviceBean = ThingOSDevice.getDeviceBean(mDevId);
        Collection<SchemaBean> schemaBeans = map.values();
        for (SchemaBean bean : schemaBeans) {
            Object value = deviceBean.getDps().get(bean.getId());
            if (!getSettingDpList().contains(bean.code)) {
                continue;
            }

            if (bean.type.equals(DataTypeEnum.OBJ.getType())) {
                // obj
                switch (bean.getSchemaType()) {
                    case BoolSchemaBean.type:
                        DpBooleanItem dpBooleanItem = new DpBooleanItem(
                                this,
                                bean,
                                (Boolean) value,
                                ITuyaDevice);

                        dpMain.addView(dpBooleanItem);
                        break;
                    case EnumSchemaBean.type:
                        DpEnumItem dpEnumItem = new DpEnumItem(
                                this,
                                bean,
                                String.valueOf(value),
                                ITuyaDevice);

                        dpMain.addView(dpEnumItem);
                        break;
                }
            }
        }

    }

    private List<String> getSettingDpList() {
        List<String> list =new ArrayList<>();
        list.add(ZigBeeDatePoint.AUTO_LOCK_TIME);
        list.add(ZigBeeDatePoint.AUTOMATIC_LOCK);
        list.add(ZigBeeDatePoint.DOORBELL_VOLUME);
        list.add(ZigBeeDatePoint.KEY_TONE);
        list.add(ZigBeeDatePoint.BEEP_VOLUME);
        list.add(ZigBeeDatePoint.LANGUAGE);
        list.add(ZigBeeDatePoint.MOTOR_TORQUE);
        list.add(ZigBeeDatePoint.LOCK_MOTOR_DIRECTION);
        list.add(ZigBeeDatePoint.SPECIAL_FUNCTION);
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ITuyaDevice.unRegisterDevListener();
        ITuyaDevice.onDestroy();
    }
}