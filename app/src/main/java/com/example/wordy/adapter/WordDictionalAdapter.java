package com.example.wordy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wordy.R;
import com.example.wordy.activity.WordDetailActivity;
import com.example.wordy.model.Definition;
import com.example.wordy.model.DictionaryResponse;
import com.example.wordy.model.Meaning;
import com.example.wordy.model.Phonetic;

import java.util.List;


public class WordDictionalAdapter extends RecyclerView.Adapter<WordDictionalAdapter.WordViewHolder> {

    private final List<DictionaryResponse> wordList;
    private Context context;
    public WordDictionalAdapter(Context context, List<DictionaryResponse> wordList) {
        this.context = context;
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        DictionaryResponse item = wordList.get(position);

        holder.wordText.setText(item.word);


        if (item.meanings != null && !item.meanings.isEmpty()) {
            String definition = item.meanings.get(0).definitions.get(0).definition;
            holder.definitionText.setText(definition);
        } else {
            holder.definitionText.setText("No definition found");
        }


        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, WordDetailActivity.class);

            intent.putExtra("word", item.word);
            intent.putExtra("phonetic", item.phonetic);


            StringBuilder allDefinitions = new StringBuilder();

            for (Meaning meaning : item.meanings) {
                allDefinitions.append("• ").append(meaning.partOfSpeech).append("\n");

                for (Definition def : meaning.definitions) {
                    allDefinitions.append("  - ").append(def.definition).append("\n");
                }

                allDefinitions.append("\n");
            }

            intent.putExtra("definition", allDefinitions.toString());



            if (item.phonetics != null && !item.phonetics.isEmpty()) {
                for (Phonetic phonetic : item.phonetics) {
                    if (phonetic.audio != null && !phonetic.audio.trim().isEmpty()) {
                        intent.putExtra("audio", phonetic.audio);
                        break;
                    }
                }
            }

            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordText, definitionText;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.txtWord);
            definitionText = itemView.findViewById(R.id.txtDefinition);
        }
    }
}
