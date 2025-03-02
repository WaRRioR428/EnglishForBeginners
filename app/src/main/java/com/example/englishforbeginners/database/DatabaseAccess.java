package com.example.englishforbeginners.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import com.example.englishforbeginners.entity.ConstructSentenceTask;
import com.example.englishforbeginners.entity.GrammarTest;
import com.example.englishforbeginners.entity.MissingWordSentenceTask;
import com.example.englishforbeginners.entity.Stats;
import com.example.englishforbeginners.entity.WrongAnswer;
import com.example.englishforbeginners.entity.Word;
import com.example.englishforbeginners.entity.WordTest;


public class DatabaseAccess {
    private SQLiteDatabase database;
    private final DatabaseOpenHelper databaseOpenHelper;
    private static volatile DatabaseAccess instance;

    private DatabaseAccess(Context context) {
        this.databaseOpenHelper = new DatabaseOpenHelper(context);
    }

    public static synchronized DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void open() {
        this.database = databaseOpenHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    //-------------------------------------БЛОК WORD------------------------------------------------

    public void addWordTests(List<WordTest> wordTests) {
        for (WordTest test : wordTests) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_TEST_THEME, test.getTheme());
            values.put(DatabaseOpenHelper.KEY_COMPLETION_STATUS, test.getStatus());

            long id = database.insert(DatabaseOpenHelper.TABLE_WORD_TEST, null, values);
            addWords(test.getWords(), id);
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_TEST_THEME, "Мини-экзамен");
        values.put(DatabaseOpenHelper.KEY_COMPLETION_STATUS, 0);
        database.insert(DatabaseOpenHelper.TABLE_WORD_TEST, null, values);
    }

    public void addWords(List<Word> words, long id) {
        for (Word w : words) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_WORD, w.word());
            values.put(DatabaseOpenHelper.KEY_TRANSLATION, w.translation());
            values.put(DatabaseOpenHelper.KEY_TRANSCRIPTION, w.transcription());

            long wordId = database.insert(DatabaseOpenHelper.TABLE_WORD, null, values);

            values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_WORD_ID, wordId);
            values.put(DatabaseOpenHelper.KEY_WORD_TEST_ID, id);
            database.insert(DatabaseOpenHelper.TABLE_WORD_TO_TEST, null, values);
        }
    }

    @SuppressLint("Range")
    public List<WordTest> getAllWordTests() {
        List<WordTest> wordTests = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_WORD_TEST;

        Cursor c = database.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                int testId = c.getInt((c.getColumnIndex(DatabaseOpenHelper.KEY_ID)));
                String theme = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_TEST_THEME));
                int status = c.getInt(c.getColumnIndex(DatabaseOpenHelper.KEY_COMPLETION_STATUS));

                WordTest wordTest = new WordTest(testId, theme, status);
                wordTest.setWords(getWordsByTestId(wordTest.getId()));
                wordTests.add(wordTest);
            }
            while (c.moveToNext());
        }

        c.close();
        return wordTests;
    }

    public List<Word> getAllWords(){
        List<Word> words = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_WORD;
        Cursor c = database.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                int wordId = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_ID));
                String engWord = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD));
                String transcription = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TRANSCRIPTION));
                String translation = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TRANSLATION));
                Word word = new Word(wordId, engWord, transcription, translation);
                words.add(word);
            }
            while (c.moveToNext());
        }
        c.close();

        return words;
    }

    @SuppressLint("Range")
    public List<Word> getWordsByTestId(int id) {
        List<Word> wordList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_WORD + " tw JOIN " +
                DatabaseOpenHelper.TABLE_WORD_TO_TEST + " wtt ON  tw." + DatabaseOpenHelper.KEY_ID + " = wtt." + DatabaseOpenHelper.KEY_WORD_ID + " WHERE wtt."
                + DatabaseOpenHelper.KEY_WORD_TEST_ID + " = '" + id + "'";

        Cursor c = database.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                int wordId = c.getInt(c.getColumnIndex(DatabaseOpenHelper.KEY_ID));
                String engWord = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_WORD));
                String transcription = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_TRANSCRIPTION));
                String translation = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_TRANSLATION));

                Word word = new Word(wordId, engWord, transcription, translation);
                wordList.add(word);

            }
            while (c.moveToNext());
        }

        c.close();
        return wordList;
    }

    //-------------------------------------БЛОК GRAMMAR---------------------------------------------

    public void addGrammarTests(List<GrammarTest> grammarTests) {
        for (GrammarTest test : grammarTests) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_TEST_THEME, test.getTheme());
            values.put(DatabaseOpenHelper.KEY_COMPLETION_STATUS, test.getStatus());

            long id = database.insert(DatabaseOpenHelper.TABLE_GRAMMAR_TEST, null, values);

            if (CollectionUtils.isNotEmpty(test.getConstructSentenceTasks())) {
                addConstructTasks(test.getConstructSentenceTasks(), id);
            }
            if (CollectionUtils.isNotEmpty(test.getMissingWordTasks())) {
                addMissingTasks(test.getMissingWordTasks(), id);
            }
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_TEST_THEME, "Мини-экзамен");
        values.put(DatabaseOpenHelper.KEY_COMPLETION_STATUS, 0);
        database.insert(DatabaseOpenHelper.TABLE_GRAMMAR_TEST, null, values);
    }

    public void addConstructTasks(List<ConstructSentenceTask> constructTasks, long id) {
        for (ConstructSentenceTask task : constructTasks) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_SENTENCE, task.correctSentence());
            values.put(DatabaseOpenHelper.KEY_TRANSLATION, task.translation());
            values.put(DatabaseOpenHelper.KEY_WRONG_SENTENCE, task.wrongSentence());

            long id_word = database.insert(DatabaseOpenHelper.TABLE_CONSTRUCT_TASK, null, values);

            values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_GRAMMAR_TEST_ID, id);
            values.put(DatabaseOpenHelper.KEY_CONSTRUCT_TASK_ID, id_word);
            database.insert(DatabaseOpenHelper.TABLE_GRAMMAR_TEST_CONSTRUCT_TASKS, null, values);
        }
    }

    public void addMissingTasks(List<MissingWordSentenceTask> missingTasks, long id) {
        for (MissingWordSentenceTask task : missingTasks) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_SENTENCE, task.getSentence());
            values.put(DatabaseOpenHelper.KEY_TRANSLATION, task.getTranslation());
            values.put(DatabaseOpenHelper.KEY_ANSWER, task.getCorrectAnswer());

            long id_word = database.insert(DatabaseOpenHelper.TABLE_MISSING_TASK, null, values);

            addWrongAnswers(task.getWrongAnswers(), id_word);

            values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_GRAMMAR_TEST_ID, id);
            values.put(DatabaseOpenHelper.KEY_MISSING_TASK_ID, id_word);
            database.insert(DatabaseOpenHelper.TABLE_GRAMMAR_TEST_MISSING_TASKS, null, values);
        }
    }

    public void addWrongAnswers(List<WrongAnswer> wrongAnswerList, long id) {
        for (WrongAnswer wrongAnswer : wrongAnswerList) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_ANSWER, wrongAnswer.answer());

            long id_word = database.insert(DatabaseOpenHelper.TABLE_WRONG_ANSWER, null, values);

            values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_MISSING_TASK_ID, id);
            values.put(DatabaseOpenHelper.KEY_WRONG_ANSWER_ID, id_word);
            database.insert(DatabaseOpenHelper.TABLE_MISSING_TASK_WRONG_ANSWERS, null, values);
        }
    }

    @SuppressLint("Range")
    public List<GrammarTest> getAllGrammarTests() {
        List<GrammarTest> grammarTests = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_GRAMMAR_TEST;

        Cursor c = database.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                int testId = c.getInt((c.getColumnIndex(DatabaseOpenHelper.KEY_ID)));
                String theme = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_TEST_THEME));
                int status = c.getInt(c.getColumnIndex(DatabaseOpenHelper.KEY_COMPLETION_STATUS));
                GrammarTest test = new GrammarTest(theme, testId, status);
                test.setMissingWordTasks(getMissingTasksByTestId(test.getId()));
                test.setConstructSentenceTasks(getConstructTasksByTestId(test.getId()));
                grammarTests.add(test);
            }
            while (c.moveToNext());
        }
        c.close();
        return grammarTests;
    }

    @SuppressLint("Range")
    public List<ConstructSentenceTask> getConstructTasksByTestId(int id) {
        List<ConstructSentenceTask> constructTaskList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_CONSTRUCT_TASK + " ct JOIN " +
                DatabaseOpenHelper.TABLE_GRAMMAR_TEST_CONSTRUCT_TASKS + " gtct ON  ct." + DatabaseOpenHelper.KEY_ID + " = gtct." +
                DatabaseOpenHelper.KEY_CONSTRUCT_TASK_ID + " WHERE gtct." + DatabaseOpenHelper.KEY_GRAMMAR_TEST_ID + " = '" + id + "'";

        Cursor c = database.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                String correctSentence = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_SENTENCE));
                String wrongSentence = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_WRONG_SENTENCE));
                String translation = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_TRANSLATION));
                ConstructSentenceTask constructTask = new ConstructSentenceTask(correctSentence, translation, wrongSentence);
                constructTaskList.add(constructTask);
            } while (c.moveToNext());
        }
        c.close();

        return constructTaskList;
    }

    @SuppressLint("Range")
    public List<MissingWordSentenceTask> getMissingTasksByTestId(int id) {
        List<MissingWordSentenceTask> missingTaskList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_MISSING_TASK + " mt JOIN " +
                DatabaseOpenHelper.TABLE_GRAMMAR_TEST_MISSING_TASKS + " gtmt ON  mt." + DatabaseOpenHelper.KEY_ID + " = gtmt." +
                DatabaseOpenHelper.KEY_MISSING_TASK_ID + " WHERE gtmt." + DatabaseOpenHelper.KEY_GRAMMAR_TEST_ID + " = '" + id + "'";

        Cursor c = database.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                int taskId = c.getInt((c.getColumnIndex(DatabaseOpenHelper.KEY_ID)));
                String sentence = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_SENTENCE));
                String translation = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_TRANSLATION));
                String correctAnswer = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_ANSWER));
                MissingWordSentenceTask missingTask = new MissingWordSentenceTask(sentence, taskId, translation, correctAnswer);
                missingTask.setWrongAnswers(getWrongAnswersByMissingTaskId(missingTask.getId()));
                missingTaskList.add(missingTask);

            }
            while (c.moveToNext());
        }
        c.close();
        return missingTaskList;
    }
    @SuppressLint("Range")
    public List<WrongAnswer> getWrongAnswersByMissingTaskId(int id) {
        List<WrongAnswer> wrongAnswerList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_WRONG_ANSWER + " wa JOIN " +
                DatabaseOpenHelper.TABLE_MISSING_TASK_WRONG_ANSWERS + " mtwa ON  wa." + DatabaseOpenHelper.KEY_ID + " = mtwa." +
                DatabaseOpenHelper.KEY_WRONG_ANSWER_ID + " WHERE mtwa." + DatabaseOpenHelper.KEY_MISSING_TASK_ID + " = '" + id + "'";

        Cursor c = database.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                String answer = c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_ANSWER));
                WrongAnswer wrongAnswer = new WrongAnswer(answer);
                wrongAnswerList.add(wrongAnswer);
            }
            while (c.moveToNext());
        }

        c.close();
        return wrongAnswerList;
    }

    //-------------------------БЛОК ОБЩИХ ДЛЯ GRAMMAR, WORD и EXAM----------------------------------

    public ArrayList<Object> getTasksAsObject(boolean grammarFlag, boolean wordsFlag){
        ArrayList<Object> tasks = new ArrayList<>();
        String selectQuery;
        Cursor c;

        if (grammarFlag) {
            //Construct sentence tasks
            selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_CONSTRUCT_TASK;
            c = database.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    String correctSentence = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_SENTENCE));
                    String wrongSentence = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WRONG_SENTENCE));
                    String translation = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TRANSLATION));

                    ConstructSentenceTask constructTask = new ConstructSentenceTask(correctSentence, translation, wrongSentence);
                    tasks.add(constructTask);
                }
                while (c.moveToNext());
            }
            c.close();

            //Missing sentence tasks
            selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_MISSING_TASK;
            c = database.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    int taskId = c.getInt((c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_ID)));
                    String sentence = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_SENTENCE));
                    String translation = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TRANSLATION));
                    String correctAnswer = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_ANSWER));

                    MissingWordSentenceTask missingTask = new MissingWordSentenceTask(sentence, taskId, translation, correctAnswer);
                    missingTask.setWrongAnswers(getWrongAnswersByMissingTaskId(missingTask.getId()));
                    tasks.add(missingTask);
                }
                while (c.moveToNext());
            }
            c.close();
        }

        if (wordsFlag) {
            //Word tasks
            selectQuery = "SELECT  * FROM " + DatabaseOpenHelper.TABLE_WORD;
            c = database.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    int wordId = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_ID));
                    String engWord = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD));
                    String transcription = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TRANSCRIPTION));
                    String translation = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TRANSLATION));

                    Word word = new Word(wordId, engWord, transcription, translation);
                    tasks.add(word);
                }
                while (c.moveToNext());
            }
            c.close();
        }

        return tasks;
    }

    public void updateTestStatus(boolean grammarFlag, int id, int status) {
        if (status != 0) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_COMPLETION_STATUS, status);
            if (grammarFlag) {
                database.update(DatabaseOpenHelper.TABLE_GRAMMAR_TEST, values, "id = ?", new String[]{String.valueOf(id)});
            }
            else {
                database.update(DatabaseOpenHelper.TABLE_WORD_TEST, values, "id = ?", new String[]{String.valueOf(id)});
            }
        }
    }

    public boolean isBlockComplete(boolean grammarFlag) {
        int sum = 0;
        int count = 0;

        String selectQuery = "SELECT COUNT(*), SUM(status) FROM " + ((grammarFlag) ? DatabaseOpenHelper.TABLE_GRAMMAR_TEST : DatabaseOpenHelper.TABLE_WORD_TEST);

        Cursor c = database.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            count = c.getInt(0);
            sum = c.getInt(1);
        }
        c.close();

        return (sum >= count-1);
    }

    public void updateMiniExam(boolean grammarFlag, int status){
        if (status != 0) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_COMPLETION_STATUS, status);

            if (grammarFlag) {
                database.update(DatabaseOpenHelper.TABLE_GRAMMAR_TEST, values, "theme = ?", new String[]{"Мини-экзамен"});
            } else {
                database.update(DatabaseOpenHelper.TABLE_WORD_TEST, values, "theme = ?", new String[]{"Мини-экзамен"});
            }
        }
    }

    public boolean isAllComplete(){
        int grammarStatus = 0;
        int wordStatus = 0;

        String grammarQuery = "SELECT status FROM " + DatabaseOpenHelper.TABLE_GRAMMAR_TEST + " WHERE " + DatabaseOpenHelper.KEY_TEST_THEME + " = 'Мини-экзамен'";
        String wordQuery = "SELECT status FROM " + DatabaseOpenHelper.TABLE_WORD_TEST + " WHERE " + DatabaseOpenHelper.KEY_TEST_THEME + " = 'Мини-экзамен'";

        Cursor c = database.rawQuery(grammarQuery, null);
        if (c.moveToFirst()) {
            grammarStatus = c.getInt(0);
        }
        c.close();

        c = database.rawQuery(wordQuery, null);
        if (c.moveToFirst()) {
            wordStatus = c.getInt(0);
        }
        c.close();

        return (grammarStatus == 1 && wordStatus == 1);
    }

    //-------------------------------------БЛОК GAME------------------------------------------------

    public void addGameWords(List<Word> words) {
        for (Word w : words) {
            ContentValues values = new ContentValues();
            values.put(DatabaseOpenHelper.KEY_ID, w.id());
            values.put(DatabaseOpenHelper.KEY_WORD, w.word());
            values.put(DatabaseOpenHelper.KEY_TRANSCRIPTION, w.transcription());

            database.insert(DatabaseOpenHelper.TABLE_GAME_WORD, null, values);
        }
    }

    public Word getRandomGameWord(){
        Word word = null;

        String selectQuery = "SELECT * FROM " + DatabaseOpenHelper.TABLE_GAME_WORD + " ORDER BY RANDOM() LIMIT 1";
        Cursor c = database.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            int wordId = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_ID));
            String engWord = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD));
            String transcription = c.getString(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TRANSCRIPTION));
            word = new Word(wordId, engWord, transcription, "");
        }
        c.close();

        return word;
    }

    //-------------------------------------БЛОК STATS-----------------------------------------------

    public void initStats() {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_ID, 1);

        values.put(DatabaseOpenHelper.KEY_GRAMMAR_TOTAL_ANSWERS, 0);
        values.put(DatabaseOpenHelper.KEY_GRAMMAR_CORRECT_ANSWERS, 0);
        values.put(DatabaseOpenHelper.KEY_GRAMMAR_TOTAL_TESTS, 0);
        values.put(DatabaseOpenHelper.KEY_GRAMMAR_SUCCESSFUL_TESTS, 0);
        values.put(DatabaseOpenHelper.KEY_GRAMMAR_MINI_EXAM_MARK, 0);
        values.put(DatabaseOpenHelper.KEY_GRAMMAR_MINI_EXAM_COMPLETIONS, 0);

        values.put(DatabaseOpenHelper.KEY_WORD_TOTAL_ANSWERS, 0);
        values.put(DatabaseOpenHelper.KEY_WORD_CORRECT_ANSWERS, 0);
        values.put(DatabaseOpenHelper.KEY_WORD_TOTAL_TESTS, 0);
        values.put(DatabaseOpenHelper.KEY_WORD_SUCCESSFUL_TESTS, 0);
        values.put(DatabaseOpenHelper.KEY_WORD_MINI_EXAM_MARK, 0);
        values.put(DatabaseOpenHelper.KEY_WORD_MINI_EXAM_COMPLETIONS, 0);

        values.put(DatabaseOpenHelper.KEY_EXAM_MARK, 0);
        values.put(DatabaseOpenHelper.KEY_EXAM_COMPLETIONS, 0);

        values.put(DatabaseOpenHelper.KEY_TOTAL_GAMES, 0);
        values.put(DatabaseOpenHelper.KEY_SUCCESSFUL_GAMES, 0);
        values.put(DatabaseOpenHelper.KEY_TOTAL_TRIES, 0);

        values.put(DatabaseOpenHelper.KEY_MESSAGES_SENT, 0);

        database.insert(DatabaseOpenHelper.TABLE_STATS, null, values);
    }
    @SuppressLint("Recycle")
    public String getProgressStats(boolean grammar) {
        String table = (grammar) ? DatabaseOpenHelper.TABLE_GRAMMAR_TEST : DatabaseOpenHelper.TABLE_WORD_TEST;

        int total = 0;
        int completed = 0;

        String countQuery = "SELECT COUNT(*) FROM " + table;
        String completedQuery = "SELECT COUNT(*) FROM " + table + " WHERE " + DatabaseOpenHelper.KEY_COMPLETION_STATUS + " = 1";

        Cursor c = database.rawQuery(countQuery, null);
        if (c.moveToFirst()) total = c.getInt(0);
        c = database.rawQuery(completedQuery, null);
        if (c.moveToFirst()) completed = c.getInt(0);
        c.close();

        return (completed + "/" + total);
    }

    public Stats getRequiredStats(String statsType) {
        Stats stats = new Stats();

        String selectQuery = "SELECT * FROM " + DatabaseOpenHelper.TABLE_STATS + " WHERE " + DatabaseOpenHelper.KEY_ID + " = 1";
        Cursor c = database.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            switch (statsType) {
                case "grammar":
                    stats.totalAnswers = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_GRAMMAR_TOTAL_ANSWERS));
                    stats.correctAnswers = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_GRAMMAR_CORRECT_ANSWERS));
                    stats.totalTests = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_GRAMMAR_TOTAL_TESTS));
                    stats.successfulTests = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_GRAMMAR_SUCCESSFUL_TESTS));
                    stats.miniExamMark = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_GRAMMAR_MINI_EXAM_MARK));
                    stats.miniExamCompletions = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_GRAMMAR_MINI_EXAM_COMPLETIONS));
                    break;

                case "word":
                    stats.totalAnswers = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD_TOTAL_ANSWERS));
                    stats.correctAnswers = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD_CORRECT_ANSWERS));
                    stats.totalTests = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD_TOTAL_TESTS));
                    stats.successfulTests = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD_SUCCESSFUL_TESTS));
                    stats.miniExamMark = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD_MINI_EXAM_MARK));
                    stats.miniExamCompletions = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_WORD_MINI_EXAM_COMPLETIONS));
                    break;

                case "exam":
                    stats.examMark = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_EXAM_MARK));
                    stats.examCompletions = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_EXAM_COMPLETIONS));
                    break;

                case "game":
                    stats.totalGames = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TOTAL_GAMES));
                    stats.successfulGames = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_SUCCESSFUL_GAMES));
                    stats.totalTries = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_TOTAL_TRIES));
                    break;

                case "chat":
                    stats.messagesSent = c.getInt(c.getColumnIndexOrThrow(DatabaseOpenHelper.KEY_MESSAGES_SENT));
                    break;

                default:
                    break;
            }
        }

        c.close();
        return stats;
    }

    public void updateRequiredStats(Stats stats, String statsType) {
        ContentValues values = new ContentValues();

        switch (statsType) {
            case "grammar":
                values.put(DatabaseOpenHelper.KEY_GRAMMAR_TOTAL_ANSWERS, stats.totalAnswers);
                values.put(DatabaseOpenHelper.KEY_GRAMMAR_CORRECT_ANSWERS, stats.correctAnswers);
                values.put(DatabaseOpenHelper.KEY_GRAMMAR_TOTAL_TESTS, stats.totalTests);
                values.put(DatabaseOpenHelper.KEY_GRAMMAR_SUCCESSFUL_TESTS, stats.successfulTests);
                values.put(DatabaseOpenHelper.KEY_GRAMMAR_MINI_EXAM_MARK, stats.miniExamMark);
                values.put(DatabaseOpenHelper.KEY_GRAMMAR_MINI_EXAM_COMPLETIONS, stats.miniExamCompletions);
                break;

            case "word":
                values.put(DatabaseOpenHelper.KEY_WORD_TOTAL_ANSWERS, stats.totalAnswers);
                values.put(DatabaseOpenHelper.KEY_WORD_CORRECT_ANSWERS, stats.correctAnswers);
                values.put(DatabaseOpenHelper.KEY_WORD_TOTAL_TESTS, stats.totalTests);
                values.put(DatabaseOpenHelper.KEY_WORD_SUCCESSFUL_TESTS, stats.successfulTests);
                values.put(DatabaseOpenHelper.KEY_WORD_MINI_EXAM_MARK, stats.miniExamMark);
                values.put(DatabaseOpenHelper.KEY_WORD_MINI_EXAM_COMPLETIONS, stats.miniExamCompletions);
                break;

            case "exam":
                values.put(DatabaseOpenHelper.KEY_EXAM_MARK, stats.examMark);
                values.put(DatabaseOpenHelper.KEY_EXAM_COMPLETIONS, stats.examCompletions);
                break;

            case "game":
                values.put(DatabaseOpenHelper.KEY_TOTAL_GAMES, stats.totalGames);
                values.put(DatabaseOpenHelper.KEY_SUCCESSFUL_GAMES, stats.successfulGames);
                values.put(DatabaseOpenHelper.KEY_TOTAL_TRIES, stats.totalTries);
                break;

            case "chat":
                values.put(DatabaseOpenHelper.KEY_MESSAGES_SENT, stats.messagesSent);
                break;

            default:
                break;
        }

        database.update(DatabaseOpenHelper.TABLE_STATS, values, "id = ?", new String[]{String.valueOf(1)});
    }
}
