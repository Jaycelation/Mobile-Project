package com.example.kid_app.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

/**
 * FirestoreHelper — singleton cung cấp instance Firebase đã cấu hình.
 *
 * Khi USE_EMULATOR = true, app kết nối tới Firebase Local Emulator Suite thay vì cloud.
 * - Firestore : localhost:8080
 * - Auth      : localhost:9099
 * - Storage   : localhost:9199
 *
 * Địa chỉ 10.0.2.2 = "localhost của máy host" khi chạy trên Android Emulator.
 * Nếu chạy trên thiết bị thật thì thay bằng IP WiFi của máy tính (vd: 192.168.1.x).
 *
 * TODO Bước 14: đặt USE_EMULATOR = false khi build production
 */
public class FirestoreHelper {

    // ====== CẤU HÌNH EMULATOR ======
    private static final boolean USE_EMULATOR = true;
    private static final String EMULATOR_HOST = "10.0.2.2";  // Android Emulator → host machine
    private static final int FIRESTORE_PORT = 8080;
    private static final int AUTH_PORT = 9099;
    private static final String STORAGE_HOST = "http://10.0.2.2:9199";
    // ================================

    private static FirestoreHelper instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    private FirestoreHelper() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        if (USE_EMULATOR) {
            setupEmulator();
        }
    }

    private void setupEmulator() {
        // Firestore emulator
        try {
            firestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT);
        } catch (IllegalStateException e) {
            // Đã được cấu hình trước đó, bỏ qua
        }

        // Auth emulator
        try {
            auth.useEmulator(EMULATOR_HOST, AUTH_PORT);
        } catch (IllegalStateException e) {
            // Đã được cấu hình trước đó
        }

        // Storage emulator
        try {
            storage.useEmulator(EMULATOR_HOST, 9199);
        } catch (IllegalStateException e) {
            // Đã được cấu hình trước đó
        }
    }

    /**
     * Lấy singleton instance.
     * Gọi một lần trong Application.onCreate() để đảm bảo emulator được setup sớm nhất.
     */
    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }
}
