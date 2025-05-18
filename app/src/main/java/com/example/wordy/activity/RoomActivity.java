package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class RoomActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private String myUserId;
    private String myName = "Player";

    private String roomCode = "";

    private DatabaseReference dbRef;
    private DatabaseReference roomRef;

    private TextView tvRoomCode;
    private Button btnCreateRoom, btnJoinRoom, btnStartGame;
    private EditText edtJoinCode, edtName;
    private ListView lvPlayerList;

    private List<String> playerNames = new ArrayList<>();
    private ArrayAdapter<String> playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        tvRoomCode = findViewById(R.id.tvRoomCode);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        btnJoinRoom = findViewById(R.id.btnJoinRoom);
        btnStartGame = findViewById(R.id.btnStartGame);
        edtJoinCode = findViewById(R.id.edtJoinCode);
        edtName = findViewById(R.id.edtName); // ⚠️ Thêm EditText nhập tên người chơi
        lvPlayerList = findViewById(R.id.lvPlayerList);

        btnCreateRoom.setEnabled(false);
        playerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playerNames);
        lvPlayerList.setAdapter(playerAdapter);

        btnCreateRoom.setOnClickListener(v -> {
            if (myUserId != null) {
                myName = edtName.getText().toString().trim();
                if (myName.isEmpty()) myName = "Player";
                createRoom();
            }
        });

        btnJoinRoom.setOnClickListener(v -> {
            if (myUserId != null) {
                myName = edtName.getText().toString().trim();
                if (myName.isEmpty()) myName = "Player";
                String code = edtJoinCode.getText().toString().trim().toUpperCase();
                joinRoom(code);
            }
        });

        btnStartGame.setOnClickListener(v -> startGame());

        signInIfNeeded();
    }

    private void signInIfNeeded() {
        btnCreateRoom.setEnabled(false);

        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    myUserId = auth.getCurrentUser().getUid();
                    btnCreateRoom.setEnabled(true);
                } else {
                    Toast.makeText(this, "Firebase Auth failed!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            myUserId = auth.getCurrentUser().getUid();
            btnCreateRoom.setEnabled(true);
        }
    }

    private void createRoom() {
        roomCode = generateRoomCode();
        roomRef = dbRef.child("rooms").child(roomCode);

        Map<String, Object> data = new HashMap<>();
        data.put("hostId", myUserId);
        data.put("currentTurn", myUserId);
        data.put("gameStarted", false);
        data.put("gameOver", false);
        data.put("firstLetter", randomLetter());

        Map<String, Object> playerData = new HashMap<>();
        playerData.put("name", myName);
        playerData.put("score", 0);

        Map<String, Object> players = new HashMap<>();
        players.put(myUserId, playerData);
        data.put("players", players);

        Map<String, Object> skips = new HashMap<>();
        skips.put(myUserId, false);
        data.put("skips", skips);
        data.put("usedWords", new ArrayList<String>());

        roomRef.setValue(data)
                .addOnSuccessListener(unused -> {
                    tvRoomCode.setText("Room Code: " + roomCode);
                    listenToRoom();
                    Toast.makeText(this, "Phòng đã tạo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Tạo phòng thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void joinRoom(String code) {
        if (code.isEmpty()) return;

        roomRef = dbRef.child("rooms").child(code);
        roomCode = code;

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(RoomActivity.this, "Không tìm thấy phòng", Toast.LENGTH_SHORT).show();
                    return;
                }

                long playerCount = snapshot.child("players").getChildrenCount();
                if (playerCount >= 3) {
                    Toast.makeText(RoomActivity.this, "Phòng đã đầy!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> playerData = new HashMap<>();
                playerData.put("name", myName);
                playerData.put("score", 0);

                roomRef.child("players").child(myUserId).setValue(playerData)
                        .addOnSuccessListener(unused -> {
                            tvRoomCode.setText("Room Code: " + roomCode);
                            roomRef.child("skips").child(myUserId).setValue(false);
                            listenToRoom();
                            Toast.makeText(RoomActivity.this, "Đã vào phòng", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenToRoom() {
        roomRef.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playerNames.clear();
                for (DataSnapshot player : snapshot.getChildren()) {
                    String name = player.child("name").getValue(String.class);
                    playerNames.add(name);
                }
                playerAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        roomRef.child("gameStarted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean started = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(started)) {
                    Intent i = new Intent(RoomActivity.this, WordChainGameActivity.class);
                    i.putExtra("roomCode", roomCode);
                    i.putExtra("userId", myUserId);
                    startActivity(i);
                    finish();
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startGame() {
        roomRef.child("gameStarted").setValue(true);
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }

    private String randomLetter() {
        return String.valueOf("abcdefghijklmnopqrstuvwxyz".charAt(new Random().nextInt(26)));
    }
}