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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.repository.ChildProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ObjectGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    private Vibrator vibrator;
    
    private ImageView ivObjectDisplay;
    private TextView tvObjectName, tvProgressInfo;
    private View cardObjectMain;

    private int currentIndex = 0;
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId;

    private static class LearningObject {
        String name;
        int imageRes;
        LearningObject(String name, int imageRes) { this.name = name; this.imageRes = imageRes; }
    }

    private List<LearningObject> objectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_game);

        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        initData();
        updateUI();

        // Gán sự kiện trước
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        cardObjectMain.setOnClickListener(v -> handleObjectClick());

        // 🔥 Khóa ngay lập tức sau khi gán listener (vì setOnClickListener sẽ tự động enable lại)
        setObjectClickable(false);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setObjectClickable(false));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if ("intro_id".equals(utteranceId) || "next_prompt_id".equals(utteranceId)) {
                                setObjectClickable(true);
                            } else if ("info_id".equals(utteranceId)) {
                                new Handler().postDelayed(() -> moveToNextObject(), 1000);
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setObjectClickable(true));
                    }
                });

                // Bắt đầu đọc hướng dẫn
                speakIntro();
            } else {
                setObjectClickable(true);
            }
        });
    }

    private void initViews() {
        ivObjectDisplay = findViewById(R.id.iv_object_display);
        tvObjectName = findViewById(R.id.tv_object_name);
        tvProgressInfo = findViewById(R.id.tv_progress_info);
        cardObjectMain = findViewById(R.id.card_object_main);
        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initData() {
        objectList.clear();
        objectList.add(new LearningObject("Quả Táo", R.drawable.tao_xoa_nen));
        objectList.add(new LearningObject("Quả Lê", R.drawable.qua_le_xoa_nen));
        objectList.add(new LearningObject("Quả Dứa", R.drawable.dua_xoa_nen));
        objectList.add(new LearningObject("Quả Cam", R.drawable.cam_xoa_nen));
        objectList.add(new LearningObject("Quả Chuối", R.drawable.chuoi));
        objectList.add(new LearningObject("Quả Dưa Hấu", R.drawable.dua_hau_xoa_nen));
    }

    private void updateUI() {
        if (currentIndex >= objectList.size()) {
            finishGame();
            return;
        }

        LearningObject current = objectList.get(currentIndex);
        ivObjectDisplay.setImageResource(current.imageRes);
        tvObjectName.setText(current.name);
        tvProgressInfo.setText("Hình " + (currentIndex + 1) + " / " + objectList.size());
    }

    private void speakIntro() {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "intro_id");
        tts.speak("Bé hãy chạm vào hình để khám phá các loại quả nhé!", TextToSpeech.QUEUE_FLUSH, params, "intro_id");
    }

    private void handleObjectClick() {
        // Vô hiệu hóa ngay khi bấm để tránh bấm liên tục
        setObjectClickable(false);
        cardObjectMain.startAnimation(jumpAnim);
        vibrateDevice();

        String text = "Đây là " + objectList.get(currentIndex).name;
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "info_id");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "info_id");
    }

    private void moveToNextObject() {
        currentIndex++;
        if (currentIndex < objectList.size()) {
            updateUI();
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "next_prompt_id");
            tts.speak("Bé khám phá tiếp nào!", TextToSpeech.QUEUE_FLUSH, params, "next_prompt_id");
        } else {
            finishGame();
        }
    }

    private void setObjectClickable(boolean clickable) {
        cardObjectMain.setClickable(clickable);
        cardObjectMain.setAlpha(clickable ? 1.0f : 0.6f);
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
            childProfileRepository.addPoints(selectedChildId, 20);
            childProfileRepository.completeLesson(selectedChildId, "learning_fruit");
        }
        Toast.makeText(this, "Tuyệt quá! Bé đã khám phá xong tất cả các loại quả rồi! 🌟", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(this::finish, 1500);
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
