package com.example.wordy.interfaces;


import android.content.Context;

import com.example.wordy.model.Word;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonVocabularyLoader {

    public static List<Word> loadWordsFromJson(Context context, String fileName) {
        List<Word> words = new ArrayList<>();
        try {
            String jsonString = readJsonFromAssets(context, fileName);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray vocabularyArray = jsonObject.getJSONArray("vocabulary");

            for (int i = 0; i < vocabularyArray.length(); i++) {
                JSONObject categoryObject = vocabularyArray.getJSONObject(i);
                JSONArray wordsArray = categoryObject.getJSONArray("words");
                for (int j = 0; j < wordsArray.length(); j++) {
                    JSONObject wordObject = wordsArray.getJSONObject(j);
                    String english = wordObject.getString("word");
                    String vietnamese = wordObject.getString("meaning");
                    words.add(new Word(english, vietnamese));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return words;
    }

    private static String readJsonFromAssets(Context context, String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }
}