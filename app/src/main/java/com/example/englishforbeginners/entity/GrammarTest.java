package com.example.englishforbeginners.entity;

import java.io.Serializable;
import java.util.List;

public class GrammarTest implements Serializable {

    private final String theme;
    private final int id;
    private int status;
    private List<ConstructSentenceTask> constructSentenceTasks;
    private List<MissingWordSentenceTask> missingWordTasks;

    public GrammarTest(String theme, int id, int status) {
        this.theme = theme;
        this.id = id;
        this.status = status;
    }

    public String getTheme() {
        return theme;
    }


    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<ConstructSentenceTask> getConstructSentenceTasks() {
        return constructSentenceTasks;
    }

    public void setConstructSentenceTasks(List<ConstructSentenceTask> constructSentenceTasks) {
        this.constructSentenceTasks = constructSentenceTasks;
    }

    public List<MissingWordSentenceTask> getMissingWordTasks() {
        return missingWordTasks;
    }

    public void setMissingWordTasks(List<MissingWordSentenceTask> missingWordTasks) {
        this.missingWordTasks = missingWordTasks;
    }
}
