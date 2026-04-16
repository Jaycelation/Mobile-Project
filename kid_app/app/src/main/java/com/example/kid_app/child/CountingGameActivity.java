package com.example.kid_app.child;

import android.content.Context;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CountingGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    private Vibrator vibrator;
    
    private TextView tvNumberValue, tvNumberName, tvProgressInfo;
    private View cardNumberMain, layoutDisplay;

    private int currentNumber = 1;
    private final int MAX_NUMBER = 20;
    
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId;
    private String assignmentId;

    private final String[] numberNames = {
        "", "Một", "Hai", "Ba", "Bốn", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười",
        "Mười Một", "Mười Hai", "Mười Ba", "Mười Bốn", "Mười Lăm", "Mười Sáu", "Mười Bảy", "Mười Tám", "Mười Chín", "Hai Mươi"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counting_game);

        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        
        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);

        initViews();
        setNumberClickable(false);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setNumberClickable(false));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if ("instruction_id".equals(utteranceId)) {
                                setNumberClickable(true); 
                            } else if ("info_id".equals(utteranceId)) {
                                new Handler().postDelayed(() -> {
                                    currentNumber++;
                                    if (currentNumber <= MAX_NUMBER) updateUI();
                                    else finishGame();
                                }, 1000);
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setNumberClickable(true));
                    }
                });
                updateUI();
            } else {
                setNumberClickable(true);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        cardNumberMain.setOnClickListener(v -> handleNumberClick());
    }

    private void initViews() {
        tvNumberValue = findViewById(R.id.tv_number_value);
        tvNumberName = findViewById(R.id.tv_number_name);
        tvProgressInfo = findViewById(R.id.tv_progress_info);
        cardNumberMain = findViewById(R.id.card_number_main);
        layoutDisplay = findViewById(R.id.layout_number_display);
        
        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void updateUI() {
        if (currentNumber > MAX_NUMBER) {
            finishGame();
            return;
        }

        setNumberClickable(false);
        // HIỆN SỐ THẬT NHƯ CŨ
        tvNumberValue.setText(String.valueOf(currentNumber));
        tvNumberName.setText("Số " + numberNames[currentNumber]);
        tvProgressInfo.setText("Số " + currentNumber + " / " + MAX_NUMBER);
        
        cardNumberMain.setBackgroundResource(R.drawable.bg_icon_circle);
        cardNumberMain.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFA726));

        // CHỈ ĐỔI GIỌNG NÓI HƯỚNG DẪN
        String text = currentNumber == 1 ? "Bé hãy chạm vào vòng tròn nhé!" : "Tiếp theo nào!";
        speakWithId(text, "instruction_id");
    }

    private void handleNumberClick() {
        setNumberClickable(false);
        cardNumberMain.startAnimation(jumpAnim);
        vibrateDevice();

        if (selectedChildId != null) {
            childProfileRepository.addPoints(selectedChildId, 2);
        }

        // Đọc to tên số khi bé nhấn
        String text = "Đây là số " + numberNames[currentNumber];
        speakWithId(text, "info_id");
    }

    private void setNumberClickable(boolean enabled) {
        cardNumberMain.setClickable(enabled);
        cardNumberMain.setEnabled(enabled);
        cardNumberMain.setAlpha(enabled ? 1.0f : 0.6f);
    }

    private void speakWithId(String text, String id) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id);
        }
    }

    private void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(80);
            }
        }
    }

    private void finishGame() {
        if (selectedChildId != null) {
            childProfileRepository.addPoints(selectedChildId, 10);
            saveActivityAttempt();
        }
        Toast.makeText(this, "Tuyệt vời! Bé đã học xong! 🌟", Toast.LENGTH_LONG).show();
        finish();
    }

    private void saveActivityAttempt() {
        Map<String, Object> attempt = new HashMap<>();
        attempt.put("contentType", "counting");
        attempt.put("score", 50);
        attempt.put("startedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection(AppConstants.COL_CHILD_PROFILES).document(selectedChildId).collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS).add(attempt);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
