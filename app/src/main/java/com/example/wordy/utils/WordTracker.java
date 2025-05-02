package com.example.wordy.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WordTracker {

    public static void markWordAsLearned(String userId, String topicName, String word) {
        if (userId == null || topicName == null || word == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        long timestamp = System.currentTimeMillis();
        String readableDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        data.put("learnedAt", timestamp);
        data.put("learnedDate", readableDate);

        db.collection("learnedWords")
                .document(userId)
                .collection(topicName)
                .document(word)
                .set(data);
    }
}