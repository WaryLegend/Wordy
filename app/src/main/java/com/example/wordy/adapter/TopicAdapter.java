package com.example.wordy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.activity.TopicWordActivity;
import com.example.wordy.model.Topic;
import com.example.wordy.model.Word;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private Context context;
    private List<Topic> topicList;

    public TopicAdapter(Context context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.topic_item, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = topicList.get(position);
        if (topic == null) return;


        holder.textTopicName.setText(topic.getName() != null ? topic.getName() : "Chủ đề");


        String imgName = topic.getImg();
        if (imgName != null && !imgName.isEmpty()) {
            int imageResId = context.getResources().getIdentifier(
                    imgName.toLowerCase(),
                    "drawable",
                    context.getPackageName()
            );
            if (imageResId != 0) {
                holder.imageTopic.setImageResource(imageResId);
            } else {
                holder.imageTopic.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            holder.imageTopic.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.buttonLearnWords.setOnClickListener(v -> {
            Intent intent = new Intent(context, TopicWordActivity.class);
            Gson gson = new Gson();
            String wordListJson = gson.toJson(topic.getWords() != null ? topic.getWords() : new ArrayList<>());
            intent.putExtra("words", wordListJson);
            intent.putExtra("topicId", topic.getId());
            intent.putExtra("topicName", topic.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView textTopicName;
        Button buttonLearnWords;
        ImageView imageTopic;

        public TopicViewHolder(View itemView) {
            super(itemView);
            textTopicName = itemView.findViewById(R.id.textTopicName);
            buttonLearnWords = itemView.findViewById(R.id.buttonLearnWords);
            imageTopic = itemView.findViewById(R.id.imageTopic);
        }
    }

}