package com.tuya.lock.demo.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.tuya.lock.demo.R;


public class DialogUtils {

    public static void showDelete(Context context, DialogInterface.OnClickListener listener) {
        showDelete(context, "是否确认删除", listener);
    }

    public static void showDelete(Context context, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("提示");
        dialog.setMessage(message);
        dialog.setPositiveButton("确认", listener);
        dialog.setNegativeButton("取消", (dialog12, which) -> {
            dialog12.dismiss();
        });
        dialog.show();
    }

    public static void showNumberEdit(Context context,int timeSelect, NumberCallback callback) {
        final int[] select = {0};
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("提示");
        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_number, null);
        dialog.setView(layout);
        NumberPicker numberPicker = layout.findViewById(R.id.number_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);
        numberPicker.setValue(timeSelect);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> select[0] = newVal);
        dialog.setPositiveButton("确认", (dialog1, which) -> {
            if (null != callback) {
                callback.select(select[0]);
            }
        });
        dialog.setNegativeButton("取消", (dialog12, which) -> {
            dialog12.dismiss();
        });
        dialog.show();
    }

    public interface NumberCallback {
        void select(int number);
    }
}