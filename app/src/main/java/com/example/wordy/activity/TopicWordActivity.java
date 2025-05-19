package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.adapter.TopicWordAdapter;
import com.example.wordy.model.Word;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class TopicWordActivity extends AppCompatActivity {
    private List<Word> wordList;
    private String topicName;
    private int topicId;
    private TopicWordAdapter topicWordAdapter;
    private Set<String> tempLearnedWords;
    private FirebaseFirestore db;
    //phát âm
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_item_layout);

        // Initialize views
        Button btnReturn = findViewById(R.id.btnReturn);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewWords);

        // Set up header title
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        // icon to mini_game options (to match TopicActivity)
        MaterialButton btnIconRight = headerLayout.findViewById(R.id.btnIconRight);
        btnIconRight.setIconResource(R.drawable.ic_controller_24px);

        // Get topic name, id from Intent
        topicName = getIntent().getStringExtra("topicName");
        topicId = getIntent().getIntExtra("topicId", -1);

        if (topicName != null && !topicName.isEmpty()) {
            headerTitle.setText(topicName);
        } else {
            topicName = "Words";
            headerTitle.setText(topicName);
        }

        // Set up back button
        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        // Set up IconRight button
        btnIconRight.setOnClickListener(v -> showBottomSheetMenu());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get words from Intent
        String wordsJson = getIntent().getStringExtra("words");
        if (wordsJson != null) {
            Type listType = new TypeToken<List<Word>>() {}.getType();
            wordList = new Gson().fromJson(wordsJson, listType);
        } else {
            wordList = new ArrayList<>();
        }

        db = FirebaseFirestore.getInstance();
        tempLearnedWords = new HashSet<>();
        loadLearnedWords();

        // Initialize adapter
        topicWordAdapter = new TopicWordAdapter(this, wordList, tempLearnedWords);
        recyclerView.setAdapter(topicWordAdapter);

        //Phát âm
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US); // Chọn giọng tiếng Anh (US)
            }
        });

    }

    private void loadLearnedWords() {
        PrefsHelper prefs = new PrefsHelper(this);
        String userId = prefs.getUserId();
        String formattedTopicName = topicName != null ? topicName.toLowerCase().replace(" ", "_") : null;

        if (userId == null || formattedTopicName == null) {
            Toast.makeText(this, "Error: Invalid user or topic", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("learnedWords")
                .document(userId)
                .collection(formattedTopicName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        tempLearnedWords.add(doc.getId());
                    }
                    topicWordAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load learned words", Toast.LENGTH_SHORT).show();
                });
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Dim background
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setDimAmount(0.5f);

        // Find buttons
        Button optionA = bottomSheetView.findViewById(R.id.optionA);
        optionA.setText("Flash Card");
        Button optionB = bottomSheetView.findViewById(R.id.optionB);
        optionB.setText("Quiz Game");
        Button optionC = bottomSheetView.findViewById(R.id.optionC);
        optionC.setText("Scramble Game");
        Button optionD = bottomSheetView.findViewById(R.id.optionD);
        optionD.setText("Listen & Fill");
        Button optionE = bottomSheetView.findViewById(R.id.optionE);
        optionE.setText("Pronunciation Game");
        Button cancelButton = bottomSheetView.findViewById(R.id.cancelButton);

        // Set click listeners
        optionA.setOnClickListener(v -> {
            Intent flashcardIntent = new Intent(this, FlashcardActivity.class);
            flashcardIntent.putExtra("topicId", topicId);
            flashcardIntent.putExtra("topicName", topicName);

            String wordsJson = new Gson().toJson(wordList);
            flashcardIntent.putExtra("words", wordsJson);
            startActivity(flashcardIntent);
            bottomSheetDialog.dismiss();
        });
        optionB.setOnClickListener(v -> {
            Intent quizIntent = new Intent(this, QuizActivity.class);
            quizIntent.putExtra("topicId", topicId);
            quizIntent.putExtra("topicName", topicName);
            startActivity(quizIntent);
            bottomSheetDialog.dismiss();
        });
        optionC.setOnClickListener(v -> {
            Intent wordScrambleIntent = new Intent(this, WordScrambleActivity.class);
            wordScrambleIntent.putExtra("topicId", topicId);
            wordScrambleIntent.putExtra("topicName", topicName);
            startActivity(wordScrambleIntent);
            bottomSheetDialog.dismiss();
        });
        optionD.setOnClickListener(v -> {
            Intent listenFillIntent = new Intent(this, ListenAndFillActivity.class);
            listenFillIntent.putExtra("topicId", topicId);
            listenFillIntent.putExtra("topicName", topicName);
            String wordsJson = new Gson().toJson(wordList);
            listenFillIntent.putExtra("words", wordsJson);
            startActivity(listenFillIntent);
            bottomSheetDialog.dismiss();
        });
        optionE.setOnClickListener(v -> {
            Intent pronunciationIntent = new Intent(this, PronunciationGameActivity.class);
            pronunciationIntent.putExtra("topicId", topicId);
            pronunciationIntent.putExtra("topicName", topicName);
            String wordsJson = new Gson().toJson(wordList);
            pronunciationIntent.putExtra("words", wordsJson);
            startActivity(pronunciationIntent);
            bottomSheetDialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Show dialog
        bottomSheetDialog.show();
    }

    //phát âm
    public void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload learned words to ensure updated state
        tempLearnedWords.clear();
        loadLearnedWords();
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