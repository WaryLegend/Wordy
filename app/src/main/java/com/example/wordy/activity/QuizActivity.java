package com.example.wordy.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.Question;
import com.example.wordy.model.QuizCategory;
import com.example.wordy.utils.WordTracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion;
    private RadioGroup radioGroupOptions;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button btnCheck, btnNext;

    private List<Question> questions;
    private int currentIndex = 0;
    private View popupFeedback;
    private ImageView imgFeedback;
    private TextView tvFeedback;
    private Button btnContinue,btnReturn;

    private String currentUserId;
    private String currentTopicId;
    private String currentTopicName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_layout);

        currentUserId = new PrefsHelper(this).getUserId();
        currentTopicId = getIntent().getStringExtra("topicId");

        tvQuestion = findViewById(R.id.tvQuestion);
        btnNext = findViewById(R.id.btnNext);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        btnCheck = findViewById(R.id.btnCheck);
        btnReturn = findViewById(R.id.btnReturn);


        popupFeedback = findViewById(R.id.popupFeedback);
        imgFeedback = findViewById(R.id.imgFeedback);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnContinue = findViewById(R.id.btnContinue);
        popupFeedback.setVisibility(View.GONE);
        currentTopicName = getIntent().getStringExtra("topicName");
        questions = loadQuestionsFromAsset();


        Log.d("QUIZ_TOPIC", "Received topicName: " + currentTopicName);


        btnContinue.setOnClickListener(v -> {
            popupFeedback.setVisibility(View.GONE);
            btnNext.performClick();
        });

        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy câu hỏi cho chủ đề này", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        if (questions != null && !questions.isEmpty()) {
            showQuestion(questions.get(currentIndex));
        }

        btnCheck.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex < questions.size()) {
                showQuestion(questions.get(currentIndex));
            } else {
                Toast.makeText(this, "You finished the quiz!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void showQuestion(Question q) {
        tvQuestion.setText(q.getQuestion());
        List<String> options = q.getOptions();
        Log.d("QUIZ_DATA", "Question: " + q.getQuestion());
        Log.d("QUIZ_DATA", "Options: " + q.getOptions());

        optionA.setText(q.getOptions().get(0));
        optionB.setText(q.getOptions().get(1));
        optionC.setText(q.getOptions().get(2));
        optionD.setText(q.getOptions().get(3));

        radioGroupOptions.clearCheck();

        btnNext.setVisibility(View.GONE);
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

            showFeedback(true, "Chính xác!");
        } else {
            showFeedback(false, "Wrong! Correct answer: " + current.getCorrectAnswer());
        }

        btnNext.setVisibility(View.VISIBLE);
    }
    private void showFeedback(boolean correct, String message) {
        popupFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setText(message);
        imgFeedback.setImageResource(correct ? R.drawable.happy : R.drawable.sad);
    }


    private List<Question> loadQuestionsFromAsset() {
        try {
            InputStream is = getAssets().open("vocabulary_quiz.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Type listType = new TypeToken<List<QuizCategory>>(){}.getType();
            List<QuizCategory> categories = new Gson().fromJson(jsonObject.get("categories"), listType);

            for (QuizCategory category : categories) {
                Log.d("DEBUG_TOPIC_MATCH", "Comparing: " + category.getName() + " vs " + currentTopicName);
                if (category.getName().equalsIgnoreCase(currentTopicName)) {
                    return category.getQuestions();
                }
            }
            Log.e("QUIZ_ERROR", "No questions found for topic: " + currentTopicName);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
