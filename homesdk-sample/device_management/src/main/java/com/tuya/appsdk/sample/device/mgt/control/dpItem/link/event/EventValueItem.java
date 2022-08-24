package com.tuya.appsdk.sample.device.mgt.control.dpItem.link.event;

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
import com.tuya.smart.sdk.bean.TuyaSmartThingEvent;
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
public class EventValueItem extends FrameLayout {
    public EventValueItem(Context context,
                             AttributeSet attrs,
                             int defStyle,
                          @NotNull TuyaSmartThingEvent event, ITuyaDevice device) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_action_view, this);

    }

    @JvmOverloads
    public EventValueItem(@NotNull Context context, @Nullable AttributeSet attrs,  @NotNull TuyaSmartThingEvent event, ITuyaDevice device) {
        this(context, attrs, 0, event,device );
    }
    @JvmOverloads
    public EventValueItem(@NotNull Context context, @NotNull TuyaSmartThingEvent event, @NotNull ITuyaDevice device) {
        this(context, null, 0, event, device);
    }
}
