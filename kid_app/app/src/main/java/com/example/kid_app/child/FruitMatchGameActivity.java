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

import java.util.Locale;

public class FruitMatchGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    
    private View card1, card2, card3;
    private ImageView iv1, iv2, iv3;
    private TextView tvInstruction;
    
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId;

    private int currentLevel = 0;
    private String[] fruitNames = {"quả táo", "quả lê", "quả dứa", "quả cam", "quả dưa hấu"};
    
    private int[][] optionsResources = {
            {R.drawable.cam_xoa_nen, R.drawable.tao_xoa_nen, R.drawable.dua_hau_xoa_nen},
            {R.drawable.qua_le_xoa_nen, R.drawable.chuoi, R.drawable.dua_xoa_nen},
            {R.drawable.cam_xoa_nen, R.drawable.dua_hau_xoa_nen, R.drawable.dua_xoa_nen},
            {R.drawable.cam_xoa_nen, R.drawable.tao_xoa_nen, R.drawable.qua_le_xoa_nen},
            {R.drawable.dua_hau_xoa_nen, R.drawable.chuoi, R.drawable.dua_xoa_nen}
    };
    
    private int[] correctIndexes = {1, 0, 2, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_match_game);

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
        card1 = findViewById(R.id.card_fruit_1);
        card2 = findViewById(R.id.card_fruit_2);
        card3 = findViewById(R.id.card_fruit_3);
        iv1 = findViewById(R.id.iv_fruit_1);
        iv2 = findViewById(R.id.iv_fruit_2);
        iv3 = findViewById(R.id.iv_fruit_3);
        tvInstruction = findViewById(R.id.tv_instruction);

        card1.setOnClickListener(v -> handleAnswer(0, card1));
        card2.setOnClickListener(v -> handleAnswer(1, card2));
        card3.setOnClickListener(v -> handleAnswer(2, card3));
    }

    private void loadLevelData(int level) {
        if (level >= fruitNames.length) return;
        tvInstruction.setText("Hãy chạm vào " + fruitNames[level]);
        iv1.setImageResource(optionsResources[level][0]);
        iv2.setImageResource(optionsResources[level][1]);
        iv3.setImageResource(optionsResources[level][2]);
    }

    private void speakQuestion(int level) {
        if (level >= fruitNames.length) return;
        String text = "Bé ơi, hãy chạm vào " + fruitNames[level];
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
        if (currentLevel < fruitNames.length) {
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
