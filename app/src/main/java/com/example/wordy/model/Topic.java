package com.example.wordy.model;

import java.util.List;

public class Topic {
    private int id;
    private String name;
    private String img;
    private List<Word> words;

    public Topic() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public List<Word> getWords() { return words; }
    public void setWords(List<Word> words) { this.words = words; }
}
