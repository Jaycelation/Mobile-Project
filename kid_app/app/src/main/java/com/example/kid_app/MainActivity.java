package com.example.kid_app;

import android.os.Bundle;
import android.widget.TextView;

import com.example.kid_app.common.BaseActivity;

/**
 * MainActivity — màn hình chính placeholder.
 * Ở Bước 1, đây chỉ là màn hình trung gian hiển thị tóm tắt app.
 * Ở các bước sau sẽ được thay thế bởi:
 * - ParentHomeActivity (Bước 4)
 * - ChildHomeActivity (Bước 5)
 * - TeacherHomeActivity (Bước 11)
 * - AdminHomeActivity (Bước 13)
 * dựa vào role người dùng từ Firestore/SharedPreferences.
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}