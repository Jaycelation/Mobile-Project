package com.example.kid_app.child;

import android.content.SharedPreferences;
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

public class AlphabetMatchGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    
    private View card1, card2, card3;
    private TextView tvLetter1, tvLetter2, tvLetter3, tvInstruction;
    
    private ChildProfileRepository childProfileRepository;
    private ActivityAttemptRepository attemptRepository;
    private String selectedChildId, assignmentId;
    private boolean isAssignmentMode = false;

    private int currentLevel = 0;
    private int maxQuestions = 10;
    private int totalScore = 0;
    
    private final String[] alphabet = {
        "A", "Ă", "Â", "B", "C", "D", "Đ", "E", "Ê", "G", "H", "I", "K", "L", "M", 
        "N", "O", "Ô", "Ơ", "P", "Q", "R", "S", "T", "U", "Ư", "V", "X", "Y"
    };

    private final String[] letterSpeechNames = {
        "A", "Ă", "Â", "Bê", "Xê", "Dê", "Đê", "E", "Ê", "Giê", "Hát", "I", "Ka", "En-lờ", "Em-mờ",
        "En-nờ", "O", "Ô", "Ơ", "Pê", "Qui", "Er-rờ", "Ét-xì", "Tê", "U", "Ư", "Vê", "Ít-xì", "Y"
    };

    private String targetLetter = "";
    private String targetSpeechName = "";
    private int correctCardIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabet_match_game);

        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        isAssignmentMode = getIntent().getBooleanExtra("isAssignmentMode", false);
        if (isAssignmentMode) maxQuestions = 5;

        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        childProfileRepository = new ChildProfileRepository();
        attemptRepository = new ActivityAttemptRepository();
        
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        selectedChildId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        setCardsEnabled(false);
        setupNewLevelData(); 

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> setCardsEnabled(false));
                    }
                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            if ("question_id".equals(utteranceId) || "wrong_id".equals(utteranceId)) {
                                setCardsEnabled(true);
                            } else if ("praise_id".equals(utteranceId)) {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    currentLevel++;
                                    if (currentLevel < maxQuestions) {
                                        setupNewLevelData();
                                        speakQuestion();
                                    } else {
                                        finishGame();
                                    }
                                }, 500);
                            }
                        });
                    }
                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> setCardsEnabled(true));
                    }
                });
                speakQuestion();
            } else {
                setCardsEnabled(true);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initViews() {
        card1 = findViewById(R.id.card_letter_1);
        card2 = findViewById(R.id.card_letter_2);
        card3 = findViewById(R.id.card_letter_3);
        tvLetter1 = findViewById(R.id.tv_letter_1);
        tvLetter2 = findViewById(R.id.tv_letter_2);
        tvLetter3 = findViewById(R.id.tv_letter_3);
        tvInstruction = findViewById(R.id.tv_instruction);

        card1.setOnClickListener(v -> handleAnswer(0, card1));
        card2.setOnClickListener(v -> handleAnswer(1, card2));
        card3.setOnClickListener(v -> handleAnswer(2, card3));
    }

    private void setupNewLevelData() {
        Random random = new Random();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < alphabet.length; i++) indices.add(i);
        Collections.shuffle(indices);

        int correctIdxInAlphabet = indices.get(0);
        targetLetter = alphabet[correctIdxInAlphabet];
        targetSpeechName = letterSpeechNames[correctIdxInAlphabet];
        
        correctCardIndex = random.nextInt(3);
        
        String[] options = new String[3];
        options[correctCardIndex] = targetLetter;
        options[(correctCardIndex + 1) % 3] = alphabet[indices.get(1)];
        options[(correctCardIndex + 2) % 3] = alphabet[indices.get(2)];

        tvLetter1.setText(options[0]);
        tvLetter2.setText(options[1]);
        tvLetter3.setText(options[2]);
        
        tvInstruction.setText("Câu " + (currentLevel + 1) + "/" + maxQuestions + ": Chạm vào chữ " + targetLetter);
    }

    private void speakQuestion() {
        if (tts == null) return;
        String text = "Bé ơi, hãy chạm vào chữ " + targetSpeechName;
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "question_id");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "question_id");
    }

    private void handleAnswer(int selectedIndex, View selectedCard) {
        if (tts != null) tts.stop();
        if (selectedIndex == correctCardIndex) {
            setCardsEnabled(false);
            totalScore += 2;
            selectedCard.startAnimation(jumpAnim);
            
            if (selectedChildId != null) {
                childProfileRepository.addPoints(selectedChildId, 2);
            }

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "praise_id");
            if (tts != null) {
                tts.speak("Đúng rồi! Bé giỏi quá!", TextToSpeech.QUEUE_FLUSH, params, "praise_id");
            }
            Toast.makeText(this, "Chính xác! 🥳", Toast.LENGTH_SHORT).show();
        } else {
            if (isAssignmentMode) {
                setCardsEnabled(false);
                if (tts != null) {
                    tts.speak("Chưa đúng rồi!", TextToSpeech.QUEUE_FLUSH, null, "wrong_once");
                }
                Toast.makeText(this, "Chưa đúng rồi! 😟", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    currentLevel++;
                    if (currentLevel < maxQuestions) {
                        setupNewLevelData();
                        speakQuestion();
                    } else {
                        finishGame();
                    }
                }, 1500);
            } else {
                setCardsEnabled(false);
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wrong_id");
                if (tts != null) {
                    tts.speak("Chưa đúng rồi, bé chọn lại nhé!", TextToSpeech.QUEUE_FLUSH, params, "wrong_id");
                }
                Toast.makeText(this, "Thử lại nào bé yêu! 😟", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setCardsEnabled(boolean enabled) {
        card1.setEnabled(enabled); card2.setEnabled(enabled); card3.setEnabled(enabled);
        card1.setAlpha(enabled ? 1.0f : 0.6f);
        card2.setAlpha(enabled ? 1.0f : 0.6f);
        card3.setAlpha(enabled ? 1.0f : 0.6f);
    }

    private void finishGame() {
        if (isAssignmentMode && assignmentId != null && selectedChildId != null) {
            updateAssignment();
        }
        Toast.makeText(this, "Hoàn thành! Tổng điểm: " + totalScore, Toast.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1000);
    }

    private void updateAssignment() {
        Map<String, Object> submission = new HashMap<>();
        submission.put("status", "submitted");
        submission.put("score", totalScore);
        submission.put("completedAt", new java.util.Date());
        
        FirebaseFirestore.getInstance().collection("assignment_submissions")
                .whereEqualTo("childId", selectedChildId)
                .whereEqualTo("assignmentId", assignmentId)
                .get().addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        snap.getDocuments().get(0).getReference().update(submission);
                    } else {
                        submission.put("childId", selectedChildId);
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
        super.onDestroy();
    }
}
