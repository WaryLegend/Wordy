package com.example.wordy.model;

import java.util.List;

public class DictionaryResponse {
    public String word;
    public String phonetic;
    public List<Phonetic> phonetics;
    public List<Meaning> meanings;
    public String origin;
}
