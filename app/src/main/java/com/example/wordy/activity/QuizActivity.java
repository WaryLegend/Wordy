package com.example.wordy.activity;

import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.Question;
import com.example.wordy.model.QuizCategory;
import com.example.wordy.utils.WordTracker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion;
    private RadioGroup radioGroupOptions;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button btnCheck;
    private Button btnReturn;
    private List<Question> questions;
    private int currentIndex = 0;
    private String currentUserId, currentTopicId, currentTopicName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_layout);

        currentUserId = new PrefsHelper(this).getUserId();
        currentTopicId = getIntent().getStringExtra("topicId");
        currentTopicName = getIntent().getStringExtra("topicName");

        // change title & right button (gone)
        View headerLayout = findViewById(R.id.header);
        Button rm = headerLayout.findViewById(R.id.btnIconRight);
        rm.setVisibility(View.GONE);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText(currentTopicName);

        tvQuestion = findViewById(R.id.tvQuestion);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        btnCheck = findViewById(R.id.btnCheck);
        btnReturn = findViewById(R.id.btnReturn);

        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        questions = loadQuestionsFromAsset();

        if (questions != null && !questions.isEmpty()) {
            showQuestion(questions.get(currentIndex));
        }
        btnCheck.setOnClickListener(v -> checkAnswer());
    }

    private void showQuestion(Question q) {
        tvQuestion.setText(q.getQuestion());
        List<String> randomizedOptions = new ArrayList<>(q.getOptions());
        Collections.shuffle(randomizedOptions);

        optionA.setText(randomizedOptions.get(0));
        optionB.setText(randomizedOptions.get(1));
        optionC.setText(randomizedOptions.get(2));
        optionD.setText(randomizedOptions.get(3));
    }

    private void checkAnswer() {
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please choose an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedBtn = findViewById(selectedId);
        String selectedText = selectedBtn.getText().toString();
        Question current = questions.get(currentIndex);

        boolean isCorrect = selectedText.equals(current.getCorrectAnswer());

        if (isCorrect) {
            String learnedWord = current.getWord();
            if (learnedWord != null && currentTopicId != null) {
                WordTracker.markWordAsLearned(currentUserId, currentTopicId, learnedWord);
            }
            showFeedback(true, "Correct!", "Answer: " + current.getCorrectAnswer());
        } else {
            showFeedback(false, "Incorrect!", "Answer: " + current.getCorrectAnswer());
        }
        radioGroupOptions.clearCheck();
    }
    private void showFeedback(boolean correct, String message, String answer) {
        // Create Dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_feedback);

        // edit dialog layout
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(20 * getResources().getDisplayMetrics().density);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(background);
        dialog.getWindow().setElevation(12 * getResources().getDisplayMetrics().density);
        dialog.setCancelable(false);

        ImageView imgFeedback = dialog.findViewById(R.id.imgFeedback);
        TextView tvFeedback = dialog.findViewById(R.id.tvFeedback);
        TextView tvCorrection = dialog.findViewById(R.id.tvCorrection);
        Button btnContinue = dialog.findViewById(R.id.btnContinue);

        tvFeedback.setText(message);
        tvFeedback.setTextColor(ContextCompat.getColor(this, correct ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        tvCorrection.setText(answer);
        imgFeedback.setImageResource(correct ? R.drawable.happy : R.drawable.sad);

        // Continue button
        btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            currentIndex++;
            if (currentIndex < questions.size()) {
                showQuestion(questions.get(currentIndex));
            } else {
                Toast.makeText(this, "You finished the quiz!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        dialog.show();
    }


    private List<Question> loadQuestionsFromAsset() {
        try {
            InputStream is = getAssets().open("vocabulary_quiz.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Type listType = new TypeToken<List<QuizCategory>>(){}.getType();
            List<QuizCategory> categories = new Gson().fromJson(jsonObject.get("categories"), listType);

            for (QuizCategory category : categories) {
                if (category.getName().equalsIgnoreCase(currentTopicName)) {
                    return category.getQuestions();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}