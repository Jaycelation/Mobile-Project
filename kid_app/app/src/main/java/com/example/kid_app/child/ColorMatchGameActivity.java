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
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.repository.ChildProfileRepository;

import java.util.Locale;

public class ColorMatchGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    
    private View card1, card2, card3;
    private View view1, view2, view3;
    private TextView tvInstruction;
    
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId;

    private int currentLevel = 0;
    private String[] colorNames = {"màu đỏ", "màu xanh lá", "màu vàng", "màu xanh dương", "màu cam", "màu tím"};
    private String[] colorHexs = {"#FF5252", "#4CAF50", "#FFEB3B", "#2196F3", "#FF9800", "#9C27B0"};
    
    private String[][] optionsHexs = {
            {"#4CAF50", "#FF5252", "#2196F3"},
            {"#4CAF50", "#FFEB3B", "#FF9800"},
            {"#2196F3", "#9C27B0", "#FFEB3B"},
            {"#2196F3", "#FF5252", "#4CAF50"},
            {"#FF9800", "#9C27B0", "#4CAF50"},
            {"#FF5252", "#9C27B0", "#2196F3"}
    };
    
    private int[] correctIndexes = {1, 0, 2, 0, 0, 1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_match_game);

        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        setCardsEnabled(false);
        loadLevelData(0);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setCardsEnabled(false));
                    }
                    @Override
                    public void onDone(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if ("question_id".equals(utteranceId) || "wrong_id".equals(utteranceId)) {
                                setCardsEnabled(true);
                            } else if ("praise_id".equals(utteranceId)) {
                                new Handler().postDelayed(() -> nextLevel(), 1000);
                            }
                        });
                    }
                    @Override
                    public void onError(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setCardsEnabled(true));
                    }
                });
                speakQuestion(0);
            } else {
                setCardsEnabled(true);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initViews() {
        card1 = findViewById(R.id.card_color_1);
        card2 = findViewById(R.id.card_color_2);
        card3 = findViewById(R.id.card_color_3);
        view1 = findViewById(R.id.view_color_1);
        view2 = findViewById(R.id.view_color_2);
        view3 = findViewById(R.id.view_color_3);
        tvInstruction = findViewById(R.id.tv_instruction);

        card1.setOnClickListener(v -> handleAnswer(0, card1));
        card2.setOnClickListener(v -> handleAnswer(1, card2));
        card3.setOnClickListener(v -> handleAnswer(2, card3));
    }

    private void loadLevelData(int level) {
        if (level >= colorNames.length) return;
        tvInstruction.setText("Hãy chạm vào " + colorNames[level]);
        view1.setBackgroundColor(Color.parseColor(optionsHexs[level][0]));
        view2.setBackgroundColor(Color.parseColor(optionsHexs[level][1]));
        view3.setBackgroundColor(Color.parseColor(optionsHexs[level][2]));
    }

    private void speakQuestion(int level) {
        if (level >= colorNames.length) return;
        String text = "Bé ơi, hãy chạm vào " + colorNames[level];
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "question_id");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "question_id");
    }

    private void handleAnswer(int selectedIndex, View selectedCard) {
        if (selectedIndex == correctIndexes[currentLevel]) {
            setCardsEnabled(false);
            selectedCard.startAnimation(jumpAnim);
            
            if (selectedChildId != null) {
                childProfileRepository.addPoints(selectedChildId, 10);
            }

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "praise_id");
            tts.speak("Đúng rồi! Bé giỏi quá!", TextToSpeech.QUEUE_FLUSH, params, "praise_id");
            Toast.makeText(this, "Chính xác! 🥳", Toast.LENGTH_SHORT).show();
            
            currentLevel++;
        } else {
            setCardsEnabled(false);
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wrong_id");
            tts.speak("Chưa đúng rồi, bé chọn lại nhé!", TextToSpeech.QUEUE_FLUSH, params, "wrong_id");
            Toast.makeText(this, "Thử lại nào bé yêu! 😟", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextLevel() {
        if (currentLevel < colorNames.length) {
            loadLevelData(currentLevel);
            speakQuestion(currentLevel);
        } else {
            Toast.makeText(this, "Bé đã hoàn thành trò chơi! 🌟", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setCardsEnabled(boolean enabled) {
        card1.setEnabled(enabled); card2.setEnabled(enabled); card3.setEnabled(enabled);
        card1.setAlpha(enabled ? 1.0f : 0.6f);
        card2.setAlpha(enabled ? 1.0f : 0.6f);
        card3.setAlpha(enabled ? 1.0f : 0.6f);
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
