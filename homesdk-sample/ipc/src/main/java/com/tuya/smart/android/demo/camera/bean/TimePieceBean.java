package com.tuya.smart.android.demo.camera.bean;

import androidx.annotation.NonNull;

/**
 * Created by lee on 2017/4/24.
 */

public class TimePieceBean implements Comparable<TimePieceBean> {
    private int startTime;
    private int endTime;
    private int playTime;
    private int prefix;

    public TimePieceBean() {
    }



    public long getStartTimeInMillisecond() {
        return startTime * 1000L;
    }

    public long getEndTimeInMillisecond() {
        return endTime * 1000L;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getPlayTime() {
        return playTime;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public int getPrefix() {
        return prefix;
    }

    public void setPrefix(int prefix) {
        this.prefix = prefix;
    }


    @Override
    public int compareTo(@NonNull TimePieceBean o) {
        //自定义比较方法，如果认为此实体本身大则返回1，否则返回-1
        if(this.endTime >= o.getEndTime()){
            return 1;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "TimePieceBean{" +
                "starttime='" + startTime + '\'' +
                "playTime='" + playTime + '\'' +
                ", endtime='" + endTime + '\'' +
                '}';
    }
}
