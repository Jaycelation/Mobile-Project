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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PuzzleGameActivity extends BaseActivity {

    private ImageView ivLeftHalf, ivRightPlaceholder;
    private ImageButton btnOption1, btnOption2, btnOption3;
    private View layoutFeedbackView;
    private TextView tvFeedbackEmoji, tvFeedbackTitle, tvFeedbackDesc, tvLevel, tvTimer, tvInstruction;
    private Button btnRetry, btnNext;

    private String childId, attemptId, assignmentId;
    private long startTime;
    private ActivityAttemptRepository attemptRepository;
    private ChildProfileRepository childProfileRepository;

    private int currentLevel = 1;
    private int maxQuestions = 10;
    private int totalScore = 0;
    private boolean isAssignmentMode = false;
    private TextToSpeech tts;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private int secondsElapsed = 0;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            secondsElapsed++;
            tvTimer.setText(String.format(Locale.getDefault(), "%02d", secondsElapsed % 100));
            timerHandler.postDelayed(this, 1000);
        }
    };
    
    private final int[][] fruitPairs = {
        {R.drawable.ic_fruit_apple_left, R.drawable.ic_fruit_apple_right},
        {R.drawable.ic_fruit_pear_left, R.drawable.ic_fruit_pear_right},
        {R.drawable.ic_fruit_banana_left, R.drawable.ic_fruit_banana_right}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);

        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        isAssignmentMode = getIntent().getBooleanExtra("isAssignmentMode", false);
        if (isAssignmentMode) maxQuestions = 5;

        attemptRepository = new ActivityAttemptRepository();
        childProfileRepository = new ChildProfileRepository();

        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        bindViews();
        initTTS();
        setupLevel();
        startAttempt();
        startTimer();
    }

    private void bindViews() {
        ivLeftHalf = findViewById(R.id.iv_left_half);
        ivRightPlaceholder = findViewById(R.id.iv_right_placeholder);
        btnOption1 = findViewById(R.id.btn_option_1);
        btnOption2 = findViewById(R.id.btn_option_2);
        btnOption3 = findViewById(R.id.btn_option_3);
        
        layoutFeedbackView = findViewById(R.id.layout_feedback_view);
        tvFeedbackEmoji = findViewById(R.id.tv_feedback_emoji);
        tvFeedbackTitle = findViewById(R.id.tv_feedback_title);
        tvFeedbackDesc = findViewById(R.id.tv_feedback_desc);
        tvInstruction = findViewById(R.id.tv_instruction);
        
        btnRetry = findViewById(R.id.btn_retry);
        btnNext = findViewById(R.id.btn_next);
        tvLevel = findViewById(R.id.tv_level);
        tvTimer = findViewById(R.id.tv_timer);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnRetry.setOnClickListener(v -> layoutFeedbackView.setVisibility(View.GONE));
        
        btnNext.setOnClickListener(v -> {
            if (currentLevel < maxQuestions) {
                currentLevel++;
                setupLevel();
            } else {
                finishGame();
            }
        });
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> setOptionsEnabled(false));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            if ("instruction".equals(utteranceId) || "wrong".equals(utteranceId)) {
                                setOptionsEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> setOptionsEnabled(true));
                    }
                });
                speakInstruction();
            }
        });
    }

    private void speakInstruction() {
        speak("Bé hãy tìm mảnh ghép còn thiếu để hoàn thiện bức tranh nhé!", "instruction");
    }

    private void speak(String text, String utteranceId) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        }
    }

    private void setOptionsEnabled(boolean enabled) {
        btnOption1.setEnabled(enabled);
        btnOption2.setEnabled(enabled);
        btnOption3.setEnabled(enabled);
        btnOption1.setAlpha(enabled ? 1.0f : 0.6f);
        btnOption2.setAlpha(enabled ? 1.0f : 0.6f);
        btnOption3.setAlpha(enabled ? 1.0f : 0.6f);
    }

    private void startTimer() {
        secondsElapsed = 0;
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void setupLevel() {
        layoutFeedbackView.setVisibility(View.GONE);
        ivRightPlaceholder.setImageResource(0);
        ivRightPlaceholder.setBackgroundResource(R.drawable.bg_pattern_item_missing);
        ivRightPlaceholder.setAlpha(0.5f);
        ivRightPlaceholder.setPadding(20, 20, 20, 20);
        
        tvLevel.setText("Câu " + currentLevel + "/" + maxQuestions);
        startTime = SystemClock.elapsedRealtime();

        Random random = new Random();
        int targetIdx = random.nextInt(fruitPairs.length);
        
        ivLeftHalf.setImageResource(fruitPairs[targetIdx][0]);
        
        int correctPos = random.nextInt(3);
        int wrongIdx1 = (targetIdx + 1) % fruitPairs.length;
        int wrongIdx2 = (targetIdx + 2) % fruitPairs.length;

        if (correctPos == 0) {
            btnOption1.setImageResource(fruitPairs[targetIdx][1]);
            btnOption2.setImageResource(fruitPairs[wrongIdx1][1]);
            btnOption3.setImageResource(fruitPairs[wrongIdx2][1]);
        } else if (correctPos == 1) {
            btnOption1.setImageResource(fruitPairs[wrongIdx1][1]);
            btnOption2.setImageResource(fruitPairs[targetIdx][1]);
            btnOption3.setImageResource(fruitPairs[wrongIdx2][1]);
        } else {
            btnOption1.setImageResource(fruitPairs[wrongIdx1][1]);
            btnOption2.setImageResource(fruitPairs[wrongIdx2][1]);
            btnOption3.setImageResource(fruitPairs[targetIdx][1]);
        }

        setChoiceListeners(correctPos + 1, fruitPairs[targetIdx][1]);
        if (currentLevel > 1) {
            speakInstruction();
        }
    }

    private void setChoiceListeners(int correctIndex, int correctRes) {
        btnOption1.setOnClickListener(v -> handleAnswer(correctIndex == 1, correctRes));
        btnOption2.setOnClickListener(v -> handleAnswer(correctIndex == 2, correctRes));
        btnOption3.setOnClickListener(v -> handleAnswer(correctIndex == 3, correctRes));
    }

    private void startAttempt() {
        if (childId != null) {
            ActivityAttempt attempt = new ActivityAttempt(childId, "game_puzzle_01", null, 
                isAssignmentMode ? AppConstants.SESSION_ASSIGNMENT : AppConstants.SESSION_FREE_PLAY, "game");
            attemptRepository.startAttempt(childId, attempt).addOnSuccessListener(ref -> attemptId = ref.getId());
        }
    }

    private void handleAnswer(boolean isCorrect, int correctRes) {
        if (tts != null) tts.stop();
        if (isCorrect) {
            totalScore += 2;
            layoutFeedbackView.setVisibility(View.VISIBLE);
            
            ivRightPlaceholder.setImageResource(correctRes);
            ivRightPlaceholder.setBackground(null);
            ivRightPlaceholder.setAlpha(1.0f);
            ivRightPlaceholder.setPadding(0, 0, 0, 0);

            tvFeedbackEmoji.setText("🥳");
            tvFeedbackTitle.setText("Chính xác!");
            tvFeedbackTitle.setTextColor(getResources().getColor(R.color.status_success));
            tvFeedbackDesc.setText("Bé thật khéo tay, mảnh ghép hoàn toàn khớp rồi!");
            
            btnRetry.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
            if (currentLevel >= maxQuestions) {
                btnNext.setText("HOÀN THÀNH 🏁");
            } else {
                btnNext.setText("CÂU TIẾP THEO ➔");
            }
            
            speak("Đúng rồi! Bé giỏi quá!", "correct");
            saveResult(2, "passed");
        } else {
            if (isAssignmentMode) {
                setOptionsEnabled(false);
                speak("Chưa chính xác rồi!", "wrong");
                Toast.makeText(this, "Chưa khớp rồi! 😟", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (currentLevel < maxQuestions) {
                        currentLevel++;
                        setupLevel();
                    } else {
                        finishGame();
                    }
                }, 1500);
            } else {
                speak("Chưa đúng rồi, bé chọn lại nhé!", "wrong");
                Toast.makeText(this, "Chưa khớp rồi, bé chọn mảnh khác nhé! 😟", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveResult(int score, String status) {
        if (childId != null && attemptId != null) {
            int duration = (int) ((SystemClock.elapsedRealtime() - startTime) / 1000);
            attemptRepository.completeAttempt(childId, attemptId, score, status, duration);
            childProfileRepository.addPoints(childId, score);
        }
    }

    private void finishGame() {
        timerHandler.removeCallbacks(timerRunnable);
        if (isAssignmentMode && assignmentId != null && childId != null) {
            updateAssignment();
        }
        Toast.makeText(this, "Hoàn thành! Tổng điểm: " + totalScore, Toast.LENGTH_LONG).show();
        finish();
    }

    private void updateAssignment() {
        Map<String, Object> submission = new HashMap<>();
        submission.put("status", "submitted");
        submission.put("score", totalScore);
        submission.put("completedAt", new java.util.Date());
        
        FirebaseFirestore.getInstance().collection("assignment_submissions")
                .whereEqualTo("childId", childId)
                .whereEqualTo("assignmentId", assignmentId)
                .get().addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        snap.getDocuments().get(0).getReference().update(submission);
                    } else {
                        submission.put("childId", childId);
                        submission.put("assignmentId", assignmentId);
                        FirebaseFirestore.getInstance().collection("assignment_submissions").add(submission);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        timerHandler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }
}
