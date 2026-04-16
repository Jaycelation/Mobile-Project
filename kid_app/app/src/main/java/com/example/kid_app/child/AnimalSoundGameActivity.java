package com.example.kid_app.child;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.repository.ChildProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnimalSoundGameActivity extends BaseActivity {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Animation jumpAnim;
    private TextToSpeech tts;
    
    private ImageView ivAnimalImage;
    private View cardAnimalSound;
    
    private int currentIndex = 0;
    private boolean isClickable = false;
    private ChildProfileRepository childProfileRepository;
    private String selectedChildId;

    private static class Animal {
        String name;
        int imageRes;
        int soundRes;
        String prompt;

        Animal(String name, int imageRes, int soundRes, String prompt) {
            this.name = name;
            this.imageRes = imageRes;
            this.soundRes = soundRes;
            this.prompt = prompt;
        }
    }

    private List<Animal> animalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_sound_game);

        childProfileRepository = new ChildProfileRepository();
        selectedChildId = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        initData();
        initTTS();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        cardAnimalSound.setOnClickListener(v -> handleAnimalTouch());
    }

    private void initViews() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        jumpAnim = AnimationUtils.loadAnimation(this, R.anim.jump);
        ivAnimalImage = findViewById(R.id.iv_animal_image);
        cardAnimalSound = findViewById(R.id.card_animal_sound);
        setCardClickable(false);
    }

    private void initData() {
        animalList.clear();
        String simplePrompt = "Bé hãy chạm vào hình nhé!";
        animalList.add(new Animal("Chó", R.drawable.cho, R.raw.dog_bark, simplePrompt));
        animalList.add(new Animal("Mèo", R.drawable.meo, R.raw.meo, simplePrompt));
        animalList.add(new Animal("Vịt", R.drawable.vit, R.raw.vit, simplePrompt));
        animalList.add(new Animal("Bò", R.drawable.bo, R.raw.bo, simplePrompt));
        animalList.add(new Animal("Sư Tử", R.drawable.con_su_tu, R.raw.lion_roar, simplePrompt));
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> setCardClickable(false));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            if ("intro_id".equals(utteranceId) || "prompt_id".equals(utteranceId)) {
                                setCardClickable(true);
                            } else if ("info_id".equals(utteranceId)) {
                                playAnimalSound();
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> setCardClickable(true));
                    }
                });
                updateUI();
                speak(animalList.get(0).prompt, "intro_id");
            }
        });
    }

    private void updateUI() {
        if (currentIndex < animalList.size()) {
            ivAnimalImage.setImageResource(animalList.get(currentIndex).imageRes);
        }
    }

    private void handleAnimalTouch() {
        if (!isClickable) return;
        setCardClickable(false);

        cardAnimalSound.startAnimation(jumpAnim);
        vibrateDevice();

        String text = "Đây là bạn " + animalList.get(currentIndex).name;
        speak(text, "info_id");
    }

    private void playAnimalSound() {
        stopSound();
        try {
            mediaPlayer = MediaPlayer.create(this, animalList.get(currentIndex).soundRes);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    new Handler(Looper.getMainLooper()).postDelayed(this::moveToNextAnimal, 1000);
                });
                mediaPlayer.start();
            } else {
                moveToNextAnimal();
            }
        } catch (Exception e) {
            moveToNextAnimal();
        }
    }

    private void moveToNextAnimal() {
        currentIndex++;
        if (currentIndex < animalList.size()) {
            updateUI();
            speak(animalList.get(currentIndex).prompt, "prompt_id");
        } else {
            finishGame();
        }
    }

    private void finishGame() {
        if (selectedChildId != null) {
            childProfileRepository.addPoints(selectedChildId, 10);
        }
        Toast.makeText(this, "Bé thật giỏi! Đã khám phá hết rồi! 🌟", Toast.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
    }

    private void setCardClickable(boolean clickable) {
        isClickable = clickable;
        cardAnimalSound.setClickable(clickable);
        cardAnimalSound.setEnabled(clickable);
        cardAnimalSound.setAlpha(clickable ? 1.0f : 0.8f);
    }

    private void speak(String text, String utteranceId) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        stopSound();
        super.onDestroy();
    }
}
