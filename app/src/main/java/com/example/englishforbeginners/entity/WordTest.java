package com.example.englishforbeginners.entity;

import java.io.Serializable;
import java.util.List;

public class WordTest implements Serializable {

    private final int id;
    private final String theme;
    private List<Word> words;
    private int status;

    public WordTest(int id, String theme, int status) {
        this.id = id;
        this.theme = theme;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
