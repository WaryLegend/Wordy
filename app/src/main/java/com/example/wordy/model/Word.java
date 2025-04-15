package com.example.wordy.model;

public class Word {
    public int wordnumber;
    public String word;
    public String meaning;
    public String type;
    public String pronunciation;
    public String example;

    public Word() {
    }

    public int getWordnumber() {
        return wordnumber;
    }

    public void setWordnumber(int wordnumber) {
        this.wordnumber = wordnumber;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
