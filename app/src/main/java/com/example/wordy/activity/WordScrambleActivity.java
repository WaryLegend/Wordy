package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.interfaces.JsonVocabularyLoader;
import com.example.wordy.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WordScrambleActivity extends AppCompatActivity {
    private TextView tvTimer, tvScore, tvVietnamese, tvScrambledWord, tvFeedback;
    private EditText etAnswer;
    private Button btnSubmit, btnPlayAgain, btnStartPause;
    private List<Word> words = new ArrayList<>();
    private int score = 0;
    private long timeLeft;
    private CountDownTimer timer;
    private Vibrator vibrator;
    private String currentTopicName;
    private Word currentWord;
    private boolean isGameEnded = false;
    private boolean isGameStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_scramble);

        // Initialize views
        tvTimer = findViewById(R.id.tv_timer);
        tvScore = findViewById(R.id.tv_score);
        tvVietnamese = findViewById(R.id.tv_vietnamese);
        tvScrambledWord = findViewById(R.id.tv_scrambled_word);
        tvFeedback = findViewById(R.id.tv_feedback);
        etAnswer = findViewById(R.id.enter_answer);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnStartPause = findViewById(R.id.btn_start_pause);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Get topicId and topicName from Intent
        int topicId = getIntent().getIntExtra("topicId", -1);
        currentTopicName = getIntent().getStringExtra("topicName");

        // Set up back button
        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        // Change title & right button
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText(currentTopicName);
        headerLayout.findViewById(R.id.btnIconRight).setVisibility(View.GONE);

        // Load vocabulary
        words = JsonVocabularyLoader.loadWordsFromJson(this, "voca.json", topicId);
        if (words.isEmpty()) {
            Toast.makeText(this, "Error: Could not load vocabulary!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Handle Enter key on keyboard to submit answer
        etAnswer.setOnEditorActionListener((v, actionId, event) -> {
            if (!isGameEnded && isGameStarted && (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN))) {
                onSubmitClick();
                return true;
            }
            return false;
        });

        // Button listeners
        btnSubmit.setOnClickListener(v -> onSubmitClick());
        btnPlayAgain.setOnClickListener(v -> startGame());
        btnStartPause.setOnClickListener(v -> toggleGameState());

        initializeGame();
    }

    private void initializeGame() {
        score = 0;
        timeLeft = 60000; // 60 seconds
        isGameEnded = false;
        isGameStarted = false;
        tvScore.setText("Score: 0");
        tvTimer.setText("Time: 60");
        tvFeedback.setVisibility(View.GONE);
        btnPlayAgain.setVisibility(View.GONE);
        btnStartPause.setVisibility(View.VISIBLE);
        btnStartPause.setText("Start Game");
        etAnswer.setText("");
        etAnswer.setEnabled(false);
        btnSubmit.setEnabled(false);
        tvVietnamese.setVisibility(View.INVISIBLE);
        tvScrambledWord.setVisibility(View.INVISIBLE);
        nextWord();
    }

    private void startGame() {
        initializeGame();
        toggleGameState(); // Start the game immediately
    }

    private void toggleGameState() {
        if (isGameEnded) return;

        if (!isGameStarted) {
            // Start the game
            isGameStarted = true;
            btnStartPause.setText("Pause Game");
            tvVietnamese.setVisibility(View.VISIBLE);
            tvScrambledWord.setVisibility(View.VISIBLE);
            etAnswer.setEnabled(true);
            btnSubmit.setEnabled(true);
            startTimer();
        } else {
            // Pause the game
            isGameStarted = false;
            btnStartPause.setText("Start Game");
            tvVietnamese.setVisibility(View.INVISIBLE);
            tvScrambledWord.setVisibility(View.INVISIBLE);
            etAnswer.setEnabled(false);
            btnSubmit.setEnabled(false);
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    private void nextWord() {
        if (isGameEnded) return;

        tvFeedback.setVisibility(View.GONE);
        etAnswer.setText("");

        // Choose random word
        Random random = new Random();
        currentWord = words.get(random.nextInt(words.size()));
        String english = currentWord.getWord().toLowerCase();

        // Scramble word
        String scrambledWord = scrambleWord(english);

        // Update UI
        tvVietnamese.setText(currentWord.getMeaning());
        tvScrambledWord.setText(scrambledWord.toUpperCase()); // Display in uppercase for clarity
    }

    private String scrambleWord(String word) {
        List<Character> chars = new ArrayList<>();
        for (char c : word.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        StringBuilder scrambled = new StringBuilder();
        for (char c : chars) {
            scrambled.append(c);
        }
        // Ensure scrambled word is different from original
        while (scrambled.toString().equals(word) && word.length() > 1) {
            Collections.shuffle(chars);
            scrambled = new StringBuilder();
            for (char c : chars) {
                scrambled.append(c);
            }
        }
        return scrambled.toString();
    }

    private void onSubmitClick() {
        if (isGameEnded || !isGameStarted) return;

        String answer = etAnswer.getText().toString().trim().toLowerCase();
        if (answer.isEmpty()) {
            tvFeedback.setText("Please enter a word!");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvFeedback.setVisibility(View.VISIBLE);
            tvFeedback.postDelayed(() -> {
                if (!isGameEnded) tvFeedback.setVisibility(View.GONE);
            }, 800);
            return;
        }

        if (answer.equals(currentWord.getWord().toLowerCase())) {
            score += 10;
            tvScore.setText("Score: " + score);
            tvFeedback.setText("Correct!");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvFeedback.setVisibility(View.VISIBLE);
            etAnswer.setText("");
            tvScrambledWord.postDelayed(this::nextWord, 1000);
        } else {
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(200);
            }
            timeLeft = Math.max(0, timeLeft - 1000);
            tvTimer.setText("Time: " + (timeLeft / 1000));
            tvFeedback.setText("Wrong! Try again.");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvFeedback.setVisibility(View.VISIBLE);
            tvFeedback.postDelayed(() -> {
                if (!isGameEnded) tvFeedback.setVisibility(View.GONE);
            }, 800);
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                tvTimer.setText("Time: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void endGame() {
        if (isGameEnded) return;
        isGameEnded = true;

        if (timer != null) {
            timer.cancel();
        }
        tvTimer.setText("Time: 0");
        tvFeedback.setVisibility(View.GONE);
        btnPlayAgain.setVisibility(View.VISIBLE);
        btnStartPause.setVisibility(View.GONE);
        etAnswer.setEnabled(false);
        btnSubmit.setEnabled(false);
        tvScrambledWord.setText("");
        tvVietnamese.setText("Game Over! Score: " + score);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}