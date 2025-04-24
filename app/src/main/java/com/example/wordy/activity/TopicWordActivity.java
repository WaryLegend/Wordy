package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.adapter.TopicWordAdapter;
import com.example.wordy.model.Word;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TopicWordActivity extends AppCompatActivity {
    private List<Word> wordList;
    private String topicName;
    private String topicId;

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
        topicId = getIntent().getStringExtra("topicId");

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

        // Initialize adapter
        TopicWordAdapter topicWordAdapter = new TopicWordAdapter(this, wordList);
        recyclerView.setAdapter(topicWordAdapter);
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
        Button optionD = bottomSheetView.findViewById(R.id.optionD);
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

            bottomSheetDialog.dismiss();
        });
        optionD.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Show dialog
        bottomSheetDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}