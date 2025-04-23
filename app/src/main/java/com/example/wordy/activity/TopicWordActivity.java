package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private String topicName = "Words";
    private String topicId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_items_layout);


        btnReturn = findViewById(R.id.btnReturn);
        recyclerView = findViewById(R.id.recyclerViewWords);


        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);

        MaterialButton btnIconRight = headerLayout.findViewById(R.id.btnIconRight);
        btnIconRight.setIconResource(R.drawable.ic_controller_24px);

        topicName = getIntent().getStringExtra("topicName");
        topicId = getIntent().getStringExtra("topicId");

        if (topicName != null && !topicName.isEmpty()) {
            headerTitle.setText(topicName);
        } else {
            topicName = "Words";
            headerTitle.setText(topicName);
        }


        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnIconRight.setOnClickListener(v -> showBottomSheetMenu());


        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        String wordsJson = getIntent().getStringExtra("words");
        if (wordsJson != null) {
            Type listType = new TypeToken<List<Word>>() {}.getType();
            wordList = new Gson().fromJson(wordsJson, listType);
        } else {
            wordList = new ArrayList<>();
        }


        wordAdapter = new WordAdapter(this, wordList);
        recyclerView.setAdapter(wordAdapter);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);


        Objects.requireNonNull(bottomSheetDialog.getWindow()).setDimAmount(0.5f);


        Button optionA = bottomSheetView.findViewById(R.id.optionA);
        Button optionB = bottomSheetView.findViewById(R.id.optionB);
        Button optionC = bottomSheetView.findViewById(R.id.optionC);
        Button optionD = bottomSheetView.findViewById(R.id.optionD);
        Button cancelButton = bottomSheetView.findViewById(R.id.cancelButton);


        optionA.setOnClickListener(v -> {
            Intent quizIntent = new Intent(this, QuizActivity.class);
            quizIntent.putExtra("topicId", topicId != null ? topicId : topicName.toLowerCase().replace(" ", "_"));
            quizIntent.putExtra("topicName", topicName);
            startActivity(quizIntent);
            bottomSheetDialog.dismiss();
        });
        optionB.setOnClickListener(v -> {
            Intent flashcardIntent = new Intent(this, FlashcardActivity.class);
            flashcardIntent.putExtra("topicId", topicId);
            flashcardIntent.putExtra("topicName", topicName);

            String wordsJson = new Gson().toJson(wordList);
            flashcardIntent.putExtra("words", wordsJson);

            Log.d("FlashcardDebug", "Sending to FlashcardActivity: " + topicId + " / " + topicName + " / wordsJson.length=" + wordsJson.length());

            startActivity(flashcardIntent);
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

        bottomSheetDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}