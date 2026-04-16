package com.example.kid_app.child;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class NumberMatchGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    
    private View card1, card2, card3;
    private TextView tv1, tv2, tv3, tvInstruction;
    
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId, assignmentId;
    private boolean isAssignmentMode = false;

    private int currentLevel = 0;
    private int maxQuestions = 10;
    private int totalScore = 0;
    
    private int targetNumber;
    private List<Integer> options = new ArrayList<>();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_match_game);

        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        isAssignmentMode = getIntent().getBooleanExtra("isAssignmentMode", false);
        if (isAssignmentMode) maxQuestions = 5;

        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        
        initViews();
        setCardsEnabled(false);
        setupLevelData();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { runOnUiThread(() -> setCardsEnabled(false)); }
                    @Override public void onDone(String id) {
                        runOnUiThread(() -> {
                            if ("question_id".equals(id) || "wrong_id".equals(id)) setCardsEnabled(true);
                            else if ("praise_id".equals(id)) new Handler(Looper.getMainLooper()).postDelayed(() -> nextLevel(), 1000);
                        });
                    }
                    @Override public void onError(String id) { runOnUiThread(() -> setCardsEnabled(true)); }
                });
                speakQuestion();
            } else {
                setCardsEnabled(true);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initViews() {
        card1 = findViewById(R.id.card_number_1); card2 = findViewById(R.id.card_number_2); card3 = findViewById(R.id.card_number_3);
        tv1 = findViewById(R.id.tv_number_1); tv2 = findViewById(R.id.tv_number_2); tv3 = findViewById(R.id.tv_number_3);
        tvInstruction = findViewById(R.id.tv_instruction);
        
        View tvProgress = findViewById(R.id.tv_progress);
        if (tvProgress != null) tvProgress.setVisibility(View.GONE);
    }

    private void setupLevelData() {
        targetNumber = random.nextInt(20) + 1;
        options.clear();
        options.add(targetNumber);
        while (options.size() < 3) {
            int wrong = random.nextInt(20) + 1;
            if (!options.contains(wrong)) options.add(wrong);
        }
        Collections.shuffle(options);

        tvInstruction.setText("Hãy chạm vào số " + targetNumber);
        tv1.setText(String.valueOf(options.get(0)));
        tv2.setText(String.valueOf(options.get(1)));
        tv3.setText(String.valueOf(options.get(2)));

        card1.setOnClickListener(v -> handleAnswer(0, card1));
        card2.setOnClickListener(v -> handleAnswer(1, card2));
        card3.setOnClickListener(v -> handleAnswer(2, card3));
    }

    private void speakQuestion() {
        speak("Bé ơi, hãy chạm vào số " + targetNumber, "question_id");
    }

    private void handleAnswer(int index, View selectedCard) {
        setCardsEnabled(false);
        if (tts != null) tts.stop();

        if (options.get(index) == targetNumber) {
            totalScore += 2;
            if (selectedChildId != null) childProfileRepository.addPoints(selectedChildId, 2);
            
            selectedCard.startAnimation(jumpAnim);
            Toast.makeText(this, "Chính xác! 🥳", Toast.LENGTH_SHORT).show();
            speak("Đúng rồi! Bé giỏi quá!", "praise_id");
            currentLevel++;
        } else {
            if (isAssignmentMode) {
                currentLevel++;
                new Handler(Looper.getMainLooper()).postDelayed(this::nextLevel, 1000);
            } else {
                Toast.makeText(this, "Thử lại nào bé yêu! 😟", Toast.LENGTH_SHORT).show();
                speak("Chưa đúng rồi, bé chọn lại nhé!", "wrong_id");
            }
        }
    }

    private void nextLevel() {
        if (currentLevel < maxQuestions || !isAssignmentMode) {
            setupLevelData();
            speakQuestion();
        } else {
            finishGame();
        }
    }

    private void finishGame() {
        if (isAssignmentMode && assignmentId != null && selectedChildId != null) {
            updateAssignment();
        }
        Toast.makeText(this, "Hoàn thành! Điểm: " + totalScore, Toast.LENGTH_LONG).show();
        finish();
    }

    private void updateAssignment() {
        Map<String, Object> s = new HashMap<>();
        s.put("status", "submitted"); s.put("score", totalScore); s.put("completedAt", new java.util.Date());
        FirebaseFirestore.getInstance().collection("assignment_submissions")
                .whereEqualTo("childId", selectedChildId).whereEqualTo("assignmentId", assignmentId)
                .get().addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) snap.getDocuments().get(0).getReference().update(s);
                    else { s.put("childId", selectedChildId); s.put("assignmentId", assignmentId); FirebaseFirestore.getInstance().collection("assignment_submissions").add(s); }
                });
    }

    private void setCardsEnabled(boolean enabled) {
        card1.setEnabled(enabled); card2.setEnabled(enabled); card3.setEnabled(enabled);
        float a = enabled ? 1.0f : 0.6f; card1.setAlpha(a); card2.setAlpha(a); card3.setAlpha(a);
    }

    private void speak(String text, String id) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id);
        }
    }

    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}
