package com.example.wordy.utils;

import android.content.Context;
import android.util.Log;

import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.QuizCategory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AchievementHelper {

    public static void checkAndUpdateWordAchievements(Context context) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = new PrefsHelper(context).getUserId();

        List<String> topicNames = getAllTopicKeysFromJson(context);
        int[] totalLearned = {0};

        for (String topic : topicNames) {
            db.collection("learnedWords")
                    .document(userId)
                    .collection(topic)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        int wordsLearned = queryDocumentSnapshots.size();
                        totalLearned[0] += wordsLearned;

                        Log.d("ACHIEVEMENT_CHECK", "Topic: " + topic + " có " + wordsLearned + " từ đã học.");
                        Log.d("ACHIEVEMENT_CHECK", "Tổng đã học tới giờ: " + totalLearned[0]);

                        // Cập nhật thành tựu
                        if (totalLearned[0] >= 1) {
                            db.collection("users").document(userId)
                                    .update("achievements.first_word", true);
                        }
                        if (totalLearned[0] >= 10) {
                            db.collection("users").document(userId)
                                    .update("achievements.learned_10_words", true);
                        }
                        if (totalLearned[0] >= 25) {
                            db.collection("users").document(userId)
                                    .update("achievements.learned_25_words", true);
                        }
                        if (totalLearned[0] >= 50) {
                            db.collection("users").document(userId)
                                    .update("achievements.learned_50_words", true);
                        }
                        if (totalLearned[0] >= 100) {
                            db.collection("users").document(userId)
                                    .update("achievements.learned_100_words", true);
                        }

                    }).addOnFailureListener(e -> {
                        Log.e("ACHIEVEMENT_CHECK", "Lỗi khi lấy dữ liệu learnedWords: " + e.getMessage());
                    });
        }
    }



    // Hàm này sẽ lấy tên tất cả topic từ file vocabulary.json
    private static List<String> getAllTopicKeysFromJson(Context context) {
        Log.d("TOPIC_KEY_JSON", "Bắt đầu đọc topic từ JSON");

        List<String> topicNames = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("voca.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if (!jsonObject.has("vocabulary")) {
                Log.e("TOPIC_KEY_JSON", "JSON không có key 'vocabulary' !!!");
            } else {
                Log.d("TOPIC_KEY_JSON", "Đã tìm thấy key 'vocabulary'");
            }

            Type listType = new TypeToken<List<QuizCategory>>() {}.getType();
            List<QuizCategory> categories = new Gson().fromJson(jsonObject.get("vocabulary"), listType);

            if (categories == null || categories.isEmpty()) {
                Log.e("TOPIC_KEY_JSON", "categories null hoặc trống !");
            } else {
                Log.d("TOPIC_KEY_JSON", "Đọc được số categories: " + categories.size());
            }

            for (QuizCategory category : categories) {
                String topicKey = category.getName().toLowerCase().replace(" ", "_");
                Log.d("TOPIC_KEY_JSON", "Topic key đọc được từ JSON: " + topicKey);
                topicNames.add(topicKey);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TOPIC_KEY_JSON", "Lỗi khi đọc JSON: " + e.getMessage());
        }

        return topicNames;
    }


}
