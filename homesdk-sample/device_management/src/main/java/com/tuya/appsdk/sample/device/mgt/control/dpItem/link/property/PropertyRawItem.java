package com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.common.utils.HexUtil;
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
public class PropertyRawItem extends FrameLayout {

    public PropertyRawItem(Context context,
                           AttributeSet attrs,
                           int defStyle,
                           Object object,
                           final TuyaSmartThingProperty property, ITuyaDevice device) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_dp_raw_type, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(property.getCode());

        EditText etDp = findViewById(R.id.etDp);
        String value = (String) object;
        etDp.setText(value);

        if (!TextUtils.equals(property.getAccessMode(), "ro")) {
            // Data can be issued by the cloud.
            etDp.setOnEditorActionListener((v, actionId, event) -> {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String rawValue = etDp.getText().toString();

                    if (checkRawValue(rawValue)) { //raw | file
                        JSONObject data = new JSONObject();
                        data.put(property.getCode(), rawValue);

                        device.publishThingMessageWithType(TuyaSmartThingMessageType.PROPERTY, data, new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {

                            }

                            @Override
                            public void onSuccess() {
                                ToastUtil.shortToast(context, "publish success");
                            }
                        });
                    }
                    return true;
                }
                return false;
            });
        }

    }

    @JvmOverloads
    public PropertyRawItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull Object value,@NotNull TuyaSmartThingProperty property, ITuyaDevice device) {
        this(context, attrs, 0, value, property,device );
    }
    @JvmOverloads
    public PropertyRawItem(@NotNull Context context, @NotNull Object value,@NotNull TuyaSmartThingProperty property, @NotNull ITuyaDevice device) {
        this(context, null, 0, value, property, device);
    }
    private Boolean checkRawValue(String rawValue) {
        return HexUtil.checkHexString(rawValue) && rawValue.length() % 2 == 0;
    }
}
