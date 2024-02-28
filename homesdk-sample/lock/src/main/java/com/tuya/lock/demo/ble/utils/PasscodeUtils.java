package com.tuya.lock.demo.ble.utils;

import java.security.SecureRandom;

public class PasscodeUtils {

    /**
     * @param digits 位数
     * @return 随机生成密码
     */
    public static String getRandom(int digits) {
        StringBuilder randomString = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < digits; i++) {
            randomString.append(random.nextInt(9));
        }
        return randomString.toString();
    }
}