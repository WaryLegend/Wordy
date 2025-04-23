package com.example.wordy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.example.wordy.R;
import com.example.wordy.model.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private Context context;
    private List<Word> wordList;

    public WordAdapter(Context context, List<Word> wordList) {
        this.context = context;
        this.wordList = wordList;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.word_card_layout, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.textWord.setText(word.getWord());
        holder.textDefinition.setText(word.getMeaning());
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textWord, textDefinition;

        public WordViewHolder(View itemView) {
            super(itemView);
            textWord = itemView.findViewById(R.id.textWord);
            textDefinition = itemView.findViewById(R.id.textDefinition);
        }
    }
}

