package com.example.kid_app.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;

public class GameListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_home).setOnClickListener(v -> finish());

        // 1. Thử thách con số
        View btnPlayNumber = findViewById(R.id.btn_play_number);
        if (btnPlayNumber != null) {
            btnPlayNumber.setOnClickListener(v -> {
                startActivity(new Intent(this, NumberMatchGameActivity.class));
            });
        }

        // 10. Đếm số quả
        View btnPlayCountingFruit = findViewById(R.id.btn_play_counting_fruit);
        if (btnPlayCountingFruit != null) {
            btnPlayCountingFruit.setOnClickListener(v -> {
                startActivity(new Intent(this, CountingFruitGameActivity.class));
            });
        }

        // 2. Ghép hình bóng
        View btnPlayShadow = findViewById(R.id.btn_play_shadow);
        if (btnPlayShadow != null) {
            btnPlayShadow.setOnClickListener(v -> {
                startActivity(new Intent(this, ShadowMatchGameActivity.class));
            });
        }

        // 3. Đúng con vật
        View btnPlayAnimal = findViewById(R.id.btn_play_animal);
        if (btnPlayAnimal != null) {
            btnPlayAnimal.setOnClickListener(v -> {
                startActivity(new Intent(this, AnimalGameActivity.class));
            });
        }

        // 9. Đúng chữ cái
        View btnPlayAlphabetMatch = findViewById(R.id.btn_play_alphabet_match);
        if (btnPlayAlphabetMatch != null) {
            btnPlayAlphabetMatch.setOnClickListener(v -> {
                startActivity(new Intent(this, AlphabetMatchGameActivity.class));
            });
        }

        // 7. Đúng loại quả
        View btnPlayFruit = findViewById(R.id.btn_play_fruit);
        if (btnPlayFruit != null) {
            btnPlayFruit.setOnClickListener(v -> {
                startActivity(new Intent(this, FruitMatchGameActivity.class));
            });
        }

        // 8. Đúng màu sắc
        View btnPlayColorMatch = findViewById(R.id.btn_play_color_match);
        if (btnPlayColorMatch != null) {
            btnPlayColorMatch.setOnClickListener(v -> {
                startActivity(new Intent(this, ColorMatchGameActivity.class));
            });
        }

        // 4. Quy luật
        View btnPlayPattern = findViewById(R.id.btn_play_pattern);
        if (btnPlayPattern != null) {
            btnPlayPattern.setOnClickListener(v -> {
                startActivity(new Intent(this, PatternGameActivity.class));
            });
        }

        // 5. Nhanh tay
        View btnPlayFast = findViewById(R.id.btn_play_fast);
        if (btnPlayFast != null) {
            btnPlayFast.setOnClickListener(v -> {
                startActivity(new Intent(this, FastEyeGameActivity.class));
            });
        }

        // 6. Ghép tranh
        View btnPlayPuzzle = findViewById(R.id.btn_play_puzzle);
        if (btnPlayPuzzle != null) {
            btnPlayPuzzle.setOnClickListener(v -> {
                startActivity(new Intent(this, PuzzleGameActivity.class));
            });
        }
    }
}
