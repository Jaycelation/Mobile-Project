package com.example.kid_app.child;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

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

public class CountingFruitGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    private GridLayout gridFruits;
    private TextView tvInstruction;
    private CardView[] optionCards = new CardView[3];
    private TextView[] optionTexts = new TextView[3];

    private ChildProfileRepository childProfileRepository;
    private String selectedChildId, assignmentId;
    private boolean isAssignmentMode = false;

    private int currentLevel = 0;
    private int maxQuestions = 10;
    private int totalScore = 0;
    private int targetCount;
    private List<Integer> options = new ArrayList<>();
    private Random random = new Random();

    private final int[] fruitDrawables = {
            R.drawable.tao_xoa_nen,
            R.drawable.qua_le_xoa_nen,
            R.drawable.dua_xoa_nen,
            R.drawable.cam_xoa_nen,
            R.drawable.chuoi,
            R.drawable.dua_hau_xoa_nen
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counting_fruit_game);

        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        isAssignmentMode = getIntent().getBooleanExtra("isAssignmentMode", false);
        if (isAssignmentMode) maxQuestions = 5;

        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        setOptionsEnabled(false);
        generateLevelData();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { runOnUiThread(() -> setOptionsEnabled(false)); }
                    @Override public void onDone(String id) {
                        runOnUiThread(() -> {
                            if ("question_id".equals(id) || "wrong_id".equals(id)) setOptionsEnabled(true);
                            else if ("praise_id".equals(id)) new Handler(Looper.getMainLooper()).postDelayed(() -> nextLevel(), 1000);
                        });
                    }
                    @Override public void onError(String id) { runOnUiThread(() -> setOptionsEnabled(true)); }
                });
                speakQuestion();
            } else {
                setOptionsEnabled(true);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initViews() {
        gridFruits = findViewById(R.id.grid_fruits);
        tvInstruction = findViewById(R.id.tv_instruction);
        optionCards[0] = findViewById(R.id.card_option_1);
        optionCards[1] = findViewById(R.id.card_option_2);
        optionCards[2] = findViewById(R.id.card_option_3);
        optionTexts[0] = findViewById(R.id.tv_option_1);
        optionTexts[1] = findViewById(R.id.tv_option_2);
        optionTexts[2] = findViewById(R.id.tv_option_3);
        for (int i = 0; i < 3; i++) {
            int index = i;
            optionCards[i].setOnClickListener(v -> handleAnswer(index, optionCards[index]));
        }
    }

    private void generateLevelData() {
        gridFruits.removeAllViews();
        targetCount = random.nextInt(10) + 1;
        int fruitRes = fruitDrawables[random.nextInt(fruitDrawables.length)];
        for (int i = 0; i < targetCount; i++) {
            ImageView iv = new ImageView(this);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = (int) (75 * getResources().getDisplayMetrics().density);
            p.height = (int) (75 * getResources().getDisplayMetrics().density);
            p.setMargins(10, 10, 10, 10);
            iv.setLayoutParams(p); iv.setImageResource(fruitRes); iv.setPadding(5, 5, 5, 5);
            gridFruits.addView(iv);
        }
        options.clear(); options.add(targetCount);
        while (options.size() < 3) {
            int w = random.nextInt(10) + 1;
            if (!options.contains(w)) options.add(w);
        }
        Collections.shuffle(options);
        for (int i = 0; i < 3; i++) {
            optionTexts[i].setText(String.valueOf(options.get(i)));
            optionCards[i].setCardBackgroundColor(Color.WHITE);
        }
    }

    private void speakQuestion() {
        speak("Bé hãy đếm xem có bao nhiêu quả nhé!", "question_id");
    }

    private void handleAnswer(int index, View selectedCard) {
        if (tts != null) tts.stop();
        if (options.get(index) == targetCount) {
            setOptionsEnabled(false);
            totalScore += 2;
            selectedCard.startAnimation(jumpAnim);
            ((CardView)selectedCard).setCardBackgroundColor(Color.parseColor("#C8E6C9"));
            if (selectedChildId != null) childProfileRepository.addPoints(selectedChildId, 2);
            speak("Đúng rồi! Có " + targetCount + " quả. Bé giỏi quá!", "praise_id");
            Toast.makeText(this, "Chính xác! 🥳", Toast.LENGTH_SHORT).show();
            currentLevel++;
        } else {
            if (isAssignmentMode) {
                setOptionsEnabled(false);
                ((CardView)selectedCard).setCardBackgroundColor(Color.parseColor("#FFCDD2"));
                currentLevel++;
                new Handler(Looper.getMainLooper()).postDelayed(this::nextLevel, 1000);
            } else {
                setOptionsEnabled(false);
                ((CardView)selectedCard).setCardBackgroundColor(Color.parseColor("#FFCDD2"));
                speak("Chưa đúng rồi, bé đếm lại nhé!", "wrong_id");
                Toast.makeText(this, "Thử lại nào bé! 😟", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> ((CardView)selectedCard).setCardBackgroundColor(Color.WHITE), 1000);
            }
        }
    }

    private void nextLevel() {
        if (currentLevel < maxQuestions || !isAssignmentMode) {
            generateLevelData();
            speakQuestion();
        } else {
            finishGame();
        }
    }

    private void finishGame() {
        if (isAssignmentMode && assignmentId != null && selectedChildId != null) updateAssignment();
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

    private void setOptionsEnabled(boolean e) {
        for (CardView card : optionCards) {
            card.setEnabled(e); card.setClickable(e);
            card.setAlpha(e ? 1.0f : 0.6f);
        }
    }

    private void speak(String text, String utteranceId) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        }
    }

    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}
