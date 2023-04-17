package com.tuya.lock.demo.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class CopyLinkTextHelper {

    private static CopyLinkTextHelper instance = null;
    private static ClipboardManager manager;

    private CopyLinkTextHelper(Context context) {
        //获取剪贴板管理器：
        manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public synchronized static CopyLinkTextHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CopyLinkTextHelper(context);
        }
        return instance;
    }

    /**
     * @param text 复制文字到剪切板
     */
    public void CopyText(String text) {
        // 创建能够存入剪贴板的ClipData对象
        //‘Label’这是任意文字标签
        ClipData mClipData = ClipData.newPlainText("Label", text);
        //将ClipData数据复制到剪贴板：
        manager.setPrimaryClip(mClipData);
    }
}