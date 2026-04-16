package com.example.kid_app.child;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.DragEvent;
import android.view.MotionEvent;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShadowMatchGameActivity extends BaseActivity {

    private ImageView[] ivFruits = new ImageView[3];
    private ImageView[] ivShadows = new ImageView[3];
    private TextView tvInstruction;
    private TextToSpeech tts;
    private Animation jumpAnim;
    
    private int totalCorrectMatches = 0;
    private int matchesInCurrentRound = 0;
    private boolean isInteractionEnabled = false;
    
    private String selectedChildId, assignmentId;
    private boolean isAssignmentMode = false;
    private ChildProfileRepository childProfileRepository;

    private static class FruitItem {
        int resId;
        String name;
        FruitItem(int resId, String name) { this.resId = resId; this.name = name; }
    }

    private List<FruitItem> allFruits = new ArrayList<>();
    private List<FruitItem> currentLevelFruits = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow_match_game);

        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        
        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        isAssignmentMode = getIntent().getBooleanExtra("isAssignmentMode", false);
        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);

        initData();
        initViews();
        generateNewLevel();
        initTTS();
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initData() {
        allFruits.clear();
        allFruits.add(new FruitItem(R.drawable.tao_xoa_nen, "apple"));
        allFruits.add(new FruitItem(R.drawable.qua_le_xoa_nen, "pear"));
        allFruits.add(new FruitItem(R.drawable.dua_xoa_nen, "pineapple"));
        allFruits.add(new FruitItem(R.drawable.cam_xoa_nen, "orange"));
        allFruits.add(new FruitItem(R.drawable.chuoi, "banana"));
        allFruits.add(new FruitItem(R.drawable.dua_hau_xoa_nen, "watermelon"));
    }

    private void initViews() {
        ivFruits[0] = findViewById(R.id.iv_fruit_pear);
        ivFruits[1] = findViewById(R.id.iv_fruit_apple);
        ivFruits[2] = findViewById(R.id.iv_fruit_pineapple);

        ivShadows[0] = findViewById(R.id.iv_shadow_pineapple);
        ivShadows[1] = findViewById(R.id.iv_shadow_apple);
        ivShadows[2] = findViewById(R.id.iv_shadow_pear);
        
        tvInstruction = findViewById(R.id.tv_instruction);
    }

    private void generateNewLevel() {
        matchesInCurrentRound = 0;
        setInteractionEnabled(false);
        
        Collections.shuffle(allFruits);
        currentLevelFruits.clear();
        for (int i = 0; i < 3; i++) {
            currentLevelFruits.add(allFruits.get(i));
        }

        List<Integer> fruitPos = new ArrayList<>();
        List<Integer> shadowPos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            fruitPos.add(i);
            shadowPos.add(i);
        }
        Collections.shuffle(fruitPos);
        Collections.shuffle(shadowPos);

        for (int i = 0; i < 3; i++) {
            FruitItem fruit = currentLevelFruits.get(i);
            
            ImageView ivFruit = ivFruits[fruitPos.get(i)];
            ivFruit.setImageResource(fruit.resId);
            ivFruit.setTag(fruit.name);
            ivFruit.setVisibility(View.VISIBLE);
            ivFruit.setAlpha(1.0f);
            setupDragSource(ivFruit);

            ImageView ivShadow = ivShadows[shadowPos.get(i)];
            ivShadow.setImageResource(fruit.resId);
            ivShadow.setTag(fruit.name);
            // Áp dụng bóng đen mờ
            ivShadow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivShadow.setImageTintList(ColorStateList.valueOf(Color.BLACK));
            }
            ivShadow.setAlpha(0.25f);
            ivShadow.setOnDragListener(new ShadowDragListener(fruit.name));
        }
    }

    private void setupDragSource(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && isInteractionEnabled) {
                ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
                ClipData dragData = new ClipData((CharSequence) v.getTag(),
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);
                v.startDragAndDrop(dragData, myShadow, null, 0);
                v.setVisibility(View.INVISIBLE); 
                return true;
            }
            return false;
        });
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> setInteractionEnabled(false));
                    }
                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            if ("praise_id".equals(utteranceId)) {
                                if (matchesInCurrentRound >= 3) {
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        generateNewLevel();
                                        speakQuestion();
                                    }, 1000);
                                } else {
                                    setInteractionEnabled(true);
                                }
                            } else {
                                setInteractionEnabled(true);
                            }
                        });
                    }
                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> setInteractionEnabled(true));
                    }
                });
                speakQuestion();
            }
        });
    }

    private void speakQuestion() {
        speak("Bé hãy kéo hình vào đúng bóng nhé!", "question_id");
    }

    private void setInteractionEnabled(boolean enabled) {
        this.isInteractionEnabled = enabled;
        float alpha = enabled ? 1.0f : 0.6f;
        for (ImageView iv : ivFruits) {
            if (iv.getVisibility() == View.VISIBLE) {
                iv.setAlpha(alpha);
            }
        }
    }

    private class ShadowDragListener implements View.OnDragListener {
        private String targetTag;
        public ShadowDragListener(String tag) { this.targetTag = tag; }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DROP:
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String draggedTag = item.getText().toString();
                    
                    if (draggedTag.equals(targetTag)) {
                        handleCorrectMatch((ImageView) v);
                        return true;
                    } else {
                        handleWrongMatch();
                        return false;
                    }
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        resetFruitVisibilityAfterDrag();
                    }
                    return true;
            }
            return false;
        }
    }

    private void handleCorrectMatch(ImageView shadowView) {
        setInteractionEnabled(false);
        matchesInCurrentRound++;
        totalCorrectMatches++;
        
        if (selectedChildId != null) {
            childProfileRepository.addPoints(selectedChildId, 10);
        }
        
        // HIỆN MÀU THẬT - XOÁ BÓNG ĐEN
        shadowView.clearColorFilter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shadowView.setImageTintList(null);
        }
        shadowView.setAlpha(1.0f);
        shadowView.startAnimation(jumpAnim);
        
        Toast.makeText(this, "Chính xác! 🥳", Toast.LENGTH_SHORT).show();
        speak("Đúng rồi! Bé giỏi quá!", "praise_id");
    }

    private void handleWrongMatch() {
        setInteractionEnabled(false);
        Toast.makeText(this, "Chưa đúng rồi bé ơi! 😟", Toast.LENGTH_SHORT).show();
        speak("Chưa đúng rồi, bé kéo lại nhé!", "wrong_id");
    }

    private void resetFruitVisibilityAfterDrag() {
        // Hiện lại những quả chưa được ghép đúng ở hàng dưới
        for (ImageView ivFruit : ivFruits) {
            String tag = (String) ivFruit.getTag();
            boolean isMatched = false;
            for (ImageView ivShadow : ivShadows) {
                if (tag.equals(ivShadow.getTag()) && ivShadow.getColorFilter() == null) {
                    isMatched = true;
                    break;
                }
            }
            if (!isMatched) {
                ivFruit.setVisibility(View.VISIBLE);
            }
        }
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
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
