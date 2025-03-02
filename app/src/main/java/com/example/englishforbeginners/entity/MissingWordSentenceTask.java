package com.example.englishforbeginners.entity;

import java.io.Serializable;
import java.util.List;

public class MissingWordSentenceTask implements Serializable {

    private final String sentence;
    private final int id;
    private final String translation;
    private final String correctAnswer;
    private List<WrongAnswer> wrongAnswers;

    public MissingWordSentenceTask(String sentence, int id, String translation, String correctAnswer) {
        this.sentence = sentence;
        this.id = id;
        this.translation = translation;
        this.correctAnswer = correctAnswer;
    }

    public String getSentence() {
        return sentence;
    }

    public int getId() {
        return id;
    }

    public String getTranslation() {
        return translation;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<WrongAnswer> getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(List<WrongAnswer> wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }
}
