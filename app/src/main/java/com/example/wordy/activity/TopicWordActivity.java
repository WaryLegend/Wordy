package com.example.wordy.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.adapter.WordAdapter;
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

    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private List<Word> wordList;
    private Button btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_items_layout);

        // Initialize views
        btnReturn = findViewById(R.id.btnReturn);
        recyclerView = findViewById(R.id.recyclerViewWords);

        // Set up header title
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        // icon to mini_game options (to match TopicActivity)
        MaterialButton btnIconRight = headerLayout.findViewById(R.id.btnIconRight);
        btnIconRight.setIconResource(R.drawable.ic_controller_24px);

        // Get topic name from Intent
        String topicName = getIntent().getStringExtra("topicName");
        if (topicName != null && !topicName.isEmpty()) {
            headerTitle.setText(topicName);
        } else {
            headerTitle.setText("Words"); // Fallback
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
        wordAdapter = new WordAdapter(this, wordList);
        recyclerView.setAdapter(wordAdapter);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Dim background
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setDimAmount(0.5f);

        // Find buttons
        Button optionA = bottomSheetView.findViewById(R.id.optionA);
        Button optionB = bottomSheetView.findViewById(R.id.optionB);
        Button optionC = bottomSheetView.findViewById(R.id.optionC);
        Button optionD = bottomSheetView.findViewById(R.id.optionD);
        Button cancelButton = bottomSheetView.findViewById(R.id.cancelButton);

        // Set click listeners
        optionA.setOnClickListener(v -> {
            Toast.makeText(this, "Selected Option A", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });
        optionB.setOnClickListener(v -> {
            Toast.makeText(this, "Selected Option B", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });
        optionC.setOnClickListener(v -> {
            Toast.makeText(this, "Selected Option C", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });
        optionD.setOnClickListener(v -> {
            Toast.makeText(this, "Selected Option D", Toast.LENGTH_SHORT).show();
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