package com.example.kid_app.child;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import com.example.kid_app.data.model.ActivityAttempt;
import com.example.kid_app.data.repository.ActivityAttemptRepository;
import com.example.kid_app.data.repository.ChildProfileRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FastEyeGameActivity extends BaseActivity {

    private static final String TAG = "FastEyeGame";
    
    private TextView tvTimer, tvTargetIcon, tvLevel;
    private View layoutFeedbackView;
    private GridLayout gameGrid;
    private Button btnNext;

    private int seconds = 20;
    private int foundCount = 0;
    private int currentLevel = 1;
    private final int targetRequired = 3;
    private String targetEmoji = "🐸";
    
    private final Handler handler = new Handler();
    private long startTime;
    private String childId, attemptId;
    private ActivityAttemptRepository attemptRepository;
    private ChildProfileRepository childProfileRepository;

    private TextToSpeech tts;
    private boolean isInteractionEnabled = false;

    private final List<String> allEmojis = Arrays.asList("🐸", "🐢", "🐍", "🐊", "🦎", "🐙", "🐚", "🦀", "🐠", "🦁", "🐘", "🦒", "🦊", "🐱", "🐶");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_fast_eye_game);

            attemptRepository = new ActivityAttemptRepository();
            childProfileRepository = new ChildProfileRepository();
            
            SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
            childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

            bindViews();
            
            // NẠP DỮ LIỆU NGAY LẬP TỨC ĐỂ TRÁNH MÀN TRẮNG
            setupLevelData();
            setGridEnabled(false);

            initTTS();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void bindViews() {
        tvTimer = findViewById(R.id.tv_timer);
        tvTargetIcon = findViewById(R.id.tv_target_icon);
        tvLevel = findViewById(R.id.tv_level);
        gameGrid = findViewById(R.id.game_grid);
        layoutFeedbackView = findViewById(R.id.layout_feedback_view);
        btnNext = findViewById(R.id.btn_next);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        btnNext.setOnClickListener(v -> {
            currentLevel++;
            startNewRound();
        });
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> setGridEnabled(false));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            if ("question_id".equals(utteranceId)) {
                                setGridEnabled(true);
                                startTimer();
                            } else if ("praise_id".equals(utteranceId)) {
                                if (layoutFeedbackView != null) {
                                    layoutFeedbackView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> setGridEnabled(true));
                    }
                });
                speakQuestion();
            }
        });
    }

    private void setupLevelData() {
        if (layoutFeedbackView != null) layoutFeedbackView.setVisibility(View.GONE);
        foundCount = 0;
        seconds = 20;
        if (tvTimer != null) tvTimer.setText("00:20");
        tvLevel.setText("Màn " + currentLevel);
        
        targetEmoji = allEmojis.get(new Random().nextInt(allEmojis.size()));
        if (tvTargetIcon != null) tvTargetIcon.setText(targetEmoji);
        
        setupGrid();
    }

    private void startNewRound() {
        setupLevelData();
        setGridEnabled(false);
        handler.removeCallbacksAndMessages(null);
        startTime = SystemClock.elapsedRealtime();
        
        speakQuestion();

        if (childId != null) {
            ActivityAttempt attempt = new ActivityAttempt(childId, "game_fast_eye_01", null, AppConstants.SESSION_FREE_PLAY, "game");
            attemptRepository.startAttempt(childId, attempt).addOnSuccessListener(ref -> attemptId = ref.getId());
        }
    }

    private void speakQuestion() {
        speak("Bé hãy chạm nhanh vào hình giống bạn ở trên nhé!", "question_id");
    }

    private void setupGrid() {
        if (gameGrid == null) return;
        
        List<String> items = new ArrayList<>();
        for (int i = 0; i < targetRequired; i++) items.add(targetEmoji);
        
        while (items.size() < 9) {
            String randEmoji = allEmojis.get(new Random().nextInt(allEmojis.size()));
            if (!randEmoji.equals(targetEmoji)) items.add(randEmoji);
        }
        
        Collections.shuffle(items);

        for (int i = 0; i < 9; i++) {
            final int index = i;
            final String currentEmoji = items.get(index);
            
            View view = gameGrid.getChildAt(index);
            if (view instanceof FrameLayout) {
                FrameLayout frame = (FrameLayout) view;
                frame.setAlpha(1.0f);
                frame.setEnabled(false);
                
                if (frame.getChildAt(0) instanceof TextView) {
                    TextView tv = (TextView) frame.getChildAt(0);
                    tv.setText(currentEmoji);
                }
                
                frame.setOnClickListener(v -> {
                    if (currentEmoji.equals(targetEmoji)) {
                        v.setEnabled(false);
                        v.animate().alpha(0.3f).scaleX(0.8f).scaleY(0.8f).setDuration(300).start();
                        foundCount++;
                        checkWin();
                    } else {
                        Toast.makeText(this, "Nhầm rồi bé ơi! ❌", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void setGridEnabled(boolean enabled) {
        this.isInteractionEnabled = enabled;
        for (int i = 0; i < 9; i++) {
            View view = gameGrid.getChildAt(i);
            if (view instanceof FrameLayout) {
                view.setEnabled(enabled);
                view.setAlpha(enabled ? 1.0f : 0.6f);
            }
        }
    }

    private void checkWin() {
        if (foundCount >= targetRequired) {
            handler.removeCallbacksAndMessages(null);
            setGridEnabled(false);
            saveResult(15, "passed");
            speak("Đúng rồi! Bé giỏi quá!", "praise_id");
        }
    }

    private void saveResult(int score, String status) {
        if (childId != null && attemptId != null) {
            int duration = (int) ((SystemClock.elapsedRealtime() - startTime) / 1000);
            attemptRepository.completeAttempt(childId, attemptId, score, status, duration);
            childProfileRepository.addPoints(childId, score);
        }
    }

    private void startTimer() {
        handler.removeCallbacksAndMessages(null);
        startTime = SystemClock.elapsedRealtime(); // Reset lại start time khi timer thực sự chạy
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (seconds > 0) {
                    seconds--;
                    if (tvTimer != null) {
                        tvTimer.setText(String.format(Locale.getDefault(), "00:%02d", seconds));
                    }
                    handler.postDelayed(this, 1000);
                } else {
                    if (!isFinishing()) {
                        Toast.makeText(FastEyeGameActivity.this, "Hết giờ rồi! Thử lại nhé.", Toast.LENGTH_SHORT).show();
                        startNewRound();
                    }
                }
            }
        }, 1000);
    }

    private void speak(String text, String id) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
