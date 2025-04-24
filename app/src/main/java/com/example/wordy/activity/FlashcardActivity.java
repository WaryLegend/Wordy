package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.adapter.FlashcardAdapter;
import com.example.wordy.model.Word;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button prevButton, btnReturn, nextButton;
    private FlashcardAdapter adapter;
    private List<Word> currentWords;
    private int currentPosition;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        recyclerView = findViewById(R.id.flashcard_recycler_view);
        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);
        btnReturn = findViewById(R.id.btnReturn);
        currentWords = new ArrayList<>();
        currentPosition = 0;


        // Receive data from Intent
        Intent intent = getIntent();
        String topicId = intent.getStringExtra("topicId");
        String topicName = intent.getStringExtra("topicName");
        String wordsJson = intent.getStringExtra("words");

        // change title & right button (gone)
        View headerLayout = findViewById(R.id.header);
        Button rm = headerLayout.findViewById(R.id.btnIconRight);
        rm.setVisibility(View.GONE);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText(topicName);

        Log.d("FlashcardDebug", "Received topicId: " + topicId + ", topicName: " + topicName);

        // Back button
        btnReturn.setOnClickListener(v -> finish());

        if (wordsJson != null && !wordsJson.isEmpty()) {
            Type listType = new TypeToken<List<Word>>() {}.getType();
            currentWords = new Gson().fromJson(wordsJson, listType);
            Log.d("FlashcardDebug", "Loaded word list from intent: " + currentWords.size() + " words");
        } else {
            Log.w("FlashcardDebug", "No word list received from Intent!");
        }

        // Set up RecyclerView
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        // Add scroll listener to sync currentPosition with visible card
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (visiblePosition != -1 && visiblePosition != currentPosition) {
                    currentPosition = visiblePosition;
                    adapter.resetFlipState(currentPosition);
                    Log.d("FlashcardDebug", "Swiped to position: " + currentPosition);
                }
            }
        });

        // Navigation buttons
        prevButton.setOnClickListener(v -> {
            if (currentPosition > 0) {
                currentPosition--;
                recyclerView.smoothScrollToPosition(currentPosition);
                adapter.resetFlipState(currentPosition);
                updateButtonStates();
                Log.d("FlashcardDebug", "Previous button clicked, position: " + currentPosition);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentPosition < currentWords.size() - 1) {
                currentPosition++;
                recyclerView.smoothScrollToPosition(currentPosition);
                adapter.resetFlipState(currentPosition);
                updateButtonStates();
                Log.d("FlashcardDebug", "Next button clicked, position: " + currentPosition);
            }
        });

        if (!currentWords.isEmpty()) {
            updateRecyclerView();
        } else {
            TextView emptyView = findViewById(R.id.empty_view);
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
    private void updateButtonStates() {
        prevButton.setEnabled(currentPosition > 0);
        nextButton.setEnabled(currentPosition < currentWords.size() - 1);
    }

    private void updateRecyclerView() {
        adapter = new FlashcardAdapter(currentWords);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(currentPosition);
    }

    public void onDestroy() {
        super.onDestroy();
    }
}