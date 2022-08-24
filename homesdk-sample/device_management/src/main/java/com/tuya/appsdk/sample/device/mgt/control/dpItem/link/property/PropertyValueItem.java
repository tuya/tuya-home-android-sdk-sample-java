package com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.slider.Slider;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.android.device.enums.TuyaSmartThingMessageType;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.TuyaSmartThingProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import kotlin.jvm.JvmOverloads;

/**
 * @author axiong
 * @date 2022/8/23
 * @description :
 */
public class PropertyValueItem extends FrameLayout {

    private static final String TAG = "PropertyValueItem";
    @RequiresApi(api = Build.VERSION_CODES.O)
    public PropertyValueItem(Context context,
                             AttributeSet attrs,
                             int defStyle,
                             Object object,
                             final TuyaSmartThingProperty property, ITuyaDevice device) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_property_integer, this);

        SeekBar slDp = findViewById(R.id.slDp);
        int value = (Integer) object;
        Map<String,Object> map = property.getTypeSpec();
        String unit = (String)map.get("unit");
        int min = (Integer)map.get("min");
        int step = (Integer)map.get("step");
        int max = (Integer)map.get("max");
        Log.i(TAG, "min = " + min + " / max = " + max + " / step = " + step);
        slDp.setMin(min);
        slDp.setMax(max);
        slDp.setProgress(value);


        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(property.getCode());
        //不是只上报类型(ro)，可写 ，还有rw可下发可上报 、wr只下发
        if(!TextUtils.equals(property.getAccessMode(), "ro")){

            slDp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //see DpIntegerItem.java step use
                    if( fromUser && (progress < min || progress > max)){
                        Log.i(TAG,"onProgressChanged progress");
                        return;
                    }
                    JSONObject data = new JSONObject();
                    data.put(property.getCode(), progress);

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

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


        }

    }

    @JvmOverloads
    public PropertyValueItem(@NotNull Context context, @Nullable AttributeSet attrs,  @NotNull Object value,@NotNull TuyaSmartThingProperty property, ITuyaDevice device) {
        this(context, attrs, 0,value, property,device );
    }
    @JvmOverloads
    public PropertyValueItem(@NotNull Context context, @NotNull Object value, @NotNull TuyaSmartThingProperty property, @NotNull ITuyaDevice device) {
        this(context, null, 0,value, property, device);
    }
}
