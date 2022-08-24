package com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.android.device.bean.EnumSchemaBean;
import com.tuya.smart.android.device.enums.TuyaSmartThingMessageType;
import com.tuya.smart.home.sdk.utils.SchemaMapper;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.TuyaSmartThingProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.collections.CollectionsKt;
import kotlin.jvm.JvmOverloads;

/**
 * @author axiong
 * @date 2022/8/23
 * @description :
 */
public class PropertyEnumItem extends FrameLayout {

    public PropertyEnumItem(Context context,
                            AttributeSet attrs,
                            int defStyle,
                            Object object,
                            final TuyaSmartThingProperty property, ITuyaDevice device) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_dp_enum, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(property.getCode());

        Button btnDp = findViewById(R.id.btnDp);
        String value = "" ;
        if(object == null || TextUtils.isEmpty((String)object)){
            Map<String,Object>  map = property.getTypeSpec();
            Object data = map.get("typeDefaultValue");
            if(data != null){
                value = (String)data;
            }
        }else{
            value = (String)object;
        }

        btnDp.setText(value);

        if (!TextUtils.equals(property.getAccessMode(), "ro")) {
            // Data can be issued by the cloud.
            ListPopupWindow listPopupWindow = new ListPopupWindow(context, null, R.attr.listPopupWindowStyle);
            listPopupWindow.setAnchorView(btnDp);

            EnumSchemaBean enumSchemaBean = SchemaMapper.toEnumSchema(JSONObject.toJSONString(property.getTypeSpec()));
            Set set = enumSchemaBean.range;
            List items = CollectionsKt.toList(set);
            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.device_mgt_item_dp_enum_popup_item, items);
            listPopupWindow.setAdapter(adapter);
            listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {

                JSONObject data = new JSONObject();

                data.put(property.getCode(), items.get(position));

                device.publishThingMessageWithType(TuyaSmartThingMessageType.PROPERTY, data, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {

                    }

                    @Override
                    public void onSuccess() {
                        btnDp.setText(
                                (CharSequence) items.get(position));
                        ToastUtil.shortToast(context, "publish success");
                    }
                });
                listPopupWindow.dismiss();


            });
            btnDp.setOnClickListener(v -> {
                listPopupWindow.show();
            });
        }


    }

    @JvmOverloads
    public PropertyEnumItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull Object value, @NotNull TuyaSmartThingProperty property, ITuyaDevice device) {
        this(context, attrs, 0, value,property,device );
    }
    @JvmOverloads
    public PropertyEnumItem(@NotNull Context context, @NotNull Object value, @NotNull TuyaSmartThingProperty property, @NotNull ITuyaDevice device) {
        this(context, null, 0, value, property, device);
    }
}
