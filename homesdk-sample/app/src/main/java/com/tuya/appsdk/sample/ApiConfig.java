package com.tuya.appsdk.sample;


/**
 * Created by mikeshou on 15/6/17.
 */
public class ApiConfig {
    /**
     * 环境
     */
    public enum EnvConfig {
        ONLINE("online"), PREVIEW("preview"), DAILY("daily");

        EnvConfig(String value) {
            this.value = value;
        }
        private String value;
        public String getValue() {
            return value;
        }
        public void setValue(String name) {
            this.value = name;
        }

        public static EnvConfig fromValue(String env) {
            for (EnvConfig type : EnvConfig.values()) {
                if (type.getValue().equals(env)) {
                    return type;
                }
            }
            return null;
        }
    }


    /**
     * 环境
     */
    private EnvConfig mEnv;

    public ApiConfig(EnvConfig env) {
        this.mEnv = env;
    }

    public EnvConfig getEnv() {
        return mEnv;
    }
}
