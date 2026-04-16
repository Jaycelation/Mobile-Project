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

public class ShapeGameActivity extends BaseActivity {

    private TextToSpeech tts;
    private Animation jumpAnim;
    private Vibrator vibrator;
    
    private ImageView ivShapeDisplay;
    private TextView tvShapeName, tvProgressInfo;
    private View cardShapeMain;

    private int currentIndex = 0;
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId;

    private static class ShapeObject {
        String name;
        int imageRes;
        ShapeObject(String name, int imageRes) { this.name = name; this.imageRes = imageRes; }
    }

    private List<ShapeObject> shapeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_game);

        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        initData();
        updateUI();

        // KHÓA TUYỆT ĐỐI NGAY TỪ ĐẦU
        setShapeClickable(false);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setShapeClickable(false));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if ("intro_id".equals(utteranceId) || "next_shape_prompt_id".equals(utteranceId)) {
                                setShapeClickable(true);
                            } else if ("shape_info_id".equals(utteranceId)) {
                                new Handler().postDelayed(() -> moveToNextShape(), 1000);
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        new Handler(Looper.getMainLooper()).post(() -> setShapeClickable(true));
                    }
                });
                speakIntro();
            } else {
                setShapeClickable(true);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        cardShapeMain.setOnClickListener(v -> handleShapeClick());
    }

    private void initViews() {
        ivShapeDisplay = findViewById(R.id.iv_shape_display);
        tvShapeName = findViewById(R.id.tv_shape_name);
        tvProgressInfo = findViewById(R.id.tv_progress_info);
        cardShapeMain = findViewById(R.id.card_shape_main);
        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initData() {
        shapeList.clear();
        shapeList.add(new ShapeObject("Hình Tròn", R.drawable.shape_red_circle));
        shapeList.add(new ShapeObject("Hình Vuông", R.drawable.shape_blue_square));
        shapeList.add(new ShapeObject("Hình Tam Giác", R.drawable.shape_blue_triangle));
        // ĐÃ SỬA: Tạm thời xóa hình Chữ Nhật theo yêu cầu người dùng
        // shapeList.add(new ShapeObject("Hình Chữ Nhật", R.drawable.dua_hau_xoa_nen));
    }

    private void updateUI() {
        if (currentIndex >= shapeList.size()) {
            finishGame();
            return;
        }

        ShapeObject current = shapeList.get(currentIndex);
        ivShapeDisplay.setImageResource(current.imageRes);
        tvShapeName.setText(current.name);
        tvProgressInfo.setText("Hình " + (currentIndex + 1) + " / " + shapeList.size());
    }

    private void speakIntro() {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "intro_id");
        tts.speak("Bé hãy chạm vào hình để khám phá nhé!", TextToSpeech.QUEUE_FLUSH, params, "intro_id");
    }

    private void handleShapeClick() {
        setShapeClickable(false);
        cardShapeMain.startAnimation(jumpAnim);
        vibrateDevice();

        String text = "Đây là " + shapeList.get(currentIndex).name;
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "shape_info_id");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "shape_info_id");
    }

    private void moveToNextShape() {
        currentIndex++;
        if (currentIndex < shapeList.size()) {
            updateUI();
            setShapeClickable(false);
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "next_shape_prompt_id");
            tts.speak("Tiếp theo nào!", TextToSpeech.QUEUE_FLUSH, params, "next_shape_prompt_id");
        } else {
            finishGame();
        }
    }

    private void setShapeClickable(boolean clickable) {
        cardShapeMain.setClickable(clickable);
        cardShapeMain.setEnabled(clickable); // KHÓA CỨNG VẬT LÝ
        cardShapeMain.setAlpha(clickable ? 1.0f : 0.6f);
    }

    private void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(80);
        }
    }

    private void finishGame() {
        if (selectedChildId != null) {
            childProfileRepository.addPoints(selectedChildId, 10);
        }
        Toast.makeText(this, "Giỏi quá! Bé đã học xong! 🌟", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
