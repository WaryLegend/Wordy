package com.example.wordy.activity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.wordy.R;


public class WordDetailActivity extends AppCompatActivity {

    private TextView txtWord, txtPhonetic, txtDefinition, txtExample;
    private Button btnAudio,btnReturn;
    private String audioUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_detail);

        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        Button rm = headerLayout.findViewById(R.id.btnIconRight);
        rm.setVisibility(View.GONE);

        txtWord = findViewById(R.id.txtWord);
        txtPhonetic = findViewById(R.id.txtPhonetic);
        txtDefinition = findViewById(R.id.txtDefinition);
        txtExample = findViewById(R.id.exampleLabel);
        btnAudio = findViewById(R.id.btnAudio);
        btnReturn = findViewById(R.id.btnReturn);

        String word = getIntent().getStringExtra("word");
        String phonetic = getIntent().getStringExtra("phonetic");
        String definition = getIntent().getStringExtra("definition");
        String example = getIntent().getStringExtra("example");
        audioUrl = getIntent().getStringExtra("audio");

        // Set header title to the word
        if (word != null && !word.isEmpty()) {
            headerTitle.setText(word);
        } else {
            headerTitle.setText("Word Detail"); // Fallback
        }

        txtWord.setText(word != null ? word : "");
        txtPhonetic.setText(phonetic != null ? phonetic : "");

        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        if (definition != null && !definition.isEmpty()) {
            txtDefinition.setText(definition);
        } else {
            txtDefinition.setText("Không có định nghĩa");
        }

        if (example != null && !example.trim().isEmpty()) {
            txtExample.setText("\"" + example + "\"");
            txtExample.setVisibility(View.VISIBLE);
        } else {
            txtExample.setVisibility(View.GONE);
        }

        if (audioUrl != null && !audioUrl.isEmpty()) {
            btnAudio.setVisibility(View.VISIBLE);
            btnAudio.setOnClickListener(v -> {
                Log.d("AUDIO", "Phát audio: " + audioUrl);

                MediaPlayer mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(audioUrl);

                    mediaPlayer.setOnPreparedListener(mp -> {
                        Log.d("AUDIO", "Đã chuẩn bị xong, phát audio");
                        mp.start();
                    });

                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        Log.e("AUDIO", "Lỗi khi phát audio: what=" + what + ", extra=" + extra);
                        Toast.makeText(this, "Không thể phát âm thanh", Toast.LENGTH_SHORT).show();
                        return true;
                    });

                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    Log.e("AUDIO", "Lỗi ngoài try-catch", e);
                    Toast.makeText(this, "Lỗi khi phát âm thanh", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            btnAudio.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}