package com.example.kid_app.child;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.repository.ChildProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ColorGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    private Vibrator vibrator;
    
    private View viewColorDisplay;
    private TextView tvColorName, tvProgressInfo;
    private View cardColorMain;

    private int currentIndex = 0;
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId, assignmentId;

    private static class ColorItem {
        String name;
        String hex;
        ColorItem(String name, String hex) { this.name = name; this.hex = hex; }
    }

    private List<ColorItem> colorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_game);

        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);

        initViews();
        initData();
        setCardEnabled(false);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String utteranceId) {}
                    @Override public void onDone(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setCardEnabled(true));
                    }
                    @Override public void onError(String utteranceId) {}
                });
                updateUI();
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        cardColorMain.setOnClickListener(v -> handleColorClick());
    }

    private void initViews() {
        viewColorDisplay = findViewById(R.id.view_color_display);
        tvColorName = findViewById(R.id.tv_color_name);
        tvProgressInfo = findViewById(R.id.tv_progress_info);
        cardColorMain = findViewById(R.id.card_color_main);
        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initData() {
        colorList.add(new ColorItem("Màu Đỏ", "#FF5252"));
        colorList.add(new ColorItem("Màu Xanh Lá", "#4CAF50"));
        colorList.add(new ColorItem("Màu Vàng", "#FFEB3B"));
        colorList.add(new ColorItem("Màu Xanh Dương", "#2196F3"));
        colorList.add(new ColorItem("Màu Cam", "#FF9800"));
        colorList.add(new ColorItem("Màu Hồng", "#E91E63"));
        colorList.add(new ColorItem("Màu Tím", "#9C27B0"));
        colorList.add(new ColorItem("Màu Nâu", "#795548"));
        colorList.add(new ColorItem("Màu Đen", "#000000"));
        colorList.add(new ColorItem("Màu Trắng", "#FFFFFF"));
    }

    private void updateUI() {
        if (currentIndex >= colorList.size()) {
            finishGame();
            return;
        }

        setCardEnabled(false);
        ColorItem current = colorList.get(currentIndex);
        viewColorDisplay.setBackgroundColor(Color.parseColor(current.hex));
        tvColorName.setText(current.name);
        
        if (current.name.equals("Màu Đen")) tvColorName.setTextColor(Color.WHITE);
        else if (current.name.equals("Màu Trắng") || current.name.equals("Màu Vàng")) tvColorName.setTextColor(Color.parseColor("#2E7D32"));
        else tvColorName.setTextColor(Color.WHITE);

        tvProgressInfo.setText("Màu " + (currentIndex + 1) + " / " + colorList.size());
        
        String instruction = currentIndex == 0 ? "Bé hãy chạm vào màu nhé!" : "Đây là " + current.name;
        speakWithId(instruction, "instruction_id");
    }

    private void handleColorClick() {
        setCardEnabled(false);
        cardColorMain.startAnimation(jumpAnim);
        vibrate();

        if (selectedChildId != null) childProfileRepository.addPoints(selectedChildId, 2);

        new Handler().postDelayed(() -> {
            currentIndex++;
            if (currentIndex < colorList.size()) updateUI();
            else finishGame();
        }, 1500);
    }

    private void setCardEnabled(boolean enabled) {
        cardColorMain.setEnabled(enabled);
        cardColorMain.setAlpha(enabled ? 1.0f : 0.6f);
    }

    private void speakWithId(String text, String id) {
        if (tts != null) {
            Bundle p = new Bundle();
            p.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, p, id);
        }
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(80);
        }
    }

    private void finishGame() {
        if (selectedChildId != null) {
            childProfileRepository.addPoints(selectedChildId, 10);
            saveActivityAttempt();
        }
        Toast.makeText(this, "Giỏi lắm! Bé đã học xong! 🎉", Toast.LENGTH_LONG).show();
        finish();
    }

    private void saveActivityAttempt() {
        Map<String, Object> attempt = new HashMap<>();
        attempt.put("contentType", "color");
        attempt.put("score", 30);
        attempt.put("startedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection(AppConstants.COL_CHILD_PROFILES).document(selectedChildId).collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS).add(attempt);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
