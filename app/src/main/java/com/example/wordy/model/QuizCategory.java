package com.example.wordy.model;

import java.io.Serializable;
import java.util.List;
public class QuizCategory implements Serializable {
    private String name;
    private List<Question> questions;

    public QuizCategory() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}