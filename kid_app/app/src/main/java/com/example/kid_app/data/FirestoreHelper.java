package com.example.kid_app.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * FirestoreHelper — singleton cung cấp instance Firebase.
 * ĐÃ TẮT EMULATOR ĐỂ DÙNG CLOUD THẬT.
 */
public class FirestoreHelper {

    private static FirestoreHelper instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    // Chuc nang: khoi tao cac instance Firebase dung chung cho toan bo ung dung.
    private FirestoreHelper() {
        // Khởi tạo trực tiếp từ Cloud
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        
        // Loại bỏ hoàn toàn logic Emulator để tránh nhầm lẫn
    }

    // Chuc nang: cung cap singleton de cac repository dung chung ket noi Firebase.
    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    // Chuc nang: tra ve doi tuong Firestore de doc/ghi du lieu cloud.
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    // Chuc nang: tra ve Firebase Auth de xu ly dang nhap va dang ky.
    public FirebaseAuth getAuth() {
        return auth;
    }

    // Chuc nang: tra ve Firebase Storage de luu tep neu can.
    public FirebaseStorage getStorage() {
        return storage;
    }
}
