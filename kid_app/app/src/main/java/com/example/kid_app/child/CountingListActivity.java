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
        setContentView(R.layout.activity_module_placeholder);

        String icon  = getIntent().getStringExtra("module_icon");
        String title = getIntent().getStringExtra("module_title");
        String step  = getIntent().getStringExtra("module_step");

        TextView tvIcon  = findViewById(R.id.tv_module_icon);
        TextView tvTitle = findViewById(R.id.tv_module_title);
        TextView tvDesc  = findViewById(R.id.tv_module_desc);

        if (icon  != null) tvIcon.setText(icon);
        if (title != null) tvTitle.setText(title);
        if (step  != null) tvDesc.setText("Module này sẽ có ở " + step + "!");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
