package com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.android.device.enums.TuyaSmartThingMessageType;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.TuyaSmartThingProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import kotlin.jvm.JvmOverloads;

/**
 * @author axiong
 * @date 2022/8/23
 * @description :
 */
public class PropertyBooleanItem extends FrameLayout {

    @SuppressLint("ClickableViewAccessibility")
    public PropertyBooleanItem(Context context,
                               AttributeSet attrs,
                               int defStyle,
                               Object object,
                               final TuyaSmartThingProperty property, ITuyaDevice device) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_dp_char_type, this);

        FrameLayout.inflate(context, R.layout.device_mgt_item_dp_boolean, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(property.getCode());

        Switch swDp = findViewById(R.id.swDp);
        boolean value = false;
        if(object instanceof Boolean){
            value= (Boolean) object;
        }
        swDp.setChecked(value);

        if(!TextUtils.equals(property.getAccessMode(), "ro")) {
            swDp.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    JSONObject data = new JSONObject();
                    Boolean isChecked = !swDp.isChecked();
                    data.put(property.getCode(), isChecked);
                    device.publishThingMessageWithType(TuyaSmartThingMessageType.PROPERTY, data, new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {

                        }

                        @Override
                        public void onSuccess() {
                            ToastUtil.shortToast(context, "publish success");
                            swDp.setChecked(isChecked);
                        }
                    });

                }
                return true;
            });
        }


        }

    @JvmOverloads
    public PropertyBooleanItem(@NotNull Context context, @Nullable AttributeSet attrs,  @NotNull Object value,@NotNull TuyaSmartThingProperty property, ITuyaDevice device) {
        this(context, attrs, 0, value ,property,device );
    }
    @JvmOverloads
    public PropertyBooleanItem(@NotNull Context context, @NotNull Object value, @NotNull TuyaSmartThingProperty property, @NotNull ITuyaDevice device) {
        this(context, null, 0,value, property, device);
    }
}
