package com.example.englishforbeginners.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseOpenHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "englishManager";

    public static final String TABLE_WORD_TEST = "wordTests";
    public static final String TABLE_WORD = "words";
    public static final String TABLE_WORD_TO_TEST = "wordsToTest";

    public static final String TABLE_GRAMMAR_TEST = "grammarTests";
    public static final String TABLE_CONSTRUCT_TASK = "constructTask";
    public static final String TABLE_MISSING_TASK = "missingTask";
    public static final String TABLE_WRONG_ANSWER = "wrongAnswer";
    public static final String TABLE_MISSING_TASK_WRONG_ANSWERS = "missingTaskWrongAnswers";
    public static final String TABLE_GRAMMAR_TEST_CONSTRUCT_TASKS = "grammarTestConstructTasks";
    public static final String TABLE_GRAMMAR_TEST_MISSING_TASKS = "grammarTestMissingTasks";

    public static final String TABLE_GAME_WORD = "gameWords";

    public static final String TABLE_STATS = "stats";

    public static final String KEY_ID = "id";
    public static final String KEY_TEST_THEME = "theme";
    public static final String KEY_COMPLETION_STATUS = "status";

    public static final String KEY_WORD = "word";
    public static final String KEY_TRANSLATION = "translation";
    public static final String KEY_TRANSCRIPTION = "transcription";
    public static final String KEY_WORD_ID = "word_id";
    public static final String KEY_WORD_TEST_ID = "word_test_id";

    public static final String KEY_SENTENCE = "sentence";
    public static final String KEY_WRONG_SENTENCE = "wrong_sentence";
    public static final String KEY_ANSWER = "answer";
    public static final String KEY_CONSTRUCT_TASK_ID = "construct_task_id";
    public static final String KEY_MISSING_TASK_ID = "missing_task_id";
    public static final String KEY_WRONG_ANSWER_ID = "wrong_answer_id";
    public static final String KEY_GRAMMAR_TEST_ID = "grammar_test_id";

    public static final String KEY_GRAMMAR_TOTAL_ANSWERS = "grammar_total_answers";
    public static final String KEY_GRAMMAR_CORRECT_ANSWERS = "grammar_correct_answers";;
    public static final String KEY_GRAMMAR_TOTAL_TESTS = "grammar_total_tests";
    public static final String KEY_GRAMMAR_SUCCESSFUL_TESTS = "grammar_successful_tests";
    public static final String KEY_GRAMMAR_MINI_EXAM_MARK = "grammar_mini_exam_mark";
    public static final String KEY_GRAMMAR_MINI_EXAM_COMPLETIONS = "grammar_mini_exam_completions";

    public static final String KEY_WORD_TOTAL_ANSWERS = "word_total_answers";
    public static final String KEY_WORD_CORRECT_ANSWERS = "word_correct_answers";
    public static final String KEY_WORD_TOTAL_TESTS = "word_total_tests";
    public static final String KEY_WORD_SUCCESSFUL_TESTS = "word_successful_tests";
    public static final String KEY_WORD_MINI_EXAM_MARK = "word_mini_exam_mark";
    public static final String KEY_WORD_MINI_EXAM_COMPLETIONS = "word_mini_exam_completions";

    public static final String KEY_EXAM_MARK = "exam_mark";
    public static final String KEY_EXAM_COMPLETIONS = "exam_completions";

    public static final String KEY_TOTAL_GAMES = "total_games";
    public static final String KEY_SUCCESSFUL_GAMES = "successful_games";
    public static final String KEY_TOTAL_TRIES = "total_tries";

    public static final String KEY_MESSAGES_SENT = "messages_sent";


    private static final String CREATE_TABLE_WORD_TEST = "CREATE TABLE "
            + TABLE_WORD_TEST + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TEST_THEME
            + " TEXT," + KEY_COMPLETION_STATUS + " INTEGER" + ")";

    private static final String CREATE_TABLE_WORD = "CREATE TABLE " + TABLE_WORD
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_WORD + " TEXT,"
            + KEY_TRANSLATION + " TEXT," + KEY_TRANSCRIPTION + " TEXT" + ")";

    private static final String CREATE_TABLE_WORD_TO_TEST = "CREATE TABLE "
            + TABLE_WORD_TO_TEST + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_WORD_ID + " INTEGER," + KEY_WORD_TEST_ID + " INTEGER"
            + ")";


    private static final String CREATE_TABLE_CONSTRUCT_TASK = "CREATE TABLE " + TABLE_CONSTRUCT_TASK
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TRANSLATION + " TEXT,"
            + KEY_SENTENCE + " TEXT," + KEY_WRONG_SENTENCE + " TEXT" + ")";

    private static final String CREATE_TABLE_MISSING_TASK = "CREATE TABLE " + TABLE_MISSING_TASK
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TRANSLATION + " TEXT,"
            + KEY_SENTENCE + " TEXT," + KEY_ANSWER + " TEXT" + ")";
    private static final String CREATE_TABLE_WRONG_ANSWER = "CREATE TABLE " + TABLE_WRONG_ANSWER
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ANSWER + " TEXT" + ")";

    private static final String CREATE_TABLE_MISSING_TASK_WRONG_ANSWERS = "CREATE TABLE "
            + TABLE_MISSING_TASK_WRONG_ANSWERS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_MISSING_TASK_ID + " INTEGER," + KEY_WRONG_ANSWER_ID + " INTEGER"
            + ")";

    private static final String CREATE_TABLE_GRAMMAR_TEST = "CREATE TABLE "
            + TABLE_GRAMMAR_TEST + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TEST_THEME
            + " TEXT," + KEY_COMPLETION_STATUS + " INTEGER" + ")";

    private static final String CREATE_TABLE_GRAMMAR_TEST_CONSTRUCT_TASKS = "CREATE TABLE "
            + TABLE_GRAMMAR_TEST_CONSTRUCT_TASKS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_GRAMMAR_TEST_ID + " INTEGER," + KEY_CONSTRUCT_TASK_ID + " INTEGER"
            + ")";

    private static final String CREATE_TABLE_GRAMMAR_TEST_MISSING_TASKS = "CREATE TABLE "
            + TABLE_GRAMMAR_TEST_MISSING_TASKS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_GRAMMAR_TEST_ID + " INTEGER," + KEY_MISSING_TASK_ID + " INTEGER"
            + ")";


    private static final String CREATE_TABLE_GAME_WORD = "CREATE TABLE " + TABLE_GAME_WORD
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_WORD + " TEXT,"
            + KEY_TRANSCRIPTION + " TEXT" + ")";


    private static final String CREATE_TABLE_STATS = "CREATE TABLE " + TABLE_STATS
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_GRAMMAR_TOTAL_ANSWERS + " INTEGER,"
            + KEY_GRAMMAR_CORRECT_ANSWERS + " INTEGER," + KEY_GRAMMAR_TOTAL_TESTS + " INTEGER,"
            + KEY_GRAMMAR_SUCCESSFUL_TESTS + " INTEGER," + KEY_GRAMMAR_MINI_EXAM_MARK + " INTEGER,"
            + KEY_GRAMMAR_MINI_EXAM_COMPLETIONS + " INTEGER," + KEY_WORD_TOTAL_ANSWERS + " INTEGER,"
            + KEY_WORD_CORRECT_ANSWERS + " INTEGER," + KEY_WORD_TOTAL_TESTS + " INTEGER,"
            + KEY_WORD_SUCCESSFUL_TESTS + " INTEGER," + KEY_WORD_MINI_EXAM_MARK + " INTEGER,"
            + KEY_WORD_MINI_EXAM_COMPLETIONS + " INTEGER," + KEY_EXAM_MARK + " INTEGER,"
            + KEY_EXAM_COMPLETIONS + " INTEGER," + KEY_TOTAL_GAMES + " INTEGER,"
            + KEY_SUCCESSFUL_GAMES + " INTEGER," + KEY_TOTAL_TRIES + " INTEGER,"
            + KEY_MESSAGES_SENT + " INTEGER" + ")";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORD_TEST);
        db.execSQL(CREATE_TABLE_WORD);
        db.execSQL(CREATE_TABLE_WORD_TO_TEST);

        db.execSQL(CREATE_TABLE_CONSTRUCT_TASK);
        db.execSQL(CREATE_TABLE_MISSING_TASK);
        db.execSQL(CREATE_TABLE_GRAMMAR_TEST);
        db.execSQL(CREATE_TABLE_GRAMMAR_TEST_CONSTRUCT_TASKS);
        db.execSQL(CREATE_TABLE_GRAMMAR_TEST_MISSING_TASKS);
        db.execSQL(CREATE_TABLE_WRONG_ANSWER);
        db.execSQL(CREATE_TABLE_MISSING_TASK_WRONG_ANSWERS);

        db.execSQL(CREATE_TABLE_GAME_WORD);

        db.execSQL(CREATE_TABLE_STATS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORD_TEST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORD_TO_TEST);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSTRUCT_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MISSING_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRAMMAR_TEST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRAMMAR_TEST_CONSTRUCT_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRAMMAR_TEST_MISSING_TASKS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WRONG_ANSWER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MISSING_TASK_WRONG_ANSWERS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_WORD);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);

        onCreate(db);
    }
}
