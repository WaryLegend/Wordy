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
import com.example.wordy.adapter.TopicAdapter;
import com.example.wordy.model.Topic;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends AppCompatActivity {

    RecyclerView rvTopics;
    List<Topic> topicList;
    TopicAdapter adapter;
    private Button btnReturn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // tiêu đề
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText("Topic");
        // bỏ cái icon bên phải (don't mind here)
        Button rm = headerLayout.findViewById(R.id.btnIconRight);
        rm.setVisibility(View.GONE);

        rvTopics = findViewById(R.id.rvTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));

        topicList = new ArrayList<>();
        adapter = new TopicAdapter(this, topicList);
        rvTopics.setAdapter(adapter);

        loadTopicsFromJson();
    }

    private void loadTopicsFromJson() {
        try {
            String jsonString = loadJSONFromAsset();
            if (jsonString != null) {
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                Type listType = new TypeToken<List<Topic>>() {}.getType();
                topicList.clear();
                topicList.addAll(new Gson().fromJson(jsonObject.get("vocabulary"), listType));
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Không thể đọc file JSON", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("voca.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
