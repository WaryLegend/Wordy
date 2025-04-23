package com.example.wordy.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.adapter.LearnedWordsAdapter;
import com.example.wordy.model.LearnedWord;
import com.example.wordy.model.Topic;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LearnedWordsActivity extends AppCompatActivity {
    private Button btnReturn;
    private RecyclerView recyclerView;
    private LearnedWordsAdapter adapter;
    private List<LearnedWord> learnedWords = new ArrayList<>();

    private FirebaseFirestore db;
    private PrefsHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learned_words);

        recyclerView = findViewById(R.id.recyclerViewLearnedWords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LearnedWordsAdapter(learnedWords);
        recyclerView.setAdapter(adapter);

        prefs = new PrefsHelper(this);
        db = FirebaseFirestore.getInstance();

        String userId = prefs.getUserId();
        loadTopicIdsFromJsonAndFetchWords(userId);
        btnReturn = findViewById(R.id.btnReturn);
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
                        adapter.notifyDataSetChanged();
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

