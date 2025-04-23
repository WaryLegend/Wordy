package com.example.wordy.model;

public class LearnedWord {
    private String topicId;
    private String word;
    private String learnedDate;

    public LearnedWord(String topicId, String word, String learnedDate) {
        this.topicId = topicId;
        this.word = word;
        this.learnedDate = learnedDate;
    }

    public String getTopicId() { return topicId; }
    public String getWord() { return word; }
    public String getLearnedDate() { return learnedDate; }
}
