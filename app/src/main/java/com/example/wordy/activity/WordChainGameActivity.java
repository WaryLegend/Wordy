package com.example.wordy.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.wordy.R;
import com.google.firebase.database.*;

import java.util.*;

public class WordChainGameActivity extends AppCompatActivity {

    private String roomCode = "";
    private String myUserId = "";
    private String firstLetter = "";
    private DatabaseReference roomRef;

    private TextView tvCountdown, tvFirstLetter, tvUsedWords;
    private EditText edtInput;
    private Button btnSubmit;
    private CountDownTimer countDownTimer;
    private boolean isMyTurn = false;

    private DataSnapshot latestSnapshot;
    private Map<String, String> playerNames = new HashMap<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_chain_game);

        roomCode = getIntent().getStringExtra("roomCode");
        myUserId = getIntent().getStringExtra("userId");

        if (myUserId == null) {
            Toast.makeText(this, "Không có userId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCountdown = findViewById(R.id.tvCountdown);
        tvFirstLetter = findViewById(R.id.tvFirstLetter);
        tvUsedWords = findViewById(R.id.tvUsedWords);
        edtInput = findViewById(R.id.edtWord);
        btnSubmit = findViewById(R.id.btnSubmit);

        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);
        requestQueue = Volley.newRequestQueue(this);

        btnSubmit.setOnClickListener(v -> submitWord(edtInput.getText().toString()));

        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                latestSnapshot = snapshot;

                playerNames.clear();
                for (DataSnapshot player : snapshot.child("players").getChildren()) {
                    String uid = player.getKey();
                    String name = player.child("name").getValue(String.class);
                    playerNames.put(uid, name != null ? name : uid);
                }

                if (Boolean.TRUE.equals(snapshot.child("gameOver").getValue(Boolean.class))) {
                    showFinalScores(snapshot.child("players"));
                    allowInput(false);
                    return;
                }

                firstLetter = snapshot.child("firstLetter").getValue(String.class);
                tvFirstLetter.setText("Bắt đầu bằng: " + firstLetter.toUpperCase());

                List<String> usedWords = snapshotToList(snapshot.child("usedWords"));
                tvUsedWords.setText("Từ đã dùng: " + String.join(", ", usedWords));

                String turnUserId = snapshot.child("currentTurn").getValue(String.class);
                if (turnUserId != null && turnUserId.equals(myUserId)) {
                    startTurn();
                    tvCountdown.setText("👉 Tới lượt của bạn (" + playerNames.get(myUserId) + ")");
                } else {
                    showWaiting(turnUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startTurn() {
        isMyTurn = true;
        allowInput(true);
        startCountdown(15);
    }

    private void submitWord(String input) {
        if (!isMyTurn) {
            showResult("⏱ Bạn đã hết thời gian!");
            return;
        }

        String word = input.trim().toLowerCase();

        if (!word.startsWith(firstLetter.toLowerCase())) {
            showResult("❌ Từ không đúng chữ cái đầu!");
            return;
        }

        List<String> usedWords = snapshotToList(latestSnapshot.child("usedWords"));
        if (usedWords.contains(word)) {
            showResult("❌ Từ đã được dùng!");
            return;
        }

        // Gọi API từ điển để kiểm tra từ
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> continueSubmitWord(word),
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        showResult("❌ Không tìm thấy trong từ điển!");
                    } else {
                        showResult("⚠ Lỗi kết nối: " + error.getMessage());
                    }
                }
        );

        requestQueue.add(request);
    }

    private void continueSubmitWord(String word) {
        roomRef.child("players").child(myUserId).child("score").get().addOnSuccessListener(scoreSnap -> {
            Long score = scoreSnap.getValue(Long.class);
            if (score == null) score = 0L;
            roomRef.child("players").child(myUserId).child("score").setValue(score + 10);
        });

        List<String> usedWords = snapshotToList(latestSnapshot.child("usedWords"));
        usedWords.add(word);
        String nextUser = getNextPlayer(latestSnapshot);

        Map<String, Object> updates = new HashMap<>();
        updates.put("usedWords", usedWords);
        updates.put("currentTurn", nextUser);
        updates.put("timerExpiresAt", System.currentTimeMillis() + 30000);

        roomRef.updateChildren(updates);
        if (nextUser != null) {
            roomRef.child("skips").child(nextUser).setValue(false);
        }

        isMyTurn = false;
        allowInput(false);
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void startCountdown(int seconds) {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText("⏱ " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                isMyTurn = false;
                roomRef.child("skips").child(myUserId).setValue(true)
                        .addOnSuccessListener(unused -> checkAllSkippedAndProceed())
                        .addOnFailureListener(e -> Log.e("DEBUG", "Failed to set skip: " + e.getMessage()));
            }
        };
        countDownTimer.start();
    }

    private void checkAllSkippedAndProceed() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean allSkipped = true;

                for (DataSnapshot skipEntry : snapshot.child("skips").getChildren()) {
                    Boolean skipped = skipEntry.getValue(Boolean.class);
                    if (skipped == null || !skipped) {
                        allSkipped = false;
                        break;
                    }
                }

                if (allSkipped) {
                    roomRef.child("gameOver").setValue(true);
                } else {
                    String nextUser = getNextPlayer(snapshot);
                    if (nextUser == null) {
                        roomRef.child("gameOver").setValue(true);
                    } else {
                        Map<String, Object> update = new HashMap<>();
                        update.put("currentTurn", nextUser);
                        update.put("timerExpiresAt", System.currentTimeMillis() + 30000);
                        roomRef.updateChildren(update);
                        roomRef.child("skips").child(nextUser).setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WordChainGameActivity.this, "Lỗi cập nhật lượt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void allowInput(boolean allow) {
        edtInput.setEnabled(allow);
        btnSubmit.setEnabled(allow);
        if (allow) edtInput.setText("");
    }

    private void showWaiting(String currentTurnId) {
        allowInput(false);
        String name = playerNames.getOrDefault(currentTurnId, "người khác");
        tvCountdown.setText("⏳ Đang chờ " + name + "...");
    }

    private void showResult(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private List<String> snapshotToList(DataSnapshot usedWordsSnapshot) {
        List<String> list = new ArrayList<>();
        for (DataSnapshot child : usedWordsSnapshot.getChildren()) {
            list.add(child.getValue(String.class));
        }
        return list;
    }

    private String getNextPlayer(DataSnapshot snapshot) {
        List<String> ids = new ArrayList<>();
        Map<String, Boolean> skips = new HashMap<>();

        for (DataSnapshot player : snapshot.child("players").getChildren()) {
            String playerId = player.getKey();
            ids.add(playerId);
            Boolean skipped = snapshot.child("skips").child(playerId).getValue(Boolean.class);
            skips.put(playerId, skipped != null ? skipped : false);
        }

        String currentTurn = snapshot.child("currentTurn").getValue(String.class);
        int currentIndex = ids.indexOf(currentTurn);

        for (int i = 1; i <= ids.size(); i++) {
            int nextIndex = (currentIndex + i) % ids.size();
            String nextUser = ids.get(nextIndex);
            if (!Boolean.TRUE.equals(skips.get(nextUser))) {
                return nextUser;
            }
        }
        return null;
    }

    private void showFinalScores(DataSnapshot playersSnapshot) {
        StringBuilder builder = new StringBuilder("🎮 KẾT QUẢ CUỐI CÙNG: ");
        for (DataSnapshot player : playersSnapshot.getChildren()) {
            String name = player.child("name").getValue(String.class);
            Long score = player.child("score").getValue(Long.class);
            builder.append(name).append(": ").append(score != null ? score : 0).append(" điểm");
        }

        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(builder.toString())
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }
}