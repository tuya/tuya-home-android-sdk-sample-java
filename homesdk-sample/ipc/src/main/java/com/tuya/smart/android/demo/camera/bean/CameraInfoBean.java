package com.tuya.smart.android.demo.camera.bean;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by lee on 2017/5/23.
 */

public class CameraInfoBean {
    private static final String VIDEO_NUM = "video_num";   // 1: 1路码流，2及>2代表多路码流
    private String id;
    private String password;
    private String p2pId;
    private int p2pSpecifiedType;
    private P2pConfig p2pConfig;
    private AudioAttributes audioAttributes;
    private String skill;
    //码流数
    private int videoNum;

    public CameraInfoBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getP2pId() {
        return p2pId;
    }

    public void setP2pId(String p2pId) {
        this.p2pId = p2pId;
    }


    public int getP2pSpecifiedType() {
        return p2pSpecifiedType;
    }

    public void setP2pSpecifiedType(int p2pSpecifiedType) {
        this.p2pSpecifiedType = p2pSpecifiedType;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public int getVideoNum() {
        JSONObject object = JSONObject.parseObject(skill);
        if (null != object) {
            return object.getIntValue(VIDEO_NUM);
        } else {
            return -1;
        }
    }

    public P2pConfig getP2pConfig() {
        return p2pConfig;
    }

    public AudioAttributes getAudioAttributes() {
        return audioAttributes;
    }

    public void setAudioAttributes(AudioAttributes audioAttributes) {
        this.audioAttributes = audioAttributes;
    }

    public void setP2pConfig(P2pConfig p2pConfig) {
        this.p2pConfig = p2pConfig;
    }

    public static class P2pConfig {
        String initStr;
        String p2pKey;
        List<Object> ices;

        public String getInitStr() {
            return initStr;
        }

        public void setInitStr(String initStr) {
            this.initStr = initStr;
        }

        public String getP2pKey() {
            return p2pKey;
        }

        public void setP2pKey(String p2pKey) {
            this.p2pKey = p2pKey;
        }

        public List<Object> getIces() {
            return ices;
        }

        public void setIces(List<Object> ices) {
            this.ices = ices;
        }
    }


    public static class AudioAttributes {
        List<Integer> callMode;
        List<Integer> hardwareCapability;

        public List<Integer> getCallMode() {
            return callMode;
        }

        public void setCallMode(List<Integer> callMode) {
            this.callMode = callMode;
        }

        public List<Integer> getHardwareCapability() {
            return hardwareCapability;
        }

        public void setHardwareCapability(List<Integer> hardwareCapability) {
            this.hardwareCapability = hardwareCapability;
        }
    }

    @Override
    public String toString() {
        return "CameraInfoBean{" +
                "password='" + password + '\'' +
                ", p2pId='" + p2pId + '\'' +
                ", p2pConfig=" + p2pConfig +
                ", audioAttributes=" + audioAttributes +
                '}';
    }
}
