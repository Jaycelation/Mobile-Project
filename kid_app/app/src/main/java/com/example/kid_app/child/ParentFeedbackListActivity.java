package com.example.kid_app.child;

import android.os.Bundle;
import com.example.kid_app.common.BaseActivity;

/**
 * ParentFeedbackListActivity - Lớp này hiện không còn được sử dụng do Phụ huynh đã vào thẳng màn hình Chat.
 * Giữ lại định nghĩa trống để tránh lỗi Build nếu có file khác tham chiếu.
 */
public class ParentFeedbackListActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish(); // Tự động đóng nếu vô tình mở phải
    }
}
