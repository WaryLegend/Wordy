package com.example.wordy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView wordText, pronunciationText, typeText, exampleText, exampleList;

        FlashcardViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.word_text);
            pronunciationText = itemView.findViewById(R.id.pronunciation_text);
            typeText = itemView.findViewById(R.id.type_text);
            exampleText = itemView.findViewById(R.id.ex_text);
            exampleList = itemView.findViewById(R.id.example);
        }
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
        holder.itemView.setTag(holder);

        if (!isFlipped[position]) {
            holder.wordText.setText(word.getWord());
            holder.typeText.setText(word.getType());
            holder.pronunciationText.setText(word.getPronunciation());

            holder.pronunciationText.setVisibility(View.VISIBLE);
            holder.typeText.setVisibility(View.VISIBLE);
            holder.exampleList.setVisibility(View.GONE);
            holder.exampleText.setVisibility(View.GONE);
        } else {
            holder.wordText.setText(word.getMeaning());
            holder.exampleList.setText(word.getExample());

            holder.pronunciationText.setVisibility(View.GONE);
            holder.typeText.setVisibility(View.GONE);
            holder.exampleList.setVisibility(View.VISIBLE);
            holder.exampleText.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> flipCard(position, v));
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void flipCard(final int position, final View view) {
        Animation flipOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.flip_out);
        Animation flipIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.flip_in);

        FlashcardViewHolder holder = (FlashcardViewHolder) view.getTag();

        flipOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                isFlipped[position] = !isFlipped[position];

                if (holder != null) {
                    bindViewHolder(holder, position);
                }

                view.startAnimation(flipIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        view.startAnimation(flipOut);
    }

    public void resetFlipState(int position) {
        if (position >= 0 && position < isFlipped.length && isFlipped[position]) {
            isFlipped[position] = false;
            notifyItemChanged(position);
        }
    }
}
