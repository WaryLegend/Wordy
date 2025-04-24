package com.example.wordy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.activity.WordDetailActivity;
import com.example.wordy.model.Word;

import java.util.List;

public class TopicWordAdapter extends RecyclerView.Adapter<TopicWordAdapter.WordViewHolder> {

    private Context context;
    private List<Word> wordList;

    public TopicWordAdapter(Context context, List<Word> wordList) {
        this.context = context;
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.topic_word_card_layout, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.textWord.setText(word.getWord());
        holder.textDefinition.setText(word.getMeaning());

        // Set click listener for btnInfo
        holder.btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(context, WordDetailActivity.class);
            intent.putExtra("word", word.getWord());
            intent.putExtra("phonetic", word.getPronunciation());
            intent.putExtra("definition", word.getMeaning());
            intent.putExtra("example", word.getExample());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textWord, textDefinition;
        Button btnInfo;

        public WordViewHolder(View itemView) {
            super(itemView);
            textWord = itemView.findViewById(R.id.textWord);
            textDefinition = itemView.findViewById(R.id.textDefinition);
            btnInfo = itemView.findViewById(R.id.btnInfo);
        }
    }
}