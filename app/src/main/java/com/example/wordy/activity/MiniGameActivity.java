package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.google.android.material.card.MaterialCardView;

public class MiniGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame);

        // Set up header
        View headerLayout = findViewById(R.id.header);
        ((TextView) headerLayout.findViewById(R.id.header_title)).setText("Mini Games");
        headerLayout.findViewById(R.id.btnIconRight).setVisibility(View.GONE);

        // Initialize buttons
        MaterialCardView btnMatchingGame = findViewById(R.id.btnMatchingGame);
        MaterialCardView btnMissingLetterGame = findViewById(R.id.btnMissingLetterGame);
        MaterialCardView btnChainGame = findViewById(R.id.btnChainGame);

        // Set up click listeners
        btnMatchingGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchingGameActivity.class);
            startActivity(intent);
        });

        btnMissingLetterGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, MissingLetterActivity.class);
            startActivity(intent);
        });

        btnChainGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, RoomActivity.class);
            startActivity(intent);
        });

        // Set up back button
        findViewById(R.id.btnReturn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}