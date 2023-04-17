package com.tuya.lock.demo.activity.code;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.constant.Constant;
import com.tuya.lock.demo.utils.JSONFormat;


/**
 * 展示用户所有数据
 */
public class ShowCodeActivity extends AppCompatActivity {

    public static void startActivity(Context context, String code) {
        Intent intent = new Intent(context, ShowCodeActivity.class);
        intent.putExtra(Constant.CODE_DATA, code);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_code);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String codeData = getIntent().getStringExtra(Constant.CODE_DATA);

        TextView codeView = findViewById(R.id.code_view);
        String endCode = JSONFormat.format(codeData);
        codeView.setText(endCode);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}