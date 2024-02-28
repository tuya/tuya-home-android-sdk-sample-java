package com.tuya.lock.demo.ble.utils;

public class JSONFormat {

    public static String format(String mJson) {
        StringBuilder source = new StringBuilder(mJson);
        if (mJson.equals("")) {
            return null;
        }
        int offset = 0;//目标字符串插入空格偏移量
        int bOffset = 0;//空格偏移量
        for (int i = 0; i < mJson.length(); i++) {
            char charAt = mJson.charAt(i);
            if (charAt == '{' || charAt == '[') {
                bOffset += 4;
                source.insert(i + offset + 1, "\n" + generateBlank(bOffset));
                offset += (bOffset + 1);
            } else if (charAt == ',') {
                source.insert(i + offset + 1, "\n" + generateBlank(bOffset));
                offset += (bOffset + 1);
            } else if (charAt == '}' || charAt == ']') {
                bOffset -= 4;
                source.insert(i + offset, "\n" + generateBlank(bOffset));
                offset += (bOffset + 1);
            }
        }
        return source.toString();
    }

    private static String generateBlank(int num) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}