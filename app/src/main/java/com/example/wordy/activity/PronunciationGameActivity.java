package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.model.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PronunciationGameActivity extends AppCompatActivity {

    private TextView textTargetWord, textResult;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextToSpeech tts;

    private List<Word> wordList;
    private String targetWord;
    private int currentIndex = 0;

    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation_game);

        textTargetWord = findViewById(R.id.textTargetWord);
        textResult = findViewById(R.id.textResult);
        Button btnStartListening = findViewById(R.id.btnStartListening);
        Button btnPlaySound = findViewById(R.id.btnPlaySound);
        Button btnNextWord = findViewById(R.id.btnNextWord);
        findViewById(R.id.btnReturn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Set up header
        View headerLayout = findViewById(R.id.header);
        ((TextView) headerLayout.findViewById(R.id.header_title)).setText("Pronunciation Game");
        headerLayout.findViewById(R.id.btnIconRight).setVisibility(View.GONE);

        // Nhận word list
        String wordsJson = getIntent().getStringExtra("words");
        Type listType = new TypeToken<List<Word>>() {}.getType();
        wordList = new Gson().fromJson(wordsJson, listType);

        if (wordList == null || wordList.isEmpty()) {
            Toast.makeText(this, "No words available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Khởi tạo Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Chọn từ đầu tiên
        targetWord = wordList.get(currentIndex).getWord();
        textTargetWord.setText(targetWord);

        // Sự kiện khi nhấn Speak Now
        btnStartListening.setOnClickListener(v -> startListening());

        // Sự kiện khi nhấn Listen
        btnPlaySound.setOnClickListener(v -> speak(targetWord));

        // Sự kiện khi nhấn Next Word
        btnNextWord.setOnClickListener(v -> nextWord());

        // Lắng nghe kết quả phát âm
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0).toLowerCase();
                    runOnUiThread(() -> {
                        if (spokenText.equals(targetWord.toLowerCase())) {
                            textResult.setText("✅ Exactly! You read: " + spokenText);
                        } else {
                            textResult.setText("❌ Wrong. You read: " + spokenText);
                        }
                    });
                } else {
                    runOnUiThread(() -> textResult.setText("Can't hear you. Please try again."));
                }
            }

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                isListening = false;
            }
            @Override public void onError(int error) {
                isListening = false;
                runOnUiThread(() -> textResult.setText("Voice recognition error. Please try again."));
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        } else {
            textResult.setText("Listening...");
            speechRecognizer.startListening(speechRecognizerIntent);
            isListening = true;
        }
    }

    private void speak(String text) {
        if (tts != null && !tts.isSpeaking()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void nextWord() {
        currentIndex++;
        if (currentIndex >= wordList.size()) {
            currentIndex = 0; // Quay lại từ đầu
            Toast.makeText(this, "Out of words. Go back to the beginning.", Toast.LENGTH_SHORT).show();
        }
        targetWord = wordList.get(currentIndex).getWord();
        textTargetWord.setText(targetWord);
        textResult.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
