package com.example.wordy.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.interfaces.JsonVocabularyLoader;
import com.example.wordy.model.Word;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchingGameActivity extends AppCompatActivity {
    private static final long MAX_BONUS_TIME = 180; // 3 minutes in seconds
    private static final int POINTS_PER_MATCH = 10;
    private static final int BONUS_POINTS_PER_SECOND = 2;
    private static final int MAX_MATCHES = 10;
    private static final int MARGIN_DP = 5;
    private static final int VIBRATION_DURATION = 200;

    private TextView tvTimer, tvScore;
    private LinearLayout gridEnglish, gridVietnamese;
    private Button btnPlayAgain, btnStartPause;
    private List<Word> words;
    private MaterialButton selectedEnglishButton, selectedVietnameseButton;
    private int score, correctMatches, totalMatches;
    private CountUpTimer timer;
    private long secondsElapsed;
    private Vibrator vibrator;
    private boolean isGameEnded, isPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_game);

        View headerLayout = findViewById(R.id.header);
        ((TextView) headerLayout.findViewById(R.id.header_title)).setText("Matching Game");
        headerLayout.findViewById(R.id.btnIconRight).setVisibility(View.GONE);

        tvTimer = findViewById(R.id.tv_timer);
        tvScore = findViewById(R.id.tv_score);
        gridEnglish = findViewById(R.id.grid_english);
        gridVietnamese = findViewById(R.id.grid_vietnamese);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnStartPause = findViewById(R.id.btn_start_pause);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        findViewById(R.id.btnReturn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        words = JsonVocabularyLoader.loadWordsFromJson(this, "voca.json");
        if (words.isEmpty()) {
            Toast.makeText(this, "Error: Could not load vocabulary!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnPlayAgain.setOnClickListener(v -> loadGame());
        btnStartPause.setOnClickListener(v -> toggleStartPause());
        loadGame();
    }

    private void loadGame() {
        resetGameState();

        Collections.shuffle(words);
        List<Word> gameWords = words.subList(0, totalMatches);
        List<String> meanings = new ArrayList<>();
        for (Word word : gameWords) {
            gridEnglish.addView(createButton(word.getWord(), word, true));
            meanings.add(word.getMeaning());
        }
        Collections.shuffle(meanings);
        for (String meaning : meanings) {
            gridVietnamese.addView(createButton(meaning, meaning, false));
        }
        setWordButtonsTextColor(Color.TRANSPARENT); // Initial state: buttons text gone
    }
    private void toggleStartPause() {
        if (isGameEnded) return;

        if (isPaused) {
            btnStartPause.setText("Start Game");
            if (timer != null) timer.stop();
            isPaused = !isPaused;
            setWordButtonsTextColor(Color.TRANSPARENT);
        } else {
            btnStartPause.setText("Pause Game");
            isPaused = !isPaused;
            setWordButtonsTextColor(getResources().getColor(android.R.color.white));
            startTimer();
        }
    }

    private void resetGameState() {
        score = correctMatches = 0;
        secondsElapsed = 0;
        isGameEnded = isPaused = false;
        totalMatches = Math.min(MAX_MATCHES, words.size());
        tvScore.setText("Score: 0");
        tvTimer.setText("Time: 0");
        btnPlayAgain.setVisibility(View.GONE);
        btnStartPause.setVisibility(View.VISIBLE);
        btnStartPause.setText("Start Game");
        gridEnglish.removeAllViews();
        gridVietnamese.removeAllViews();
        selectedEnglishButton = selectedVietnameseButton = null;
        if (timer != null) timer.stop();
    }

    private MaterialButton createButton(String text, Object tag, boolean isEnglish) {
        MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btn.setText(text);
        btn.setTag(tag);

        int margin = (int) getResources().getDisplayMetrics().density * 5; // 5dp margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(margin, margin, margin, margin); // apply to all sides
        btn.setLayoutParams(params);

        btn.setPadding(16, 16, 16, 16);
        btn.setTextColor(getResources().getColor(android.R.color.white));
        btn.setTextSize(20);
        btn.setAllCaps(false);
        btn.setCornerRadius(16);
        btn.setElevation(4);
        btn.setRippleColorResource(android.R.color.white);
        btn.setOnClickListener(v -> {
            if (isGameEnded) return;
            onWordSelected((MaterialButton) v, isEnglish);
        });
        return btn;
    }

    private void onWordSelected(MaterialButton btn, boolean isEnglish) {
        MaterialButton current = isEnglish ? selectedEnglishButton : selectedVietnameseButton;
        if (current != null) restoreButtonState(current);
        if (isEnglish) selectedEnglishButton = btn;
        else selectedVietnameseButton = btn;

        btn.setEnabled(false);
        btn.setTextColor(getResources().getColor(R.color.litePurple));

        if (selectedEnglishButton != null && selectedVietnameseButton != null) checkMatch();
    }

    private void restoreButtonState(MaterialButton btn) {
        if (btn != null) {
            btn.setEnabled(true);
            btn.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void checkMatch() {
        Word word = (Word) selectedEnglishButton.getTag();
        String meaning = (String) selectedVietnameseButton.getTag();

        if (word.getMeaning().equals(meaning)) {
            selectedEnglishButton.setVisibility(View.INVISIBLE);
            selectedVietnameseButton.setVisibility(View.INVISIBLE);
            score += POINTS_PER_MATCH;
            correctMatches++;
            updateScoreDisplay();
            if (correctMatches == totalMatches) endGame();
        } else {
            if (vibrator.hasVibrator()) vibrator.vibrate(VIBRATION_DURATION);
            restoreButtonState(selectedEnglishButton);
            restoreButtonState(selectedVietnameseButton);
        }

        selectedEnglishButton = selectedVietnameseButton = null;
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new CountUpTimer() {
            @Override
            public void onTick(long seconds) {
                secondsElapsed = seconds;
                tvTimer.setText("Time: " + seconds);
            }
        };
        timer.start();
    }

    private void endGame() {
        if (isGameEnded) return;
        isGameEnded = true;

        if (timer != null) timer.stop();
        score += calculateBonusScore();
        updateScoreDisplay();
        btnStartPause.setVisibility(View.GONE);
        btnPlayAgain.setVisibility(View.VISIBLE);
        disableAllButtons(gridEnglish);
        disableAllButtons(gridVietnamese);
    }

    private int calculateBonusScore() {
        return secondsElapsed <= MAX_BONUS_TIME ? (int) ((MAX_BONUS_TIME - secondsElapsed) * BONUS_POINTS_PER_SECOND) : 0;
    }

    private void updateScoreDisplay() {
        tvScore.setText("Score: " + score);
    }

    private void setWordButtonsVisibility(int visibility) {
        for (int i = 0; i < gridEnglish.getChildCount(); i++) {
            gridEnglish.getChildAt(i).setVisibility(visibility);
        }
        for (int i = 0; i < gridVietnamese.getChildCount(); i++) {
            gridVietnamese.getChildAt(i).setVisibility(visibility);
        }
    }

    private void setWordButtonsTextColor(int color) {
        for (int i = 0; i < gridEnglish.getChildCount(); i++) {
            ((MaterialButton) gridEnglish.getChildAt(i)).setTextColor(color);
        }
        for (int i = 0; i < gridVietnamese.getChildCount(); i++) {
            ((MaterialButton) gridVietnamese.getChildAt(i)).setTextColor(color);
        }
    }

    private void disableAllButtons(LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            layout.getChildAt(i).setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.stop();
    }

    private abstract class CountUpTimer {
        private static final long INTERVAL = 1000;
        private Handler handler = new Handler();
        private long startTime;
        private boolean isRunning;

        void start() {
            startTime = System.currentTimeMillis() - secondsElapsed * 1000; // Resume from current time
            isRunning = true;
            handler.postDelayed(this::tick, INTERVAL);
        }

        void stop() {
            isRunning = false;
            handler.removeCallbacksAndMessages(null);
        }

        private void tick() {
            if (!isRunning) return;
            long seconds = (System.currentTimeMillis() - startTime) / 1000;
            onTick(seconds);
            handler.postDelayed(this::tick, INTERVAL);
        }

        abstract void onTick(long seconds);
    }
}