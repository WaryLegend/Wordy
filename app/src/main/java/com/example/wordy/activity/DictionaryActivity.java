package com.example.wordy.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.R;
import com.example.wordy.adapter.DictionaryAdapter;
import com.example.wordy.interfaces.DictionaryApi;
import com.example.wordy.model.DictionaryResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DictionaryActivity extends AppCompatActivity {
    private DictionaryAdapter dictionaryAdapter;
    private RecyclerView recyclerView;
    private TextView hintText;
    private List<DictionaryResponse> wordList = new ArrayList<>();
    private Button btnReturn, btnSearchIcon;
    private TextInputLayout searchInputLayout;
    private TextInputEditText editTextSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        // Initialize views
        recyclerView = findViewById(R.id.rvWords);
        hintText = findViewById(R.id.hint_text);
        btnReturn = findViewById(R.id.btnReturn);
        btnSearchIcon = findViewById(R.id.btnIconRight);
        searchInputLayout = findViewById(R.id.searchInputLayout);
        editTextSearch = findViewById(R.id.editTextSearch);

        // tiêu đề
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText("Dictionary");

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dictionaryAdapter = new DictionaryAdapter(this, wordList);
        recyclerView.setAdapter(dictionaryAdapter);

        // Open TextInputLayout
        btnSearchIcon.setOnClickListener(v -> {
            btnSearchIcon.setVisibility(View.GONE);
            searchInputLayout.setVisibility(View.VISIBLE);
            editTextSearch.requestFocus();
            // Show keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        // Close TextInputLayout or navigate back
        btnReturn.setOnClickListener(v -> {
            if (searchInputLayout.getVisibility() == View.VISIBLE) {
                editTextSearch.setText(""); // Clear text
                searchInputLayout.setVisibility(View.GONE);
                btnSearchIcon.setVisibility(View.VISIBLE);
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
            } else {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String word = editTextSearch.getText().toString().trim();
                if (!word.isEmpty()) {
                    performSearch(word);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

    }

    private void performSearch(String word) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DictionaryApi api = retrofit.create(DictionaryApi.class);
        api.getMeaning(word).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<DictionaryResponse>> call, Response<List<DictionaryResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    wordList.clear();
                    wordList.add(response.body().get(0));
                    dictionaryAdapter.notifyDataSetChanged();
                    hintText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    Log.e("API", "Empty response for: " + word);
                    Toast.makeText(DictionaryActivity.this, "No results for: " + word, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DictionaryResponse>> call, Throwable t) {
                Log.e("API", "Failed to load word: " + word, t);
                Toast.makeText(DictionaryActivity.this, "Failed to find: " + word, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        btnSearchIcon.setVisibility(View.VISIBLE);
        searchInputLayout.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}