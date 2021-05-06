/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NO
 */

package com.tuya.appsdk.sample.user.info;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.Toolbar;

import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.appsdk.sample.user.main.UserFuncActivity;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.enums.TempUnitEnum;

import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

/**
 * User Info Example
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/9 10:55 AM
 */
public class UserInfoActivity extends AppCompatActivity {

    public String lat;
    public String lon;

    List<String> items = new ArrayList<>();
    Button tempUnit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_info);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        User user = TuyaHomeSdk.getUserInstance().getUser();

        TextView tvName = findViewById(R.id.tvName);
        tvName.setText(user.getNickName());
        TextView tvPhone = findViewById(R.id.tvPhone);
        tvPhone.setText(user.getMobile());
        TextView tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText(user.getEmail());
        TextView tvCountryCode = findViewById(R.id.tvCountryCode);
        tvCountryCode.setText(user.getPhoneCode());

        tempUnit = this.findViewById(R.id.Temperature);
        if (user != null) {
            if (user.getTempUnit() == 1) {
                tempUnit.setText("°C");
            } else {
                tempUnit.setText("°F");
            }
        }
        tempUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View it) {
                final ListPopupWindow listPopupWindow = new ListPopupWindow(UserInfoActivity.this, null, R.attr.listPopupWindowStyle);
                listPopupWindow.setAnchorView(tempUnit);
                final List items = CollectionsKt.listOf("°C", "°F");
                ArrayAdapter adapter = new ArrayAdapter(UserInfoActivity.this, R.layout.user_activity_item_time_item, items);
                listPopupWindow.setAdapter(adapter);
                listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public final void onItemClick(AdapterView parent, View view, final int position, long id) {
                        TuyaHomeSdk.getUserInstance().setTempUnit(Intrinsics.areEqual(items.get(position), "°C") ? TempUnitEnum.Celsius : TempUnitEnum.Fahrenheit, new IResultCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(
                                        UserInfoActivity.this,
                                        "success",
                                        Toast.LENGTH_LONG).show();
                                Button var10000 = tempUnit;
                                Intrinsics.checkExpressionValueIsNotNull(var10000, "tempUnit");
                                var10000.setText((CharSequence) items.get(position));
                            }

                            @Override
                            public void onError(@Nullable String code, @Nullable String error) {
                                Toast.makeText(UserInfoActivity.this, " error->" + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        listPopupWindow.dismiss();
                    }
                });
                listPopupWindow.show();
            }
        });


        this.findViewById(R.id.Updata).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View it) {
                String[] country = new String[]{"China",
                        "America", "English", "Australia", "Japan", "Egypt"};

                AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);

                builder.setItems(country, new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1:
                                lat = ("116.20");
                                lon = ("39.55");
                                break;
                            case 2:
                                lat = ("-77.02");
                                lon = ("39.91");
                                break;
                            case 3:
                                lat = ("-0.05");
                                lon = ("51.36");
                                break;
                            case 4:
                                lat = ("139.46");
                                lon = ("35.42");
                                break;
                            case 5:
                                lat = ("31.14");
                                lon = ("30.01");
                                break;
                            default:
                                break;
                        }

                        TuyaSdk.setLatAndLong(lat, lon);
                        Toast.makeText(UserInfoActivity.this, "success", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
            }
        });
        TextView btTimeZone = findViewById(R.id.btTimeZone);
        btTimeZone.setText(user.getTimezoneId());

        // Data can be issued by the cloud.
        ListPopupWindow listPopupWindow = new ListPopupWindow(this, null, R.attr.listPopupWindowStyle);
        listPopupWindow.setAnchorView(btTimeZone);

        //version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Set<String> availableZoneIds = ZoneId.getAvailableZoneIds();
            items = CollectionsKt.toList(availableZoneIds);
        } else {
            items.add("America/Cuiaba");
            items.add("Asia/Shanghai");
            items.add("America/New_York");
        }


        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.user_activity_item_time_item, items);
        listPopupWindow.setAdapter(arrayAdapter);
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String timezoneId = items.get(position);
                TuyaHomeSdk.getUserInstance().updateTimeZone(
                        timezoneId,
                        new IResultCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(
                                        UserInfoActivity.this,
                                        "success",
                                        Toast.LENGTH_LONG).show();
                                String s = items.get(position);
                                btTimeZone.setText(s);
                            }

                            @Override
                            public void onError(String code, String error) {
                                Toast.makeText(
                                        UserInfoActivity.this,
                                        "error" + error + timezoneId,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                listPopupWindow.dismiss();
            }
        });
        btTimeZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPopupWindow.show();
            }
        });

        findViewById(R.id.deactivate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TuyaHomeSdk.getUserInstance().cancelAccount(new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Toast.makeText(v.getContext(),
                                "error" + error,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(v.getContext(),
                                "deactivate success",
                                Toast.LENGTH_SHORT).show();
                        // Clear cache
                        HomeModel.INSTANCE.clear(v.getContext());

                        // Navigate to User Func Navigation Page
                        Intent intent = new Intent(v.getContext(), UserFuncActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
