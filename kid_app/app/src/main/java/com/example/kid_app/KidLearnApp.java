package com.example.kid_app;

import android.app.Application;

import com.example.kid_app.data.FirestoreHelper;

/**
 * Application class — điểm khởi động toàn cục của app.
 * Phải đăng ký trong AndroidManifest: android:name=".KidLearnApp"
 *
 * Khởi tạo FirestoreHelper sớm nhất có thể để emulator được cấu hình
 * trước khi bất kỳ Activity nào dùng Firebase.
 */
public class KidLearnApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Firebase helper (setup emulator nếu USE_EMULATOR=true)
        FirestoreHelper.getInstance();
    }
}
