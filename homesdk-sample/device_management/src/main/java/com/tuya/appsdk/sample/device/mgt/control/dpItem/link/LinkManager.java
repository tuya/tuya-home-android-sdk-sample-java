package com.tuya.appsdk.sample.device.mgt.control.dpItem.link;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.tuya.appsdk.sample.device.mgt.control.dpItem.link.action.ActionValueItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.link.event.EventValueItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property.PropertyBooleanItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property.PropertyEnumItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property.PropertyRawItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.link.property.PropertyValueItem;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.TuyaSmartThingAction;
import com.tuya.smart.sdk.bean.TuyaSmartThingEvent;
import com.tuya.smart.sdk.bean.TuyaSmartThingProperty;

import java.util.List;
import java.util.Map;

/**
 * @author axiong
 * @date 2022/8/23
 * @description :
 */
public class LinkManager {
    public static LinkManager getInstance(){
        return SingleHolder.INSTANCE;
    }
    private LinkManager(){
    }

    public View createPropertyView(Context context, Object value, TuyaSmartThingProperty property, ITuyaDevice device){
        Log.i("LinkManager","value = " + value);
        Map<String, Object> typeSpec =  property.getTypeSpec();
        String type = (String) typeSpec.get("type");
        View view = null;
        switch (type){
            case "value":
                view = new PropertyValueItem(context, value, property, device);
                break;
            case "bool":
                view = new PropertyBooleanItem(context, value,property, device);
                break;
            case "raw":
                view = new PropertyRawItem(context, value, property, device);
                break;
            case "enum":
                view = new PropertyEnumItem(context, value, property, device);
                break;
            case "string":
            case "array":
            case "struct":
                //同理遍历
                break;


        }
        return view;
    }



    public View createActionView(Context context, TuyaSmartThingAction action, ITuyaDevice device){
        return new ActionValueItem(context, action , device);
    }

    public View createEventView(Context context, TuyaSmartThingEvent event, ITuyaDevice device){
        return new EventValueItem(context, event, device);
    }

    private static  class SingleHolder{
        static final LinkManager INSTANCE = new LinkManager();
    }
}
