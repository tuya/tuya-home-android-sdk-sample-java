package com.thing.smart.sweeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * create by nielev on 2023/2/22
 */
public class SweeperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sweeper);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btnCommonControl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to device management
                try {
                    Class deviceControl = Class.forName("com.tuya.appsdk.sample.device.mgt.control.activity.DeviceMgtControlActivity");
                    Intent intent = new Intent(v.getContext(), deviceControl);
                    intent.putExtra("deviceId", getIntent().getStringExtra("deviceId"));
                    v.getContext().startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
        findViewById(R.id.btnP2pConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), P2pConnectActivity.class);
                intent.putExtra("deviceId", getIntent().getStringExtra("deviceId"));
                v.getContext().startActivity(intent);
            }
        });
    }
}