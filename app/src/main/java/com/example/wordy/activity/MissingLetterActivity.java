package com.example.wordy.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.interfaces.JsonVocabularyLoader;
import com.example.wordy.model.Word;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MissingLetterActivity extends AppCompatActivity {
    private TextView tvTimer, tvScore, tvVietnamese, tvWord, tvFeedback;
    private GridLayout gridLetters;
    private Button btnPlayAgain, btnStartPause;
    private List<Word> words = new ArrayList<>();
    private int score;
    private long timeLeft; // 60 seconds
    private CountDownTimer timer;
    private Vibrator vibrator;
    private Word currentWord;
    private List<Integer> missingIndices;
    private List<String> correctLetters;
    private List<String> currentGuesses;
    private int currentGuessIndex;
    private boolean isGameEnded = false;
    private boolean isGameStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_letter);

        // Initialize views
        tvTimer = findViewById(R.id.tv_timer);
        tvScore = findViewById(R.id.tv_score);
        tvVietnamese = findViewById(R.id.tv_vietnamese);
        tvWord = findViewById(R.id.tv_word);
        tvFeedback = findViewById(R.id.tv_feedback);
        gridLetters = findViewById(R.id.grid_letters);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnStartPause = findViewById(R.id.btn_start_pause);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Set up header
        View headerLayout = findViewById(R.id.header);
        ((TextView) headerLayout.findViewById(R.id.header_title)).setText("Missing Letter Game");
        headerLayout.findViewById(R.id.btnIconRight).setVisibility(View.GONE);
        // Back button
        findViewById(R.id.btnReturn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Load vocabulary
        words = JsonVocabularyLoader.loadWordsFromJson(this, "voca.json", -1);
        if (words.isEmpty()) {
            Toast.makeText(this, "Error: Could not load vocabulary!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Button listeners
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
        gridLetters.removeAllViews();
        tvVietnamese.setVisibility(View.INVISIBLE);
        tvWord.setVisibility(View.INVISIBLE);
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
            tvWord.setVisibility(View.VISIBLE);
            enableLetterButtons(true);
            startTimer();
        } else {
            // Pause the game
            isGameStarted = false;
            btnStartPause.setText("Start Game");
            tvVietnamese.setVisibility(View.INVISIBLE);
            tvWord.setVisibility(View.INVISIBLE);
            enableLetterButtons(false);
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    private void enableLetterButtons(boolean enabled) {
        for (int i = 0; i < gridLetters.getChildCount(); i++) {
            View child = gridLetters.getChildAt(i);
            if (child instanceof Button) {
                child.setEnabled(enabled);
            }
        }
    }

    private void nextWord() {
        if (isGameEnded) return;

        gridLetters.removeAllViews();
        tvFeedback.setVisibility(View.GONE);

        // Select a random word
        Random random = new Random();
        currentWord = words.get(random.nextInt(words.size()));
        String english = currentWord.getWord().toLowerCase();

        // Determine number of letter buttons (4 to 8)
        int numButtons = random.nextInt(5) + 4; // 4 to 8

        // Determine number of missing letters (1 to min(word length, numButtons-1))
        int maxMissing = Math.min(english.length(), numButtons - 1);
        int numMissing = random.nextInt(maxMissing) + 1; // At least 1 missing letter

        // Choose random indices for missing letters
        missingIndices = new ArrayList<>();
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < english.length(); i++) {
            availableIndices.add(i);
        }
        Collections.shuffle(availableIndices);
        for (int i = 0; i < numMissing; i++) {
            missingIndices.add(availableIndices.get(i));
        }
        Collections.sort(missingIndices); // Sort for left-to-right filling

        // Get correct letters and unique letters
        correctLetters = new ArrayList<>();
        Set<String> uniqueCorrectLetters = new HashSet<>();
        for (int index : missingIndices) {
            String letter = String.valueOf(english.charAt(index));
            correctLetters.add(letter);
            uniqueCorrectLetters.add(letter);
        }

        // Create display word with underscores
        StringBuilder displayWord = new StringBuilder();
        for (int i = 0; i < english.length(); i++) {
            if (missingIndices.contains(i)) {
                displayWord.append("_ ");
            } else {
                displayWord.append(english.charAt(i)).append(" ");
            }
        }

        // Initialize guesses
        currentGuesses = new ArrayList<>();
        for (int i = 0; i < numMissing; i++) {
            currentGuesses.add("_");
        }
        currentGuessIndex = 0;

        // Update UI
        tvVietnamese.setText(currentWord.getMeaning());
        tvWord.setText(displayWord.toString().trim());

        // Create letter options
        List<String> letterOptions = new ArrayList<>(uniqueCorrectLetters);
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        while (letterOptions.size() < numButtons) {
            String randomLetter = String.valueOf(alphabet.charAt(random.nextInt(alphabet.length())));
            if (!letterOptions.contains(randomLetter)) {
                letterOptions.add(randomLetter);
            }
        }
        Collections.shuffle(letterOptions);

        // Add letter buttons
        for (String letter : letterOptions) {
            Button btn = createLetterButton(letter);
            btn.setOnClickListener(v -> onLetterClick((Button) v));
            gridLetters.addView(btn);
        }

        // Enable buttons if the game is already started
        if (isGameStarted) {
            enableLetterButtons(true);
        }
    }

    private Button createLetterButton(String letter) {
        MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btn.setText(letter.toUpperCase());
        btn.setTag(letter);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        int margin = (int) getResources().getDisplayMetrics().density * 5; // 5dp margin
        params.setMargins(margin, margin, margin, margin);
        btn.setLayoutParams(params);

        btn.setPadding(16, 16, 16, 16);
        btn.setTextColor(getResources().getColor(android.R.color.white));
        btn.setTextSize(18);
        btn.setAllCaps(false);
        btn.setCornerRadius(16);
        btn.setElevation(4);
        btn.setRippleColorResource(android.R.color.white);
        btn.setEnabled(false); // Initially disabled
        return btn;
    }

    private void onLetterClick(Button btn) {
        if (isGameEnded || !isGameStarted) return;

        String selectedLetter = (String) btn.getTag();

        // Add the selected letter to the current position
        currentGuesses.set(currentGuessIndex, selectedLetter);
        currentGuessIndex++;

        // Update display word
        StringBuilder displayWord = new StringBuilder();
        int guessIdx = 0;
        for (int i = 0; i < currentWord.getWord().length(); i++) {
            if (missingIndices.contains(i)) {
                displayWord.append(currentGuesses.get(guessIdx)).append(" ");
                guessIdx++;
            } else {
                displayWord.append(currentWord.getWord().charAt(i)).append(" ");
            }
        }
        tvWord.setText(displayWord.toString().trim());

        // Check if all positions are filled
        if (currentGuessIndex == correctLetters.size()) {
            // Verify if the guesses are correct
            boolean isCorrect = true;
            for (int i = 0; i < correctLetters.size(); i++) {
                if (!currentGuesses.get(i).equalsIgnoreCase(correctLetters.get(i))) {
                    isCorrect = false;
                    break;
                }
            }

            if (isCorrect) {
                score += 10;
                tvScore.setText("Score: " + score);
                tvFeedback.setText("Correct!");
                tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                tvFeedback.setVisibility(View.VISIBLE);
                gridLetters.removeAllViews();
                tvWord.postDelayed(this::nextWord, 1000);
            } else {
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(200);
                }
                timeLeft = Math.max(0, timeLeft - 1000);
                tvTimer.setText("Time: " + (timeLeft / 1000));
                tvFeedback.setText("Wrong! Try again.");
                tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                tvFeedback.setVisibility(View.VISIBLE);
                tvFeedback.postDelayed(() -> tvFeedback.setVisibility(View.GONE), 800);

                // Reset guesses
                currentGuesses = new ArrayList<>();
                for (int i = 0; i < correctLetters.size(); i++) {
                    currentGuesses.add("_");
                }
                currentGuessIndex = 0;

                // Reset display word
                StringBuilder resetDisplayWord = new StringBuilder();
                for (int i = 0; i < currentWord.getWord().length(); i++) {
                    if (missingIndices.contains(i)) {
                        resetDisplayWord.append("_ ");
                    } else {
                        resetDisplayWord.append(currentWord.getWord().charAt(i)).append(" ");
                    }
                }
                tvWord.setText(resetDisplayWord.toString().trim());
            }
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
        gridLetters.removeAllViews();
        tvWord.setText("");
        tvVietnamese.setText("Game Over! Score: " + score);

        // Disable all buttons in gridLetters (just in case)
        enableLetterButtons(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}