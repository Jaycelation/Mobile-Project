package com.example.kid_app.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;

public class LearningListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_list);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_home).setOnClickListener(v -> finish());

        // 1. Bảng chữ cái (NEW)
        View btnLearnAlphabet = findViewById(R.id.btn_learn_alphabet);
        if (btnLearnAlphabet != null) {
            btnLearnAlphabet.setOnClickListener(v -> {
                startActivity(new Intent(this, AlphabetLearningActivity.class));
            });
        }

        // 2. Các con số
        View btnLearnCounting = findViewById(R.id.btn_learn_counting);
        if (btnLearnCounting != null) {
            btnLearnCounting.setOnClickListener(v -> {
                startActivity(new Intent(this, CountingGameActivity.class));
            });
        }

        // 3. Hình học logic
        View btnLearnShape = findViewById(R.id.btn_learn_shape);
        if (btnLearnShape != null) {
            btnLearnShape.setOnClickListener(v -> {
                startActivity(new Intent(this, ShapeGameActivity.class));
            });
        }

        // 4. Các loại quả
        View btnLearnObject = findViewById(R.id.btn_learn_object);
        if (btnLearnObject != null) {
            btnLearnObject.setOnClickListener(v -> {
                startActivity(new Intent(this, ObjectGameActivity.class));
            });
        }

        // 5. Màu sắc
        View btnLearnColor = findViewById(R.id.btn_learn_color);
        if (btnLearnColor != null) {
            btnLearnColor.setOnClickListener(v -> {
                startActivity(new Intent(this, ColorGameActivity.class));
            });
        }

        // 6. Bạn nhỏ quanh bé
        View btnLearnSound = findViewById(R.id.btn_learn_sound);
        if (btnLearnSound != null) {
            btnLearnSound.setOnClickListener(v -> {
                startActivity(new Intent(this, AnimalSoundGameActivity.class));
            });
        }
    }
}
