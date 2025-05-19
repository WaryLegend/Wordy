package com.example.wordy.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String name;
    @Nullable
    private String birthday;
    private String phone;

    private Map<String, Boolean> achievements = new HashMap<>();

    public User() {
        achievements.put("first_word", false);
        achievements.put("learned_10_words", false);
        achievements.put("learned_25_words", false);
        achievements.put("learned_50_words", false);
        achievements.put("learned_100_words", false);
    }

    public User(String name, @Nullable String birthday, String phone) {
        this.name = name;
        this.birthday = birthday;
        this.phone = phone;
        achievements.put("first_word", false);
        achievements.put("learned_10_words", false);
        achievements.put("learned_25_words", false);
        achievements.put("learned_50_words", false);
        achievements.put("learned_100_words", false);
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}
    @Nullable
    public String getBirthday() {return birthday;}
    public void setBirthday(@Nullable String birthday) {this.birthday = birthday;}
    public Map<String, Boolean> getAchievements() {
        return achievements;
    }
    public void setAchievements(Map<String, Boolean> achievements) {
        this.achievements = achievements;
    }
}
