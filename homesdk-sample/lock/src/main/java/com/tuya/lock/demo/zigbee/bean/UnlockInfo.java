package com.tuya.lock.demo.zigbee.bean;


import com.thingclips.smart.optimus.lock.api.zigbee.response.OpModeBean;

public class UnlockInfo {
    public int type;//0 标题 1 内容
    public String name;//标题文案
    public String dpCode;//标题文案
    public int count;//标题 子列表内容
    public OpModeBean infoBean;
}