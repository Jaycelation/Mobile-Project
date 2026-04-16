package com.example.kid_app.child;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ActivityAttempt;
import com.example.kid_app.data.model.QuizQuestion;
import com.example.kid_app.data.repository.ActivityAttemptRepository;
import com.example.kid_app.data.repository.ChildProfileRepository;
import com.example.kid_app.data.repository.ContentRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QuizPlayActivity extends BaseActivity {

    private View layoutQuestionView, layoutFeedbackView;
    private TextView tvProgress, tvQuestion, tvScore, tvQuizTimer;
    private ProgressBar pbQuiz;
    private Button[] optionButtons = new Button[4];
    private android.os.CountDownTimer countDownTimer;
    private final long TIME_LIMIT_MS = 15000;

    private TextView tvFeedbackEmoji, tvFeedbackTitle, tvExplanationText;
    private Button btnRetry, btnNextQuestion;

    private ContentRepository contentRepository;
    private ActivityAttemptRepository attemptRepository;
    private ChildProfileRepository childProfileRepository;

    private String childId, contentId, attemptId, assignmentId;
    private List<QuizQuestion> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private long quizStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_play);

        // Lấy childId từ Intent hoặc SharedPreferences để tránh crash
        childId = getIntent().getStringExtra(AppConstants.KEY_CHILD_ID);
        if (childId == null) {
            SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
            childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        }
        contentId = getIntent().getStringExtra(AppConstants.KEY_CONTENT_ID);
        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);
        
        if (childId == null || contentId == null) {
            android.widget.Toast.makeText(this, "Lỗi dữ liệu bài học!", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        contentRepository = new ContentRepository();
        attemptRepository = new ActivityAttemptRepository();
        childProfileRepository = new ChildProfileRepository();

        bindViews();
        
        if (childId == null) {
            Toast.makeText(this, "Lỗi: Không xác định được hồ sơ bé!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        loadQuestions();
    }

    private void bindViews() {
        layoutQuestionView = findViewById(R.id.layout_question_view);
        layoutFeedbackView = findViewById(R.id.layout_feedback_view);

        tvProgress = findViewById(R.id.tv_progress);
        tvQuestion = findViewById(R.id.tv_question);
        tvScore = findViewById(R.id.tv_score);
        tvQuizTimer = findViewById(R.id.tv_quiz_timer);
        pbQuiz = findViewById(R.id.pb_quiz);

        optionButtons[0] = findViewById(R.id.btn_option_1);
        optionButtons[1] = findViewById(R.id.btn_option_2);
        optionButtons[2] = findViewById(R.id.btn_option_3);
        optionButtons[3] = findViewById(R.id.btn_option_4);

        tvFeedbackEmoji  = findViewById(R.id.tv_feedback_emoji);
        tvFeedbackTitle  = findViewById(R.id.tv_feedback_title);
        tvExplanationText = findViewById(R.id.tv_explanation_text);
        btnRetry         = findViewById(R.id.btn_retry);
        btnNextQuestion  = findViewById(R.id.btn_next_question);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        if (layoutQuestionView != null) layoutQuestionView.setVisibility(View.VISIBLE);
        if (layoutFeedbackView != null) layoutFeedbackView.setVisibility(View.GONE);
    }

    private void loadQuestions() {
        if (contentId == null) {
            Toast.makeText(this, "Lỗi tải nội dung!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        contentRepository.getQuizQuestions(contentId)
                .addOnSuccessListener(querySnapshot -> {
                    questions = DocumentMapper.listQuizQuestions(querySnapshot);
                    if (questions == null || questions.isEmpty()) {
                        loadSampleQuestions();
                    }
                    startQuiz();
                })
                .addOnFailureListener(e -> {
                    loadSampleQuestions();
                    startQuiz();
                });
    }

    private void loadSampleQuestions() {
        questions.clear();
        // Sử dụng Arrays.asList thay cho List.of để tương thích mọi phiên bản Android
        questions.add(new QuizQuestion(contentId, "Con gì kêu Meo Meo?", Arrays.asList("Con Chó", "Con Mèo", "Con Gà", "Con Vịt"), "Con Mèo"));
        questions.add(new QuizQuestion(contentId, "Con gì gáy Ò Ó O?", Arrays.asList("Con Lợn", "Con Trâu", "Con Gà Trống", "Con Khỉ"), "Con Gà Trống"));
        questions.add(new QuizQuestion(contentId, "Con gì có vòi dài?", Arrays.asList("Con Voi", "Con Hươu", "Con Cá", "Con Chim"), "Con Voi"));
    }

    private void startQuiz() {
        quizStartTime = SystemClock.elapsedRealtime();
        currentIndex = 0;
        score = 0;
        
        ActivityAttempt attempt = new ActivityAttempt(childId, contentId, null, AppConstants.SESSION_FREE_PLAY, "quiz");
        attempt.setAssignmentId(assignmentId);
        attemptRepository.startAttempt(childId, attempt).addOnSuccessListener(ref -> attemptId = ref.getId());

        showQuestion();
    }

    private void showQuestion() {
        if (questions == null || currentIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        if (layoutFeedbackView != null) layoutFeedbackView.setVisibility(View.GONE);
        if (layoutQuestionView != null) layoutQuestionView.setVisibility(View.VISIBLE);

        QuizQuestion q = questions.get(currentIndex);
        if (q == null) return;
        
        if (tvQuestion != null) tvQuestion.setText(q.getQuestionText());
        if (tvProgress != null) tvProgress.setText(String.format(Locale.getDefault(), "Câu %d/%d", currentIndex + 1, questions.size()));
        if (tvScore != null) tvScore.setText(String.format(Locale.getDefault(), "⭐ %d", score));
        
        final List<String> opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i] == null) continue;
            optionButtons[i].setEnabled(true);
            if (opts != null && i < opts.size()) {
                final int finalI = i;
                optionButtons[i].setVisibility(View.VISIBLE);
                optionButtons[i].setText(opts.get(i));
                optionButtons[i].setOnClickListener(v -> handleAnswer(opts.get(finalI), q));
            } else {
                optionButtons[i].setVisibility(View.GONE);
            }
        }
        
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new android.os.CountDownTimer(TIME_LIMIT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvQuizTimer != null) {
                    tvQuizTimer.setText("⏳ " + (millisUntilFinished / 1000) + "s");
                }
            }
            @Override
            public void onFinish() {
                if (tvQuizTimer != null) tvQuizTimer.setText("⏳ 0s");
                handleTimeOut();
            }
        }.start();
    }
    
    private void handleTimeOut() {
        if (layoutFeedbackView != null) layoutFeedbackView.setVisibility(View.VISIBLE);
        if (tvFeedbackEmoji != null) tvFeedbackEmoji.setText("⏰");
        if (tvFeedbackTitle != null) {
            tvFeedbackTitle.setText("Hết giờ rồi!");
            tvFeedbackTitle.setTextColor(getResources().getColor(R.color.status_error));
        }
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
        for (Button btn : optionButtons) {
            if (btn != null) btn.setEnabled(false);
        }
        if (btnNextQuestion != null) {
            btnNextQuestion.setOnClickListener(v -> {
                currentIndex++;
                showQuestion();
            });
        }
    }

    private void handleAnswer(String selected, QuizQuestion q) {
        if (countDownTimer != null) countDownTimer.cancel();
        boolean correct = q.isCorrect(selected);
        if (layoutFeedbackView != null) layoutFeedbackView.setVisibility(View.VISIBLE);
        
        if (correct) {
            score++;
            if (tvFeedbackEmoji != null) tvFeedbackEmoji.setText("😊");
            if (tvFeedbackTitle != null) {
                tvFeedbackTitle.setText("Chính xác!");
                tvFeedbackTitle.setTextColor(getResources().getColor(R.color.status_success));
            }
            if (btnRetry != null) btnRetry.setVisibility(View.GONE);
        } else {
            if (tvFeedbackEmoji != null) tvFeedbackEmoji.setText("😰");
            if (tvFeedbackTitle != null) {
                tvFeedbackTitle.setText("Chưa đúng rồi!");
                tvFeedbackTitle.setTextColor(getResources().getColor(R.color.status_error));
            }
            if (btnRetry != null) {
                btnRetry.setVisibility(View.VISIBLE);
                btnRetry.setOnClickListener(v -> layoutFeedbackView.setVisibility(View.GONE));
            }
        }

        if (btnNextQuestion != null) {
            btnNextQuestion.setOnClickListener(v -> {
                currentIndex++;
                showQuestion();
            });
        }
    }

    private void finishQuiz() {
        if (countDownTimer != null) countDownTimer.cancel();
        int duration = (int) ((SystemClock.elapsedRealtime() - quizStartTime) / 1000);
        if (attemptId != null && childId != null) {
            attemptRepository.completeAttempt(childId, attemptId, score, "completed", duration);
        }
        if (childId != null) {
            childProfileRepository.addPoints(childId, score * 10);
        }
        Toast.makeText(this, "Giỏi lắm! Bé đạt " + score + " điểm!", Toast.LENGTH_LONG).show();
        finish();
    }
    
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
