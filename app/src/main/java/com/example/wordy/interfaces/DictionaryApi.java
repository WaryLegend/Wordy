package com.example.wordy.interfaces;

import com.example.wordy.model.DictionaryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DictionaryApi {
    @GET("entries/en/{word}")
    Call<List<DictionaryResponse>> getMeaning(@Path("word") String word);
}
