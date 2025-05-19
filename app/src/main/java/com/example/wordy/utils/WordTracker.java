package com.example.wordy.utils;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class WordTracker {

    // Hàm đánh dấu từ đã học và cập nhật thành tích
    public static void markWordAsLearned(Context context, String userId, String topicName, String word) {
        if (userId == null || topicName == null || word == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        long timestamp = System.currentTimeMillis();

        data.put("learnedAt", timestamp);

        db.collection("learnedWords")
                .document(userId)
                .collection(topicName)
                .document(word)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("WORD_TRACKER", "Đã lưu từ: " + word + " vào Firestore.");
                    AchievementHelper.checkAndUpdateWordAchievements(context);
                })
                .addOnFailureListener(e -> {
                    Log.e("WORD_TRACKER", "Lỗi khi lưu từ: " + e.getMessage());
                });
    }

}
