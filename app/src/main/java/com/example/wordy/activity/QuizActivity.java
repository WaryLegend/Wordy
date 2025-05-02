package com.example.wordy.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
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
import com.google.android.material.button.MaterialButton;
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
    private List<Question> questions;
    private int currentIndex = 0;
    private String currentUserId, currentTopicName;
    private int currentTopicId;
    private SharedPreferences quiz_prefs;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Khởi tạo SharedPreferences và Gson lưu tiến trình
        quiz_prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        gson = new Gson();

        currentUserId = new PrefsHelper(this).getUserId();
        currentTopicId = getIntent().getIntExtra("topicId", -1);
        currentTopicName = getIntent().getStringExtra("topicName");

        // Khôi phục currentIndex từ SharedPreferences
        currentIndex = quiz_prefs.getInt(currentTopicName + "_currentIndex", 0);

        // Change title & right button
        View headerLayout = findViewById(R.id.header);
        MaterialButton btnReset = headerLayout.findViewById(R.id.btnIconRight);
        btnReset.setIconResource(R.drawable.ic_restart_24px); // Đặt biểu tượng reset
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText(currentTopicName);

        tvQuestion = findViewById(R.id.tvQuestion);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        Button btnCheck = findViewById(R.id.btnCheck);
        Button btnReturn = findViewById(R.id.btnReturn);

        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Thêm sự kiện cho nút reset
        btnReset.setOnClickListener(v -> resetQuiz());

        // Tải danh sách câu hỏi
        questions = loadQuestions();

        if (questions != null && !questions.isEmpty()) {
            // Đảm bảo currentIndex không vượt quá kích thước danh sách
            if (currentIndex >= questions.size()) {
                currentIndex = 0; // Reset nếu vượt quá
                saveCurrentIndex();
            }
            showQuestion(questions.get(currentIndex));
        } else {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnCheck.setOnClickListener(v -> checkAnswer());
    }

    private List<Question> loadQuestions() {
        // Kiểm tra xem có danh sách câu hỏi đã lưu không
        String savedQuestionsJson = quiz_prefs.getString(currentTopicName + "_questions", null);
        if (savedQuestionsJson != null) {
            Type listType = new TypeToken<List<Question>>() {}.getType();
            return gson.fromJson(savedQuestionsJson, listType);
        }

        // Nếu không có, tải từ file JSON và xáo trộn
        List<Question> loadedQuestions = loadQuestionsFromAsset();
        if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
            Collections.shuffle(loadedQuestions); // Xáo trộn danh sách câu hỏi
            // Lưu danh sách câu hỏi vào SharedPreferences
            SharedPreferences.Editor editor = quiz_prefs.edit();
            editor.putString(currentTopicName + "_questions", gson.toJson(loadedQuestions));
            editor.apply();
        }
        return loadedQuestions;
    }

    private List<Question> loadQuestionsFromAsset() {
        try {
            InputStream is = getAssets().open("vocabulary_quiz.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Type listType = new TypeToken<List<QuizCategory>>() {}.getType();
            List<QuizCategory> categories = gson.fromJson(jsonObject.get("categories"), listType);

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

    private void showQuestion(Question q) {
        // Hiển thị số thứ tự câu hỏi trước nội dung câu hỏi
        String questionText = String.format("<b>Câu %d:</b> %s", currentIndex + 1, q.getQuestion());
        tvQuestion.setText(Html.fromHtml(questionText, Html.FROM_HTML_MODE_LEGACY));

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
//            String learnedWord = current.getWord();
//            if (learnedWord != null && currentTopicId != -1) {
//                WordTracker.markWordAsLearned(currentUserId, currentTopicName, learnedWord);
//            }
            showFeedback(true, "Correct!", "Answer: " + current.getCorrectAnswer());
        } else {
            showFeedback(false, "Incorrect!", "Answer: " + current.getCorrectAnswer());
        }
    }

    private void showFeedback(boolean correct, String message, String answer) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_feedback);

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

        btnContinue.setOnClickListener(v -> {
            radioGroupOptions.clearCheck();
            dialog.dismiss();
            currentIndex++;
            saveCurrentIndex(); // Lưu currentIndex khi chuyển câu
            if (currentIndex < questions.size()) {
                showQuestion(questions.get(currentIndex));
            } else {
                Toast.makeText(this, "You finished the quiz!", Toast.LENGTH_LONG).show();
                clearQuizState(); // Xóa trạng thái khi hoàn thành
                finish();
            }
        });
        dialog.show();
    }

    // Lưu currentIndex vào SharedPreferences
    private void saveCurrentIndex() {
        SharedPreferences.Editor editor = quiz_prefs.edit();
        editor.putInt(currentTopicName + "_currentIndex", currentIndex);
        editor.apply();
    }

    // Xóa trạng thái quiz (currentIndex và danh sách câu hỏi)
    private void clearQuizState() {
        SharedPreferences.Editor editor = quiz_prefs.edit();
        editor.remove(currentTopicName + "_currentIndex");
        editor.remove(currentTopicName + "_questions");
        editor.apply();
    }

    // Reset quiz: xóa trạng thái và tải lại câu hỏi
    private void resetQuiz() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Quiz")
                .setMessage("Bạn có muốn đặt lại quiz và bắt đầu lại từ đầu không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    clearQuizState();
                    currentIndex = 0;
                    questions = loadQuestions();
                    if (questions != null && !questions.isEmpty()) {
                        saveCurrentIndex();
                        showQuestion(questions.get(currentIndex));
                        radioGroupOptions.clearCheck();
                        Toast.makeText(this, "Quiz has been reset!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentIndex(); // Lưu currentIndex khi activity bị tạm dừng
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}