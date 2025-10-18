package com.tuya.lock.demo.ble.bean;


import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockInfoBean;

public class UnlockInfo {
    public int type;//0 标题 1 内容
    public String name;//标题文案
    public String dpCode;//标题文案
    public int count;//标题 子列表内容
    public UnlockInfoBean infoBean;
}