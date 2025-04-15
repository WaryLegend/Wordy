package com.example.wordy.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.adapter.WordAdapter;
import com.example.wordy.model.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TopicWordActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private List<Word> wordList;
    private Button btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.words_layout);

        // Initialize views
        btnReturn = findViewById(R.id.btnReturn);
        recyclerView = findViewById(R.id.recyclerViewWords);

        // Set up header title
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        // Hide search icon (to match TopicActivity)
        Button btnIconRight = headerLayout.findViewById(R.id.btnIconRight);
        if (btnIconRight != null) {
            btnIconRight.setVisibility(View.GONE);
        }

        // Get topic name from Intent
        String topicName = getIntent().getStringExtra("topicName");
        if (topicName != null && !topicName.isEmpty()) {
            headerTitle.setText(topicName);
        } else {
            headerTitle.setText("Words"); // Fallback
        }

        // Set up back button
        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}