package com.example.englishforbeginners.entity;

public class Stats {
    public int totalAnswers;
    public int correctAnswers;
    public int totalTests;
    public int successfulTests;
    public int miniExamMark;
    public int miniExamCompletions;

    public int examMark;
    public int examCompletions;

    public int totalGames;
    public int successfulGames;
    public int totalTries;

    public int messagesSent;

    public double getCorrectAnswersPercentage(){
        if (totalAnswers == 0 || correctAnswers == 0) {
            return 0;
        }

        return ((double) correctAnswers / totalAnswers * 100);
    }

    public double getSuccessfulTestsPercentage(){
        if (totalTests == 0 || successfulTests == 0) {
            return 0;
        }

        return ((double) successfulTests / totalTests * 100);
    }

    public double getSuccessfulGamesPercentage(){
        if (totalGames == 0 || successfulGames == 0) {
            return 0;
        }

        return ((double) successfulGames / totalGames * 100);
    }

    public long getMeanTries(){
        if (totalTries == 0 || totalGames == 0) {
            return 0;
        }

        return Math.round((double) totalTries / totalGames);
    }
}
