package com.tuya.appsdk.sample;

import static com.tuya.smart.android.network.TuyaSmartNetWork.DOMAIN_AY;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.base.provider.ApiUrlProvider;
import com.tuya.smart.android.user.bean.Domain;

/**
 * Created by lee on 16/5/7.
 */
public class TuyaApiUrlProvider extends ApiUrlProvider {
    //a1-cn.wgine.com  a4-abb-cn.wgine.com:5114
    private static final String PRIVIEW_DOMAIN = "{\n" +
            "  \"AY\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-cn.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-cn.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.cn.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.cn.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.cn.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-cn.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-cn.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-cn.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-cn.wgine.com/api.json\"\n" +
            "  },\n" +
            "  \"AZ\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-us.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-us.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.us.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.us.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.us.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-us.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-us.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-us.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-us.wgine.com/api.json\"\n" +
            "  },\n" +
            "  \"EU\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-eu.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-eu.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.eu.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.eu.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.eu.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-eu.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-eu.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-eu.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-eu.wgine.com/api.json\"\n" +
            "  },\n" +
            "  \"IN\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-ind.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-ind.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.ind.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.ind.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.ind.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-ind.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-ind.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-ind.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-ind.wgine.com/api.json\"\n" +
            "  }\n" +
            "}";
    private static final String DAILY_DOMAIN = "{\n" +
            "  \"AY\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-daily.tuya-inc.cn/api.json\",\n" +
            "    \"logUrl\": \"https://a1-daily.tuya-inc.cn/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.daily.tuya-inc.cn\",\n" +
            "    \"gwApiUrl\": \"http://a.daily.tuya-inc.cn/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.daily.tuya-inc.cn\"\n" +
            "  },\n" +
            "  \"AZ\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-us.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-us.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.daily.tuya-inc.cn\",\n" +
            "    \"gwApiUrl\": \"http://a.daily.tuya-inc.cn/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.daily.tuya-inc.cn\"\n" +
            "  },\n" +
            "  \"EU\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-eu.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-eu.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.daily.tuya-inc.cn\",\n" +
            "    \"gwApiUrl\": \"http://a.daily.tuya-inc.cn/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.daily.tuya-inc.cn\"\n" +
            "  }\n" +
            "}";

    public static final String PRIVIEW_ABB_DOMAIN = "{\n" +
            "  \"AY\": {\n" +
            "    \"mobileApiUrl\": \"https://a4-abb-cn.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-cn.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.cn.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.cn.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.cn.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-cn.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-cn.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-cn.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-cn.wgine.com/api.json\"\n" +
            "  },\n" +
            "  \"AZ\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-us.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-us.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.us.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.us.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.us.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-us.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-us.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-us.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-us.wgine.com/api.json\"\n" +
            "  },\n" +
            "  \"EU\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-eu.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-eu.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.eu.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.eu.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.eu.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-eu.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-eu.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-eu.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-eu.wgine.com/api.json\"\n" +
            "  },\n" +
            "  \"IN\": {\n" +
            "    \"mobileApiUrl\": \"https://a1-ind.wgine.com/api.json\",\n" +
            "    \"logUrl\": \"https://a1-ind.wgine.com/log.json\",\n" +
            "    \"mobileMqttUrl\": \"mq.mb.ind.wgine.com\",\n" +
            "    \"gwApiUrl\": \"http://a.gw.ind.wgine.com/gw.json\",\n" +
            "    \"gwMqttUrl\": \"mq.gw.ind.wgine.com\",\n" +
            "    \"mobileQuicUrl\": \"https://u1-ind.wgine.com/api.json\",\n" +
            "    \"mqttQuicUrl\": \"q1-ind.wgine.com\",\n" +
            "    \"aispeechHttpsUrl\": \"https://aispeech-ind.wgine.com/api.json\",\n" +
            "    \"aispeechQuicUrl\": \"https://i1-ind.wgine.com/api.json\"\n" +
            "  }\n" +
            "}";
    private static final String TAG = "TuyaApiUrlProvider";
    private ApiConfig.EnvConfig mEnv;

    public TuyaApiUrlProvider(Context cxt, ApiConfig.EnvConfig env) {
        super(cxt);
        mEnv = env;
        switch (mEnv) {
            case ONLINE:
                break;
            case DAILY:
                setDomain(cxt, DAILY_DOMAIN);
                break;
            default:
                setDomain(cxt, PRIVIEW_DOMAIN);
                break;
        }

    }

    public void setDomain(Context context, String domain) {
        String mRegion = getDefaultRegion(context);
        JSONObject previewDomains = JSONObject.parseObject(domain);
        Domain mDefaultDomain = previewDomains.getObject(mRegion, Domain.class);
        setDefaultDomain(mDefaultDomain);
        setDomainJson(domain);
    }

    @Override
    protected String getOldApiUrl() {
        switch (mEnv) {
            case ONLINE:
                return super.getOldApiUrl();
            case DAILY:
            default:
                return mRegion == DOMAIN_AY ? "https://a1.mb.cn.wgine.com/api.json" : "https://a1.mb.us.wgine.com/api.json";
        }
    }

    @Override
    protected String[] getOldMqttUrl(String region) {
        if (mEnv == ApiConfig.EnvConfig.ONLINE) {
            return super.getOldMqttUrl(region);
        } else {
            return mRegion == DOMAIN_AY ? new String[]{"mq.mb.cn.wgine.com", "mq.mb.cn.wgine.com"} : new String[]{"mq.mb.us.wgine.com", "mq.mb.us.wgine.com"};
        }
    }

    @Override
    protected String getOldGwApiUrl() {
        if (mEnv == ApiConfig.EnvConfig.ONLINE) {
            return super.getOldGwApiUrl();
        } else {
            return mRegion == DOMAIN_AY ? "http://a.gw.cn.wgine.com/gw.json" : "http://a.gw.us.wgine.com/gw.json";
        }
    }

    @Override
    protected String[] getOldGwMqttUrl() {
        if (mEnv == ApiConfig.EnvConfig.ONLINE) {
            return super.getOldGwMqttUrl();
        } else {
            return mRegion == DOMAIN_AY ? new String[]{"mq.gw.cn.wgine.com", "mq.gw.cn.wgine.com"} : new String[]{"mq.gw.us.wgine.com", "mq.gw.us.wgine.com"};
        }
    }

}
