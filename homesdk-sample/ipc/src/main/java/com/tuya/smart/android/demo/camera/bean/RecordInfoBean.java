package com.tuya.smart.android.demo.camera.bean;

import java.util.List;

/**
 * Created by huangdaju3 on 2018/6/7.
 */

public class RecordInfoBean {
    private int count;
    private List<TimePieceBean> items;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<TimePieceBean> getItems() {
        return items;
    }

    public void setItems(List<TimePieceBean> items) {
        this.items = items;
    }
}
