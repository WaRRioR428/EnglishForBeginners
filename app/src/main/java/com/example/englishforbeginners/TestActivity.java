package com.example.englishforbeginners;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.englishforbeginners.database.DatabaseAccess;
import com.example.englishforbeginners.entity.ConstructSentenceTask;
import com.example.englishforbeginners.entity.MissingWordSentenceTask;
import com.example.englishforbeginners.entity.Stats;
import com.example.englishforbeginners.entity.Word;
import com.example.englishforbeginners.entity.WrongAnswer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestActivity extends AppCompatActivity {

    private TestActivity context;
    private ProgressBar progressBar;
    private TextView taskText;
    private TextView transText;
    private TextView question;
    private TextView correctAnswerTextView;
    private Button buttonApply;
    private Button buttonClear;

    private List<Object> tasks;
    private String[] randomWordOrder;
    private List<Word> wrongWords;

    private TextToSpeech TTS;
    private boolean ttsEnabled ;
    private boolean ttsAvaliable;
    private String ttsText;

    private boolean infoAvaliable;

    private String punktMark;
    private String correctAnswer;
    private String userAnswer;

    private int mark;
    private int counter;
    private String testTheme;
    private String imageName;
    private boolean grammarFlag;
    private boolean isExam;
    private int testID;

    MediaPlayer mpCorrect;
    MediaPlayer mpWrong;
    MediaPlayer mpSuccess;
    MediaPlayer mpFail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Bundle extras = this.getIntent().getExtras();
        assert extras != null;
        tasks = (List<Object>) extras.getSerializable("tasks");
        testTheme = extras.getString("theme");
        imageName = extras.getString("theoryImageName");
        isExam = (testTheme.equals("Экзамен") || testTheme.equals("Мини-экзамен"));
        if (!isExam) {
            testID = extras.getInt("testID");
        }
        setGrammarFlag();

        ConstraintLayout layout = findViewById(R.id.testLayout);
        if (isExam) {
            if (testTheme.equals("Экзамен")) {
                layout.setBackgroundResource(R.drawable.exam);
            }
            else {
                layout.setBackgroundResource(R.drawable.mini_exam);
            }
        }
        else {
            if (grammarFlag) {
                layout.setBackgroundResource(R.drawable.grammar_test);
            }
            else {
                layout.setBackgroundResource(R.drawable.word_test);
            }
        }

        mpCorrect = MediaPlayer.create(this, R.raw.correct);
        mpWrong = MediaPlayer.create(this, R.raw.wrong);
        mpSuccess = MediaPlayer.create(this, R.raw.success);
        mpFail = MediaPlayer.create(this, R.raw.fail);

        this.setTitle(testTheme);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(tasks.size());

        taskText = findViewById(R.id.task);
        transText = findViewById(R.id.trans);
        question = findViewById(R.id.question);
        correctAnswerTextView = findViewById(R.id.correctAnswer);
        correctAnswerTextView.setVisibility(View.GONE);
        context = this;

        buttonClear = findViewById(R.id.buttonClear);
        buttonApply = findViewById(R.id.buttonApply);
        View.OnClickListener onClickButtonApply = v -> {
            userAnswer = transText.getText().toString().trim();
            checkIfAnswerIsCorrect();
        };
        buttonApply.setOnClickListener(onClickButtonApply);

        infoAvaliable = true;
        ttsAvaliable = true;
        TTS = new TextToSpeech(this, initStatus -> {
            if (initStatus == TextToSpeech.SUCCESS) {
                if (TTS.isLanguageAvailable(new Locale(Locale.getDefault().getLanguage()))
                        == TextToSpeech.LANG_AVAILABLE) {
                    TTS.setLanguage(Locale.ENGLISH);
                }
                TTS.setPitch(1.3f);
                TTS.setSpeechRate(0.7f);
                ttsEnabled = true;
            } else if (initStatus == TextToSpeech.ERROR) {
                Toast.makeText(context, R.string.tts_error, Toast.LENGTH_LONG).show();
                ttsEnabled = false;
            }
        });

        if (!isExam && !grammarFlag) {
            wrongWords = new ArrayList<>();
            for (Object task : tasks) {
                wrongWords.add((Word) task);
            }
        }
        else {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
            databaseAccess.open();
            wrongWords = databaseAccess.getAllWords();
            databaseAccess.close();
        }
        randomWordOrder = new String[4];

        counter = 0;
        mark = 0;

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        createTestFlow();
    }

    private void setGrammarFlag(){
        for (Object task: tasks) {
            if (task instanceof ConstructSentenceTask || task instanceof MissingWordSentenceTask) {
                grammarFlag = true;
                return;
            }
        }
        grammarFlag = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_listen).setVisible(ttsAvaliable);
        menu.findItem(R.id.action_info).setVisible(infoAvaliable);
        return true;
    }

    private static int getDrawableByString(String name) {
        try {
            Field idField = R.drawable.class.getDeclaredField(name);
            return idField.getInt(idField);
        }
        catch (Exception e) {
            Logger.getLogger(TestActivity.class.getName()).log(Level.SEVERE, e.getMessage());
            return -1;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = (displayMetrics.heightPixels) * 2 / 3;

            AlertDialog.Builder alertTheory = new AlertDialog.Builder(context);
            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.theory_alert_dialog, null);
            ImageView theoryImage = view.findViewById(R.id.theoryDialogImage);

            int resID = getDrawableByString(imageName);
            if (resID != -1) {
                theoryImage.setImageResource(resID);
            } else {
                theoryImage.setVisibility(View.GONE);
            }
            theoryImage.getLayoutParams().height = height;
            theoryImage.setScaleType(ImageView.ScaleType.FIT_XY);

            alertTheory.setView(view);
            AlertDialog dialog = alertTheory.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(view, 0, 0, 0, 0);
            dialog.show();
        }
        else if (item.getItemId() == R.id.action_listen) {
            speak(ttsText);
        }
        else {
            getOnBackPressedDispatcher().onBackPressed();
        }

        return true;
    }

    public void speak(String text) {
        if (!ttsEnabled) return;
        String utteranceId = String.valueOf(this.hashCode());
        TTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @SuppressLint("SetTextI18n")
    private void createTestFlow(){
        if (counter < tasks.size()) {
            progressBar.setProgress(counter);
            Object task = tasks.get(counter);
            if (task instanceof ConstructSentenceTask) {
                createConstructTask((ConstructSentenceTask) task);
            }
            else if (task instanceof MissingWordSentenceTask){
                createMissingTask((MissingWordSentenceTask) task);
            }
            else {
                createWordTask((Word) task);
            }
        }
        else {
            taskText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            question.setVisibility(View.GONE);
            buttonApply.setVisibility(View.GONE);
            buttonClear.setVisibility(View.GONE);
            ScrollView scroll = findViewById(R.id.scrollId);
            scroll.setVisibility(View.GONE);

            ttsAvaliable = false;
            infoAvaliable = false;
            invalidateOptionsMenu();

            transText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
            transText.setGravity(Gravity.CENTER | Gravity.BOTTOM);

            if (isExam) {
                counter = mark;
                if (mark == 0) {
                    mark++;
                }
                mark = (mark + 5 - 1) / 5;

                transText.setText(testTheme + " окончен.\n Ваша оценка: \"" + mark + "\"");

                if (mark < 3) {
                    transText.setBackgroundColor(Color.parseColor("#F8F509"));
                    mpFail.start();
                }
                else {
                    transText.setBackgroundColor(Color.parseColor("#7BEB3B"));
                    mpSuccess.start();
                }
            }
            else {
                if (mark < (tasks.size() - 5)) {
                    transText.setBackgroundColor(Color.parseColor("#F8F509"));
                    transText.setText("Вы допустили слишком много ошибок!\n Рекомендуем повторить тест.");
                    mpFail.start();
                }
                else {
                    transText.setBackgroundColor(Color.parseColor("#7BEB3B"));
                    transText.setText("Поздравляем!\n Вы справились!");
                    mpSuccess.start();
                }
            }

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                mpCorrect.release();
                mpWrong.release();
                mpSuccess.release();
                mpFail.release();

                buttonClear.setVisibility(View.VISIBLE);
                buttonClear.setEnabled(true);
                Button btnBack = findViewById(R.id.buttonClear);
                btnBack.setText((testTheme.equals("Экзамен")) ? "Вернуться в меню" : "Назад к списку");
                View.OnClickListener oclBtnBack = v -> {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                };
                btnBack.setOnClickListener(oclBtnBack);
            }, 4000);

            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
            databaseAccess.open();
            String statsType = (grammarFlag) ? "grammar" : "word";
            Stats stats;
            if (testTheme.equals("Мини-экзамен")) {
                databaseAccess.updateMiniExam(grammarFlag, (mark > 2 ? 1 : 0));

                stats = databaseAccess.getRequiredStats(statsType);
                stats.totalAnswers = stats.totalAnswers + 25;
                stats.correctAnswers = stats.correctAnswers + counter;
                stats.totalTests++;
                if (mark > 2) stats.successfulTests++;
                if (mark > stats.miniExamMark) {
                    stats.miniExamMark = mark;
                }
                stats.miniExamCompletions++;
                databaseAccess.updateRequiredStats(stats, statsType);
            }
            else if (!(testTheme.equals("Экзамен"))) {
                databaseAccess.updateTestStatus(grammarFlag, testID, (mark > (tasks.size() - 5)) ? 1 : 0);

                stats = databaseAccess.getRequiredStats(statsType);
                stats.totalAnswers = stats.totalAnswers + tasks.size();
                stats.correctAnswers = stats.correctAnswers + mark;
                stats.totalTests++;
                if (mark > (tasks.size() - 5)) stats.successfulTests++;
                databaseAccess.updateRequiredStats(stats, statsType);
            }
            else {
                stats = databaseAccess.getRequiredStats("exam");
                if (mark > stats.examMark) {
                    stats.examMark = mark;
                }
                stats.examCompletions++;
                databaseAccess.updateRequiredStats(stats, "exam");
            }
            databaseAccess.close();
        }
    }

    private void checkIfAnswerIsCorrect(){
        counter++;
        buttonApply.setEnabled(false);
        buttonClear.setEnabled(false);
        LinearLayout layout = findViewById(R.id.examLayout);
        layout.removeAllViews();
        if (counter != 0) {
            if (userAnswer.equals(correctAnswer)) {
                mark++;
                mpCorrect.start();
                transText.setBackgroundColor(Color.parseColor("#7BEB3B"));
            }
            else {
                mpWrong.start();
                transText.setBackgroundColor(Color.RED);
            }

            if (!correctAnswer.equals(transText.getText().toString().trim())) {
                correctAnswerTextView.setVisibility(View.VISIBLE);
                correctAnswerTextView.setText(correctAnswer);
            }
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            buttonApply.setEnabled(true);
            buttonClear.setEnabled(true);
            transText.setBackgroundColor(Color.WHITE);
            correctAnswerTextView.setVisibility(View.GONE);
            createTestFlow();
        }, 2500);
    }

    private void createConstructTask(ConstructSentenceTask cs){
        final boolean[] firstPress = new boolean[] {true};

        buttonClear.setVisibility(View.VISIBLE);
        buttonApply.setVisibility(View.VISIBLE);
        buttonApply.setEnabled(false);
        buttonClear.setEnabled(true);
        View.OnClickListener onClickButtonClear = v -> {
            buttonApply.setEnabled(false);
            createConstructTask(cs);
        };
        buttonClear.setOnClickListener(onClickButtonClear);

        question.setText("Составьте слова в предложение:");
        String sentenceString = cs.translation();
        taskText.setText(sentenceString);
        punktMark = sentenceString.substring(sentenceString.length() - 1);
        transText.setText(punktMark);
        correctAnswer = cs.correctSentence().substring(0, 1).toUpperCase() + cs.correctSentence().substring(1) + punktMark;

        ttsAvaliable = false;
        infoAvaliable = true;
        invalidateOptionsMenu();

        ArrayList<String> words = new ArrayList<>();
        words.addAll(Arrays.asList(cs.correctSentence().split(" ")));
        words.addAll(Arrays.asList(cs.wrongSentence().split(" ")));

        ArrayList<Integer> wordPositions = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            wordPositions.add(i);
        }
        Collections.shuffle(wordPositions);

        LinearLayout parent = findViewById(R.id.examLayout);
        if (parent.getChildCount() > 0) {
            parent.removeAllViews();
        }

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        for (int i = 0; i < words.size(); i++) {
            if (i % 3 == 0) {
                parent.addView(buttonLayout);
                buttonLayout = new LinearLayout(this);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.CENTER);
            }

            Button button = new Button(this);
            button.setHeight(150);
            button.setWidth(300);
            button.setText(words.get(wordPositions.get(i)));
            button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_button_word_list));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(20);
            params.setMarginEnd(20);
            params.setMargins(20, 20, 20, 20);
            button.setLayoutParams(params);

            @SuppressLint("SetTextI18n")
            View.OnClickListener onClickWordButton = v -> {
                Button wordButton = (Button) v;
                String buttonText = wordButton.getText().toString();
                if (firstPress[0]) {
                    buttonText = buttonText.substring(0, 1).toUpperCase() + buttonText.substring(1);
                    firstPress[0] = false;
                }
                String temp = transText.getText().toString();
                temp = temp.substring(0, temp.length() - 1);
                transText.setText(temp + " " + buttonText + punktMark);
                wordButton.setVisibility(View.GONE);
                buttonApply.setEnabled(true);
            };
            button.setOnClickListener(onClickWordButton);
            buttonLayout.addView(button);
        }

        parent.addView(buttonLayout);
    }

    private void createMissingTask(MissingWordSentenceTask mw){
        ttsAvaliable = false;
        infoAvaliable = true;
        invalidateOptionsMenu();

        buttonClear.setVisibility(View.GONE);
        buttonApply.setVisibility(View.GONE);
        buttonApply.setEnabled(false);
        buttonClear.setEnabled(false);

        question.setText("Заполните пропуск:");
        taskText.setText(mw.getSentence());
        transText.setText(mw.getTranslation());
        correctAnswer = mw.getCorrectAnswer();

        ArrayList<Integer> answerPositions = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        for (int i = 0; i < mw.getWrongAnswers().size() + 1; i++) {
            answerPositions.add(i);
        }
        for (WrongAnswer wa : mw.getWrongAnswers()) {
            answers.add(wa.answer());
        }
        answers.add(mw.getCorrectAnswer());
        Collections.shuffle(answerPositions);

        LinearLayout parent = findViewById(R.id.examLayout);
        if (parent.getChildCount() > 0) {
            parent.removeAllViews();
        }

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        for (int i = 0; i < answers.size(); i++) {
            if (i % 2 == 0) {
                parent.addView(buttonLayout);
                buttonLayout = new LinearLayout(this);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.CENTER);
            }

            Button button = new Button(this);
            button.setText(answers.get(answerPositions.get(i)));
            button.setHeight(180);
            button.setWidth(350);
            button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_button_word_list));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //params.weight = 1.0f;
            params.setMarginStart(50);
            params.setMarginEnd(50);
            params.setMargins(50, 50, 50, 50);
            button.setLayoutParams(params);

            View.OnClickListener onClickWordButton = v -> {
                Button wordButton = (Button) v;
                userAnswer = wordButton.getText().toString();
                String[] answerArray = userAnswer.split(",");
                String userSentence;
                if (answerArray.length > 1) {
                    userSentence = taskText.getText().toString().replaceFirst("_+ *", (answerArray[0].trim().equals("-")) ? "" : answerArray[0].trim() + " ");
                    userSentence = userSentence.replaceFirst("_+ *", (answerArray[1].trim().equals("-")) ? "" : answerArray[1].trim() + " ");
                }
                else {
                    userSentence = taskText.getText().toString().replaceFirst("_+ *", (userAnswer.equals("-")) ? "" : userAnswer + " ");
                }
                if (userSentence.charAt(userSentence.length() - 2) == ' ') {
                    punktMark = userSentence.substring(userSentence.length() - 1);
                    userSentence = userSentence.substring(0, userSentence.length() - 2) + punktMark;
                }
                transText.setText(userSentence);
                checkIfAnswerIsCorrect();
            };

            button.setOnClickListener(onClickWordButton);
            buttonLayout.addView(button);
        }

        parent.addView(buttonLayout);
    }

    private void createWordTask(Word word){
        buttonClear.setVisibility(View.GONE);
        buttonApply.setVisibility(View.GONE);
        buttonApply.setEnabled(false);
        buttonClear.setEnabled(false);

        question.setText("Выберите верный перевод:");
        taskText.setText(word.word());
        transText.setText(word.transcription());
        correctAnswer = word.translation();

        ttsText = word.word();
        ttsAvaliable = true;
        infoAvaliable = isExam;
        invalidateOptionsMenu();

        Random random = new Random();
        int randomValue = random.nextInt(4);

        Collections.shuffle(wrongWords);
        int i = 0;
        int index = 0;
        while (i < 4) {
            if (i == randomValue) {
                randomWordOrder[i] = correctAnswer;
                i++;
            }
            else {
                for (int j = index; j < wrongWords.size(); j++) {
                    String randomWord = wrongWords.get(j).translation();
                    if (!randomWord.equals(correctAnswer)) {
                        randomWordOrder[i] = randomWord;
                        i++;
                        index = j + 1;
                        break;
                    }
                }
            }
        }

        LinearLayout parent = findViewById(R.id.examLayout);
        if (parent.getChildCount() > 0) {
            parent.removeAllViews();
        }

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        for (i = 0; i < 4; i++) {
            if (i % 2 == 0) {
                parent.addView(buttonLayout);
                buttonLayout = new LinearLayout(this);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.CENTER);
            }

            Button button = new Button(this);
            button.setText(randomWordOrder[i]);
            button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_button_word_list));
            button.setHeight(180);
            button.setWidth(350);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            //params.weight = 5.0f;
            params.setMarginStart(50);
            params.setMarginEnd(50);
            params.setMargins(50, 50, 50, 50);
            button.setLayoutParams(params);

            View.OnClickListener onClickWordButton = v -> {
                Button wordButton = (Button) v;
                userAnswer = wordButton.getText().toString();
                transText.setText(userAnswer);
                checkIfAnswerIsCorrect();
            };

            button.setOnClickListener(onClickWordButton);
            buttonLayout.addView(button);
        }

        parent.addView(buttonLayout);
    }
}