package com.example.kid_app.child;

import android.os.Bundle;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;

/**
 * ColorListActivity — Placeholder cho module Học màu sắc (Bước 8).
 *
 * Bước 5: chỉ hiển thị placeholder.
 * Bước 8: sẽ thêm danh sách hoạt động màu sắc + kéo thả nhận biết màu.
 */
public class ColorListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_menu);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        findViewById(R.id.card_mode_1).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ColorGameActivity.class));
        });
        
        findViewById(R.id.card_mode_2).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ColorMatchGameActivity.class));
        });
    }
}
