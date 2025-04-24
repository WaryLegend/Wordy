package com.example.wordy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.model.LearnedWord;

import java.util.List;

public class LearnedWordsAdapter extends RecyclerView.Adapter<LearnedWordsAdapter.LearnedWordViewHolder> {

    private final List<LearnedWord> learnedWords;

    public LearnedWordsAdapter(List<LearnedWord> learnedWords) {
        this.learnedWords = learnedWords;
    }

    public static class LearnedWordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvTopic, tvDate;

        public LearnedWordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    @NonNull
    @Override
    public LearnedWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_learned_word, parent, false);
        return new LearnedWordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LearnedWordViewHolder holder, int position) {
        LearnedWord word = learnedWords.get(position);
        holder.tvWord.setText(word.getWord());
        holder.tvTopic.setText(word.getTopicId());
        holder.tvDate.setText(word.getLearnedDate());
    }

    @Override
    public int getItemCount() {
        return learnedWords.size();
    }
}