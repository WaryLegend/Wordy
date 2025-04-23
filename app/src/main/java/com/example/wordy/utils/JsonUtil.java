package com.example.wordy.utils;


import android.content.Context;
import android.util.Log;

import com.example.wordy.model.VocabularyList;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
    public static VocabularyList loadVocabulary(Context context) {
        String jsonString = loadJsonFromAssets(context, "voca.json");
        if (jsonString == null) {
            Log.e("JsonUtil", "Failed to load voca.json");
            return null;
        }
        Log.d("JsonUtil", "JSON loaded: " + jsonString);
        Gson gson = new Gson();
        return gson.fromJson(jsonString, VocabularyList.class);
    }
    private static String loadJsonFromAssets(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
