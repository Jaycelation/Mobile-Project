package com.example.kid_app.child;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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

public class AnimalGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private Animation jumpAnim;
    
    private View card1, card2, card3;
    private ImageView iv1, iv2, iv3;
    private TextView tvInstruction;
    
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId, assignmentId;
    private boolean isAssignmentMode = false;

    private int currentLevel = 0;
    private int maxQuestions = 10; 
    private int totalScore = 0;

    private static class Animal {
        String name;
        int resId;
        int soundId;
        Animal(String n, int r, int s) { name = n; resId = r; soundId = s; }
    }

    private List<Animal> allAnimals = new ArrayList<>();
    private List<Integer> currentOptions = new ArrayList<>();
    private int correctIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_game);

        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        isAssignmentMode = getIntent().getBooleanExtra("isAssignmentMode", false);
        if (isAssignmentMode) maxQuestions = 5;

        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initData();
        initViews();
        setCardsEnabled(false);
        generateLevel();

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
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initData() {
        allAnimals.add(new Animal("con sư tử", R.drawable.con_su_tu, R.raw.lion_roar));
        allAnimals.add(new Animal("con chó", R.drawable.cho, R.raw.dog_bark));
        allAnimals.add(new Animal("con bò", R.drawable.bo, R.raw.bo));
        allAnimals.add(new Animal("con mèo", R.drawable.meo, R.raw.meo));
        allAnimals.add(new Animal("con vịt", R.drawable.vit, R.raw.vit));
    }

    private void initViews() {
        card1 = findViewById(R.id.card_animal_1); card2 = findViewById(R.id.card_animal_2); card3 = findViewById(R.id.card_animal_3);
        iv1 = findViewById(R.id.iv_animal_1); iv2 = findViewById(R.id.iv_animal_2); iv3 = findViewById(R.id.iv_animal_3);
        tvInstruction = findViewById(R.id.tv_instruction);
        card1.setOnClickListener(v -> handleAnswer(0, card1));
        card2.setOnClickListener(v -> handleAnswer(1, card2));
        card3.setOnClickListener(v -> handleAnswer(2, card3));
    }

    private void generateLevel() {
        Random r = new Random();
        int targetIdx = r.nextInt(allAnimals.size());
        currentOptions.clear();
        currentOptions.add(targetIdx);
        while (currentOptions.size() < 3) {
            int rand = r.nextInt(allAnimals.size());
            if (!currentOptions.contains(rand)) currentOptions.add(rand);
        }
        Collections.shuffle(currentOptions);
        for(int i=0; i<3; i++) if(currentOptions.get(i) == targetIdx) correctIndex = i;

        tvInstruction.setText("Hãy chạm vào " + allAnimals.get(targetIdx).name);
        iv1.setImageResource(allAnimals.get(currentOptions.get(0)).resId);
        iv2.setImageResource(allAnimals.get(currentOptions.get(1)).resId);
        iv3.setImageResource(allAnimals.get(currentOptions.get(2)).resId);
    }

    private void speakQuestion() {
        int targetIdx = currentOptions.get(correctIndex);
        String text = "Bé ơi, hãy chạm vào " + allAnimals.get(targetIdx).name;
        Bundle p = new Bundle(); p.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "question_id");
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, p, "question_id");
    }

    private void handleAnswer(int idx, View v) {
        if (idx == correctIndex) {
            setCardsEnabled(false);
            totalScore += 2;
            v.startAnimation(jumpAnim);
            playAnimalSound(allAnimals.get(currentOptions.get(idx)).soundId);
            if (selectedChildId != null) childProfileRepository.addPoints(selectedChildId, 2);
            new Handler().postDelayed(() -> {
                Bundle p = new Bundle(); p.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "praise_id");
                if (tts != null) tts.speak("Đúng rồi! Bé giỏi quá!", TextToSpeech.QUEUE_FLUSH, p, "praise_id");
                Toast.makeText(this, "Chính xác! 🥳", Toast.LENGTH_SHORT).show();
            }, 1000);
            currentLevel++;
        } else {
            if (isAssignmentMode) {
                setCardsEnabled(false);
                currentLevel++;
                new Handler().postDelayed(this::nextLevel, 1000);
            } else {
                setCardsEnabled(false);
                Bundle p = new Bundle(); p.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wrong_id");
                if (tts != null) tts.speak("Chưa đúng rồi, bé chọn lại nhé!", TextToSpeech.QUEUE_FLUSH, p, "wrong_id");
            }
        }
    }

    private void nextLevel() {
        if (currentLevel < maxQuestions) {
            generateLevel();
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

    private void playAnimalSound(int id) { stopSound(); mediaPlayer = MediaPlayer.create(this, id); if (mediaPlayer != null) mediaPlayer.start(); }
    private void stopSound() { if (mediaPlayer != null) { try { if (mediaPlayer.isPlaying()) mediaPlayer.stop(); } catch (Exception e) {} mediaPlayer.release(); mediaPlayer = null; } }
    private void setCardsEnabled(boolean e) { card1.setEnabled(e); card2.setEnabled(e); card3.setEnabled(e); float a = e ? 1.0f : 0.6f; card1.setAlpha(a); card2.setAlpha(a); card3.setAlpha(a); }

    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } stopSound(); super.onDestroy(); }
}
