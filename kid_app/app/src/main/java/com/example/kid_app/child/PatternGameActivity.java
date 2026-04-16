package com.example.kid_app.child;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.model.ActivityAttempt;
import com.example.kid_app.data.repository.ActivityAttemptRepository;
import com.example.kid_app.data.repository.ChildProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PatternGameActivity extends BaseActivity {

    private View layoutGameView, layoutFeedbackView;
    private TextView tvLevel, tvTimer, tvFeedbackEmoji, tvFeedbackTitle, tvExplanationText, tvHint;
    private ImageView ivPattern1, ivPattern2, ivPattern3;
    private ImageButton btnChoice1, btnChoice2, btnChoice3;
    private Button btnRetry, btnNextLevel;
    
    private int currentLevel = 0;
    private int maxQuestions = 10;
    private int totalScore = 0;
    private int seconds = 30;
    private Handler handler = new Handler();
    private long startTime;

    private ActivityAttemptRepository attemptRepository;
    private ChildProfileRepository childProfileRepository;
    
    private String childId, assignmentId;
    private boolean isAssignmentMode = false;
    private String contentId = "game_pattern_01";

    private final int[] fruitRes = {R.drawable.tao_xoa_nen, R.drawable.dua_xoa_nen, R.drawable.qua_le_xoa_nen};
    private final String[] fruitNames = {"Quả Táo", "Quả Dứa", "Quả Lê"};
    
    private int correctFruitRes;
    private TextToSpeech tts;
    private boolean isInteractionEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_game);

        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        isAssignmentMode = getIntent().getBooleanExtra("isAssignmentMode", false);
        if (isAssignmentMode) maxQuestions = 5;

        attemptRepository = new ActivityAttemptRepository();
        childProfileRepository = new ChildProfileRepository();

        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        bindViews();
        setupLevel();
        initTTS();
    }

    private void bindViews() {
        layoutGameView = findViewById(R.id.layout_game_view);
        layoutFeedbackView = findViewById(R.id.layout_feedback_view);
        tvLevel = findViewById(R.id.tv_level);
        tvTimer = findViewById(R.id.tv_timer);
        tvHint = findViewById(R.id.tv_hint);
        ivPattern1 = findViewById(R.id.iv_pattern_1);
        ivPattern2 = findViewById(R.id.iv_pattern_2);
        ivPattern3 = findViewById(R.id.iv_pattern_3);
        btnChoice1 = findViewById(R.id.btn_choice_1);
        btnChoice2 = findViewById(R.id.btn_choice_2);
        btnChoice3 = findViewById(R.id.btn_choice_3);
        tvFeedbackEmoji = findViewById(R.id.tv_feedback_emoji);
        tvFeedbackTitle = findViewById(R.id.tv_feedback_title);
        tvExplanationText = findViewById(R.id.tv_explanation_text);
        btnRetry = findViewById(R.id.btn_retry);
        btnNextLevel = findViewById(R.id.btn_next_level);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { runOnUiThread(() -> setChoicesEnabled(false)); }
                    @Override public void onDone(String id) {
                        runOnUiThread(() -> {
                            if ("question_id".equals(id) || "wrong_id".equals(id)) {
                                setChoicesEnabled(true);
                                if ("question_id".equals(id)) startTimer();
                            }
                        });
                    }
                    @Override public void onError(String id) { runOnUiThread(() -> setChoicesEnabled(true)); }
                });
                speakQuestion();
            }
        });
    }

    private void setupLevel() {
        layoutFeedbackView.setVisibility(View.GONE);
        layoutGameView.setVisibility(View.VISIBLE);
        tvLevel.setText("Câu số " + (currentLevel + 1));
        seconds = 30;
        if (tvTimer != null) tvTimer.setText("00:30");
        startTime = SystemClock.elapsedRealtime();

        Random random = new Random();
        int typeA = random.nextInt(3);
        int typeB; do { typeB = random.nextInt(3); } while (typeA == typeB);

        ivPattern1.setImageResource(fruitRes[typeA]);
        ivPattern2.setImageResource(fruitRes[typeB]);
        ivPattern3.setImageResource(fruitRes[typeA]);
        correctFruitRes = fruitRes[typeB];

        List<Integer> choices = new ArrayList<>();
        choices.add(fruitRes[0]); choices.add(fruitRes[1]); choices.add(fruitRes[2]);
        Collections.shuffle(choices);

        btnChoice1.setImageResource(choices.get(0));
        btnChoice2.setImageResource(choices.get(1));
        btnChoice3.setImageResource(choices.get(2));

        tvHint.setText(fruitNames[typeA] + ", " + fruitNames[typeB] + ", " + fruitNames[typeA] + "...");
        btnChoice1.setOnClickListener(v -> handleAnswer(choices.get(0) == correctFruitRes));
        btnChoice2.setOnClickListener(v -> handleAnswer(choices.get(1) == correctFruitRes));
        btnChoice3.setOnClickListener(v -> handleAnswer(choices.get(2) == correctFruitRes));
        
        setChoicesEnabled(false);
        if (tts != null) { handler.removeCallbacksAndMessages(null); speakQuestion(); }
    }

    private void speakQuestion() {
        speak("Dãy hình đang thiếu gì nhỉ? Bé hãy chọn đáp án đúng nhé!", "question_id");
    }

    private void setChoicesEnabled(boolean enabled) {
        this.isInteractionEnabled = enabled;
        btnChoice1.setEnabled(enabled); btnChoice2.setEnabled(enabled); btnChoice3.setEnabled(enabled);
        float alpha = enabled ? 1.0f : 0.6f;
        btnChoice1.setAlpha(alpha); btnChoice2.setAlpha(alpha); btnChoice3.setAlpha(alpha);
    }

    private void handleAnswer(boolean isCorrect) {
        handler.removeCallbacksAndMessages(null);
        setChoicesEnabled(false);

        if (isCorrect) {
            totalScore += 2;
            if (childId != null) childProfileRepository.addPoints(childId, 2);
            speak("Đúng rồi! Bé giỏi quá!", "praise_id");
            showFeedback(true);
        } else {
            if (isAssignmentMode) {
                currentLevel++;
                new Handler(Looper.getMainLooper()).postDelayed(this::nextLevel, 1500);
            } else {
                speak("Chưa đúng rồi, bé thử lại nhé!", "wrong_id");
                showFeedback(false);
            }
        }
    }

    private void showFeedback(boolean isCorrect) {
        layoutFeedbackView.setVisibility(View.VISIBLE);
        if (isCorrect) {
            tvFeedbackEmoji.setText("🥳");
            tvFeedbackTitle.setText("Chính xác!");
            tvFeedbackTitle.setTextColor(getResources().getColor(R.color.status_success));
            tvExplanationText.setText("Bé giỏi quá! Tiếp tục thử thách tiếp theo nhé.");
            btnNextLevel.setVisibility(View.VISIBLE); btnRetry.setVisibility(View.GONE);
            btnNextLevel.setOnClickListener(v -> { currentLevel++; nextLevel(); });
        } else {
            tvFeedbackEmoji.setText("😟");
            tvFeedbackTitle.setText("Chưa đúng rồi!");
            tvFeedbackTitle.setTextColor(getResources().getColor(R.color.status_error));
            tvExplanationText.setText("Bé quan sát kỹ quy luật xen kẽ của các loại quả nhé!");
            btnRetry.setVisibility(View.VISIBLE); btnNextLevel.setVisibility(View.GONE);
            btnRetry.setOnClickListener(v -> { layoutFeedbackView.setVisibility(View.GONE); speakQuestion(); });
        }
    }

    private void nextLevel() {
        if (currentLevel < maxQuestions) setupLevel();
        else finishGame();
    }

    private void finishGame() {
        if (isAssignmentMode && assignmentId != null && childId != null) updateAssignment();
        Toast.makeText(this, "Hoàn thành! Điểm: " + totalScore, Toast.LENGTH_LONG).show();
        finish();
    }

    private void updateAssignment() {
        Map<String, Object> s = new HashMap<>();
        s.put("status", "submitted"); s.put("score", totalScore); s.put("completedAt", new java.util.Date());
        FirebaseFirestore.getInstance().collection("assignment_submissions")
                .whereEqualTo("childId", childId).whereEqualTo("assignmentId", assignmentId)
                .get().addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) snap.getDocuments().get(0).getReference().update(s);
                    else { s.put("childId", childId); s.put("assignmentId", assignmentId); FirebaseFirestore.getInstance().collection("assignment_submissions").add(s); }
                });
    }

    private void startTimer() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (seconds > 0) { seconds--; tvTimer.setText(String.format(Locale.getDefault(), "00:%02d", seconds)); handler.postDelayed(this, 1000); }
                else { handleAnswer(false); }
            }
        }, 1000);
    }

    private void speak(String text, String id) {
        if (tts != null) { Bundle p = new Bundle(); p.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id); tts.speak(text, TextToSpeech.QUEUE_FLUSH, p, id); }
    }

    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}
