package com.example.kid_app.child;

import android.os.Bundle;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;

/**
 * CountingListActivity — Placeholder cho module Học số đếm (Bước 9).
 *
 * Bước 5: chỉ hiển thị placeholder.
 * Bước 9: sẽ thêm danh sách bài học số đếm + activity đếm vật thể.
 */
public class CountingListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counting_menu);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        findViewById(R.id.card_mode_1).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CountingGameActivity.class));
        });
        
        findViewById(R.id.card_mode_2).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CountingFruitGameActivity.class));
        });
    }
}
