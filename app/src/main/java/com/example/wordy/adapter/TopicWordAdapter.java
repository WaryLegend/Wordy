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
import com.example.wordy.activity.TopicWordActivity;
import com.example.wordy.activity.WordDetailActivity;
import com.example.wordy.model.Word;
import com.example.wordy.utils.WordTracker;
import com.example.wordy.utils.AchievementHelper;
import com.google.android.material.button.MaterialButton;
import com.example.wordy.TempPref.PrefsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TopicWordAdapter extends RecyclerView.Adapter<TopicWordAdapter.WordViewHolder> {

    private Context context;
    private List<Word> wordList;
    private String userId;
    private final String topicName;
    private Set<String> learnedWords;

    public TopicWordAdapter(Context context, List<Word> wordList, Set<String> learnedWords) {
        this.context = context;
        this.wordList = wordList;
        this.learnedWords = learnedWords;
        PrefsHelper prefs = new PrefsHelper(context);
        this.userId = prefs.getUserId();

        this.topicName = ((TopicWordActivity) context).getIntent().getStringExtra("topicName");
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

        // Check if the word is learned
        if (learnedWords.contains(word.getWord())) {
            holder.btnInfo.setIconResource(R.drawable.ic_check_circle_24px);
            holder.btnInfo.setIconTintResource(android.R.color.holo_green_light);
        } else {
            holder.btnInfo.setIconResource(R.drawable.ic_info_24px);
            holder.btnInfo.setIconTintResource(R.color.navy_blue);
        }

        // Set click listener for btnInfo
        holder.btnInfo.setOnClickListener(v -> {
            // Mark word as learned
            if (userId != null && topicName != null && !learnedWords.contains(word.getWord())) {
                String name = topicName.toLowerCase().replace(" ", "_");
                List<String> allWords = new ArrayList<>();
                for (Word w : wordList) {
                    allWords.add(w.getWord());
                }

                WordTracker.markWordAsLearned(context, userId, name, word.getWord());

                learnedWords.add(word.getWord());
                notifyItemChanged(position);
                AchievementHelper.checkAndUpdateWordAchievements(context);
            }

            // Start WordDetailActivity
            Intent intent = new Intent(context, WordDetailActivity.class);
            intent.putExtra("word", word.getWord());
            intent.putExtra("phonetic", word.getPronunciation());
            intent.putExtra("definition", word.getMeaning());
            intent.putExtra("example", word.getExample());
            context.startActivity(intent);
        });

        holder.btnSpeaker.setOnClickListener(v -> {
            if (context instanceof TopicWordActivity) {
                ((TopicWordActivity) context).speak(word.getWord());
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textWord, textDefinition;
        MaterialButton btnInfo, btnSpeaker;

        public WordViewHolder(View itemView) {
            super(itemView);
            textWord = itemView.findViewById(R.id.textWord);
            textDefinition = itemView.findViewById(R.id.textDefinition);
            btnInfo = itemView.findViewById(R.id.btnInfo);
            btnSpeaker = itemView.findViewById(R.id.btnSpeaker);
        }
    }
}