package com.example.wordy.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.adapter.LearnedWordsAdapter;
import com.example.wordy.model.LearnedWord;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LearnedWordsActivity extends AppCompatActivity {
    private LearnedWordsAdapter learnedWordsAdapter;
    private List<LearnedWord> learnedWords;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learned_words);

        RecyclerView learnedWordsView = findViewById(R.id.LearnedWordsView);
        learnedWordsView.setLayoutManager(new LinearLayoutManager(this));
        learnedWords = new ArrayList<>();
        learnedWordsAdapter = new LearnedWordsAdapter(learnedWords);
        learnedWordsView.setAdapter(learnedWordsAdapter);

        PrefsHelper prefs = new PrefsHelper(this);
        db = FirebaseFirestore.getInstance();

        String userId = prefs.getUserId();
        loadTopicIdsFromJsonAndFetchWords(userId);

        // Set up header
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText("Words you learned");
        Button btnIconRight = headerLayout.findViewById(R.id.btnIconRight);
        if (btnIconRight != null) {
            btnIconRight.setVisibility(View.GONE);
        }

        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void loadTopicIdsFromJsonAndFetchWords(String userId) {
        List<String> topicIds = loadTopicIdsFromJson();

        for (String topicId : topicIds) {
            db.collection("learnedWords")
                    .document(userId)
                    .collection(topicId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String word = doc.getId();
                            String learnedDate = doc.getString("learnedDate");
                            learnedWords.add(new LearnedWord(topicId, word, learnedDate));
                        }
                        learnedWordsAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load learned words", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private List<String> loadTopicIdsFromJson() {
        List<String> topicIds = new ArrayList<>();
        try {
            InputStream is = getAssets().open("voca.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonArray array = jsonObject.getAsJsonArray("vocabulary");

            for (JsonElement element : array) {
                String name = element.getAsJsonObject().get("name").getAsString();
                topicIds.add(name.toLowerCase().replace(" ", "_"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return topicIds;
    }
}