package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.adapter.FlashcardAdapter;
import com.example.wordy.model.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button prevButton;
    private Button turnButton;
    private Button btnReturn;
    private Button nextButton;
    private FlashcardAdapter adapter;
    private List<Word> currentWords;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);
        Log.d("FlashcardDebug", "FlashcardActivity is opened");

        recyclerView = findViewById(R.id.flashcard_recycler_view);
        prevButton = findViewById(R.id.prev_button);
        turnButton = findViewById(R.id.turn_button);
        nextButton = findViewById(R.id.next_button);
        btnReturn = findViewById(R.id.btnReturn);
        currentWords = new ArrayList<>();
        currentPosition = 0;

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String topicId = intent.getStringExtra("topicId");
        String topicName = intent.getStringExtra("topicName");
        String wordsJson = intent.getStringExtra("words");

        Log.d("FlashcardDebug", "Received topicId: " + topicId + ", topicName: " + topicName);

        if (wordsJson != null && !wordsJson.isEmpty()) {
            Type listType = new TypeToken<List<Word>>() {}.getType();
            currentWords = new Gson().fromJson(wordsJson, listType);
            Log.d("FlashcardDebug", "Loaded word list from intent: " + currentWords.size() + " words");
        } else {
            Log.w("FlashcardDebug", "No word list received from Intent!");
        }

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        prevButton.setOnClickListener(v -> {
            if (currentPosition > 0) {
                currentPosition--;
                recyclerView.smoothScrollToPosition(currentPosition);
                adapter.resetFlipState(currentPosition);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentPosition < currentWords.size() - 1) {
                currentPosition++;
                recyclerView.smoothScrollToPosition(currentPosition);
                adapter.resetFlipState(currentPosition);
            }
        });

        turnButton.setOnClickListener(v -> {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(currentPosition);
            if (holder != null) {
                adapter.flipCard(currentPosition, holder.itemView);
            }
        });

        if (!currentWords.isEmpty()) {
            updateRecyclerView();
        }
        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void updateRecyclerView() {
        adapter = new FlashcardAdapter(currentWords);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(currentPosition);
    }
}
