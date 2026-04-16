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

public class AlphabetLearningActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    private Vibrator vibrator;
    
    private TextView tvLetterValue, tvLetterName, tvProgressInfo;
    private View cardAlphabetMain, layoutDisplay;

    private int currentIndex = 0;
    private final String[] alphabet = {
        "A", "Ă", "Â", "B", "C", "D", "Đ", "E", "Ê", "G", "H", "I", "K", "L", "M", 
        "N", "O", "Ô", "Ơ", "P", "Q", "R", "S", "T", "U", "Ư", "V", "X", "Y"
    };

    private final String[] letterNames = {
        "A", "Ă", "Â", "Bê", "Xê", "Dê", "Đê", "E", "Ê", "Giê", "Hát", "I", "Ka", "En-lờ", "Em-mờ",
        "En-nờ", "O", "Ô", "Ơ", "Pê", "Qui", "Er-rờ", "Ét-xì", "Tê", "U", "Ư", "Vê", "Ít-xì", "Y"
    };
    
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId;
    private String assignmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabet_learning);

        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        
        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);

        initViews();
        setLetterClickable(false); // Khóa ngay từ đầu

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setLetterClickable(false));
                    }
                    @Override
                    public void onDone(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            // Chỉ cho phép ấn sau khi đọc xong giới thiệu hoặc xong hiệu lệnh "Tiếp theo"
                            if ("intro_id".equals(utteranceId) || "next_prompt_id".equals(utteranceId)) {
                                setLetterClickable(true);
                            } else if ("info_id".equals(utteranceId)) {
                                // Sau khi đọc xong tên chữ cái bé vừa ấn -> Tự động chuyển câu sau 1s
                                new Handler().postDelayed(() -> moveToNextLetter(), 1000);
                            }
                        });
                    }
                    @Override
                    public void onError(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setLetterClickable(true));
                    }
                });
                updateUI();
                speakIntro();
            } else {
                setLetterClickable(true);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        cardAlphabetMain.setOnClickListener(v -> handleLetterClick());
    }

    private void initViews() {
        tvLetterValue = findViewById(R.id.tv_letter_value);
        tvLetterName = findViewById(R.id.tv_letter_name);
        tvProgressInfo = findViewById(R.id.tv_progress_info);
        cardAlphabetMain = findViewById(R.id.card_alphabet_main);
        layoutDisplay = findViewById(R.id.layout_alphabet_display);
        
        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void updateUI() {
        if (currentIndex >= alphabet.length) {
            finishGame();
            return;
        }

        tvLetterValue.setText(alphabet[currentIndex]);
        tvLetterName.setText("Chữ " + alphabet[currentIndex]);
        tvProgressInfo.setText("Chữ " + (currentIndex + 1) + " / " + alphabet.length);
        
        int[] colors = {0xFFAD1457, 0xFF6A1B9A, 0xFF283593, 0xFF2E7D32, 0xFFEF6C00, 0xFF37474F};
        layoutDisplay.setBackgroundColor(colors[currentIndex % colors.length]);
    }

    private void speakIntro() {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "intro_id");
        tts.speak("Bé hãy chạm vào chữ cái để học nhé!", TextToSpeech.QUEUE_FLUSH, params, "intro_id");
    }

    private void handleLetterClick() {
        setLetterClickable(false); // Khóa ngay khi vừa ấn để tránh ấn nhiều lần
        cardAlphabetMain.startAnimation(jumpAnim);
        vibrateDevice();

        if (selectedChildId != null) {
            childProfileRepository.addPoints(selectedChildId, 2);
        }

        String text = "Đây là chữ " + letterNames[currentIndex];
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "info_id");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "info_id");
    }

    private void moveToNextLetter() {
        currentIndex++;
        if (currentIndex < alphabet.length) {
            updateUI();
            setLetterClickable(false);
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "next_prompt_id");
            tts.speak("Tiếp theo nào!", TextToSpeech.QUEUE_FLUSH, params, "next_prompt_id");
        } else {
            finishGame();
        }
    }

    private void setLetterClickable(boolean clickable) {
        cardAlphabetMain.setClickable(clickable);
        cardAlphabetMain.setEnabled(clickable); // Khóa vật lý hoàn toàn
        cardAlphabetMain.setAlpha(clickable ? 1.0f : 0.6f);
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
            childProfileRepository.addPoints(selectedChildId, 42);
            saveActivityAttempt();
            if (assignmentId != null) {
                updateAssignmentSubmission();
            }
        }
        Toast.makeText(this, "Tuyệt vời! Bé đã học xong! 🌟", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(this::finish, 1500);
    }

    private void saveActivityAttempt() {
        Map<String, Object> attempt = new HashMap<>();
        attempt.put("contentType", "game");
        attempt.put("score", 100);
        attempt.put("startedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection(AppConstants.COL_CHILD_PROFILES).document(selectedChildId).collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS).add(attempt);
    }

    private void updateAssignmentSubmission() {
        Map<String, Object> submission = new HashMap<>();
        submission.put("status", "submitted");
        submission.put("score", 100);
        submission.put("completedAt", new java.util.Date());
        FirebaseFirestore.getInstance().collection("assignment_submissions").whereEqualTo("childId", selectedChildId).whereEqualTo("assignmentId", assignmentId).get().addOnSuccessListener(snap -> {
            if (!snap.isEmpty()) snap.getDocuments().get(0).getReference().update(submission);
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
