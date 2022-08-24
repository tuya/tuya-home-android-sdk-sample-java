package com.tuya.appsdk.sample.device.mgt.control.dpItem.link.action;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.slider.Slider;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.android.device.enums.TuyaSmartThingMessageType;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.TuyaSmartThingAction;
import com.tuya.smart.sdk.bean.TuyaSmartThingProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kotlin.jvm.JvmOverloads;

/**
 * @author axiong
 * @date 2022/8/23
 * @description :
 */
public class ActionValueItem extends FrameLayout {

    /**
     *  "inputParams": [
     *                                 {
     *                                     "code": "event_value",
     *                                     "typeSpec": {
     *                                         "unit": "￥",
     *                                         "min": 0,
     *                                         "typeDefaultValue": 0,
     *                                         "max": 5,
     *                                         "scale": 0,
     *                                         "step": 1,
     *                                         "type": "value"
     *                                     }
     *                                 },
     *                                 ]
     */

    public ActionValueItem(Context context,
                          AttributeSet attrs,
                          int defStyle,
                          final TuyaSmartThingAction action, ITuyaDevice device) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_action_view, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(action.getCode());

        TextView show = findViewById(R.id.tv_action);
        show.setText("动作包含: \t");

        List<Object>  list = action.getInputParams();
        List<String> inputCodes = new ArrayList<>();
        for(Object object:list){
            JSONObject t = JSONObject.parseObject(JSONObject.toJSONString(object));
            String inputCode = t.getString("code");
            inputCodes.add(inputCode);
            show.setText(inputCode + "\t");
        }

        Button btnDp = findViewById(R.id.btnDp);
        btnDp.setText("确认下发");



        btnDp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.shortToast(context, "请先将动作展示");
                //{
                //  "actionCode": "testAction",
                //  "inputParams": {
                //    "inputParam1":"value1", //具体依照typespec的type定义来，不一定都是string
                //    "inputParam2":"value2"
                //  }
                //}
                //inputParam1 见文首注释inputParams 的code

                JSONObject data = new JSONObject();
                data.put("actionCode", action.getCode());

                JSONObject inputParams = new JSONObject();
                for(String inputCode : inputCodes){
                    //todo value 改成自己实际的值
                    inputParams.put(inputCode, "value");
                }
                data.put("inputParams",inputParams);
                device.publishThingMessageWithType(TuyaSmartThingMessageType.ACTION, data, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {

                    }

                    @Override
                    public void onSuccess() {

                    }
                });
            }
        });

    }

    @JvmOverloads
    public ActionValueItem(@NotNull Context context, @Nullable AttributeSet attrs,  @NotNull TuyaSmartThingAction action, ITuyaDevice device) {
        this(context, attrs, 0, action,device );
    }
    @JvmOverloads
    public ActionValueItem(@NotNull Context context, @NotNull TuyaSmartThingAction action, @NotNull ITuyaDevice device) {
        this(context, null, 0, action, device);
    }
}
