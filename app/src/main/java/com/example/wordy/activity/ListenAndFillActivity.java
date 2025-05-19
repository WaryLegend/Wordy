package com.example.wordy.activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.model.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

public class ListenAndFillActivity extends AppCompatActivity {

    private List<Word> wordList;
    private int currentIndex = 0;
    private int wrongAttempts = 0;  // Đếm số lần trả lời sai
    private TextToSpeech tts;
    private EditText editAnswer;
    private TextView textQuestion;
    private Button btnPlaySound, btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_fill);

        editAnswer = findViewById(R.id.editAnswer);
        textQuestion = findViewById(R.id.textQuestion);
        btnPlaySound = findViewById(R.id.btnPlaySound);
        btnSubmit = findViewById(R.id.btnSubmit);

        findViewById(R.id.btnReturn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        // Set up header
        View headerLayout = findViewById(R.id.header);
        ((TextView) headerLayout.findViewById(R.id.header_title)).setText("Listening");
        headerLayout.findViewById(R.id.btnIconRight).setVisibility(View.GONE);

        // Nhận danh sách từ - key nên là "words" cho đồng bộ
        String wordsJson = getIntent().getStringExtra("words");  // Sửa lại key thành "words"
        Type listType = new TypeToken<List<Word>>() {}.getType();
        wordList = new Gson().fromJson(wordsJson, listType);

        if (wordList == null || wordList.isEmpty()) {
            Toast.makeText(this, "No words available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                showQuestion();
            }
        });

        btnPlaySound.setOnClickListener(v -> playSound());
        btnSubmit.setOnClickListener(v -> checkAnswer());
        findViewById(R.id.btnReturn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void showQuestion() {
        wrongAttempts = 0;  // Reset số lần sai
        textQuestion.setText("Listen and type the word (" + (currentIndex + 1) + "/" + wordList.size() + ")");
        editAnswer.setText("");
        playSound();  // Tự động phát âm khi hiện từ
    }

    private void playSound() {
        if (tts != null && currentIndex < wordList.size()) {
            String text = wordList.get(currentIndex).getWord();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void checkAnswer() {
        String userAnswer = editAnswer.getText().toString().trim().toLowerCase();
        String correctAnswer = wordList.get(currentIndex).getWord().toLowerCase();

        if (userAnswer.equals(correctAnswer)) {
            Toast.makeText(this, "✅ Correct!", Toast.LENGTH_SHORT).show();
            currentIndex++;
            if (currentIndex < wordList.size()) {
                showQuestion();
            } else {
                Toast.makeText(this, "🎉 You finished all words!", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            wrongAttempts++;
            if (wrongAttempts >= 2) {
                // Gợi ý chữ cái đầu
                Toast.makeText(this, "Hint: The word starts with '" + correctAnswer.charAt(0) + "'", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Incorrect. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
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
