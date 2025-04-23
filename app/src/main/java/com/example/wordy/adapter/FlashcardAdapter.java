package com.example.wordy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.model.Word;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<Word> words;
    private boolean[] isFlipped;

    public FlashcardAdapter(List<Word> words) {
        this.words = words;
        this.isFlipped = new boolean[words.size()];
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flashcard_item, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Word word = words.get(position);
        if (!isFlipped[position]) {
            holder.wordText.setText(word.getWord());
            holder.typeText.setText(word.getType());
            holder.pronunciationText.setText(word.getPronunciation());
            holder.meaningText.setText("");
            holder.exampleText.setText("");

            holder.pronunciationText.setVisibility(View.VISIBLE);
            holder.meaningText.setVisibility(View.GONE);
            holder.exampleText.setVisibility(View.GONE);
        } else {

            holder.wordText.setText(word.getMeaning());
            holder.typeText.setText("");
            holder.pronunciationText.setText("");
            holder.meaningText.setText("Example: " + word.getExample());
            holder.exampleText.setText("");

            holder.pronunciationText.setVisibility(View.GONE);
            holder.meaningText.setVisibility(View.VISIBLE);
            holder.exampleText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void flipCard(final int position, final View view) {
        Animation flipOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.flip_out);
        flipOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                isFlipped[position] = !isFlipped[position];
                notifyItemChanged(position);

                Animation flipIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.flip_in);
                view.startAnimation(flipIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(flipOut);
    }

    public void resetFlipState(int position) {
        isFlipped[position] = false;
        notifyItemChanged(position);
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contentLayout;
        TextView wordText;
        TextView meaningText;
        TextView pronunciationText;
        TextView exampleText;
        TextView typeText;

        FlashcardViewHolder(View itemView) {
            super(itemView);
            contentLayout = itemView.findViewById(R.id.flashcard_content);
            wordText = itemView.findViewById(R.id.word_text);
            meaningText = itemView.findViewById(R.id.meaning_text);
            pronunciationText = itemView.findViewById(R.id.pronunciation_text);
            exampleText = itemView.findViewById(R.id.example_text);
            typeText = itemView.findViewById(R.id.type_text);
        }
    }
}