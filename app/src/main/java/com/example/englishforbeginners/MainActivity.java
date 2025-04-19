package com.example.englishforbeginners;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.collections4.CollectionUtils;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.englishforbeginners.database.DatabaseAccess;
import com.example.englishforbeginners.entity.GrammarTest;
import com.example.englishforbeginners.entity.Word;
import com.example.englishforbeginners.entity.WordTest;
import com.example.englishforbeginners.parser.XmlFilesParser;

public class MainActivity extends AppCompatActivity {
    public static final String LOG = "MainActivity";
    private DatabaseAccess databaseAccess;
    private MainActivity mainContext;
    private AlertDialog loadDialog;
    private boolean isAllComplete;
    private List<Object> randomTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
        loadMenu();
    }

    private void loadMenu(){
        mainContext = this;

        this.databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        if (CollectionUtils.isEmpty(databaseAccess.getAllWordTests()) || CollectionUtils.isEmpty(databaseAccess.getAllGrammarTests())) {
            initLoadDialog(this);
        }
        String wordProgress = databaseAccess.getProgressStats(false);
        String grammarProgress = databaseAccess.getProgressStats(true);
        isAllComplete = databaseAccess.isAllComplete();
        databaseAccess.close();

        ImageButton chat = findViewById(R.id.buttonChat);
        View.OnClickListener onClickChat = v -> {
            Intent intent = new Intent(mainContext, ChatActivity.class);
            mainActivityResult.launch(intent);
        };
        chat.setOnClickListener(onClickChat);

        ImageButton stats = findViewById(R.id.buttonStats);
        View.OnClickListener onClickStats = v -> {
            Intent intent = new Intent(mainContext, StatsActivity.class);
            mainActivityResult.launch(intent);
        };
        stats.setOnClickListener(onClickStats);

        Button wordTasksList = findViewById(R.id.buttonWords);
        SpannableString ss = new SpannableString(getResources().getString(R.string.button_word_tests) + "\n" + wordProgress);
        ss.setSpan(new RelativeSizeSpan(0.5f), (ss.length() - wordProgress.length()), ss.length(), 0);
        wordTasksList.setText(ss);
        View.OnClickListener onClickWordTasksList = v -> {
            Intent intent = new Intent(mainContext, TaskListActivity.class);
            intent.putExtra("wordsFlag", true);
            mainActivityResult.launch(intent);
        };
        wordTasksList.setOnClickListener(onClickWordTasksList);

        Button grammarTasksList = findViewById(R.id.buttonGrammar);
        ss = new SpannableString(getResources().getString(R.string.button_grammar_tests) + "\n" + grammarProgress);
        ss.setSpan(new RelativeSizeSpan(0.5f), (ss.length() - grammarProgress.length()), ss.length(), 0);
        grammarTasksList.setText(ss);
        View.OnClickListener onClickGrammarTasksList = v -> {
            Intent intent = new Intent(mainContext, TaskListActivity.class);
            intent.putExtra("wordsFlag", false);
            mainActivityResult.launch(intent);
        };
        grammarTasksList.setOnClickListener(onClickGrammarTasksList);

        Button examStart = findViewById(R.id.buttonExam);
        if (!isAllComplete) {
            examStart.setBackground(AppCompatResources.getDrawable(this, R.drawable.round_button_disabled));
        }
        View.OnClickListener onClickExamStart = v -> {
            if (isAllComplete) {
                getRandomTasks();
                Intent intent = new Intent(mainContext, TheoryActivity.class);
                intent.putExtra("tasks", (Serializable) randomTasks);
                intent.putExtra("theme", "Экзамен");
                mainActivityResult.launch(intent);
            }
            else {
                Toast.makeText(this, "Пройдите оба мини-экзамена в разделах \"Грамматика\" и \"Слова\".", Toast.LENGTH_LONG).show();
            }
        };
        examStart.setOnClickListener(onClickExamStart);

        Button transcriptionGame = findViewById(R.id.buttonGame);
        View.OnClickListener onClickGameStart = v -> {
            databaseAccess.open();
            Intent intent = new Intent(mainContext, TheoryActivity.class);
            intent.putExtra("theme", "Транскрипция");
            databaseAccess.close();
            mainActivityResult.launch(intent);
        };
        transcriptionGame.setOnClickListener(onClickGameStart);
    }

    private void getRandomTasks(){
        databaseAccess.open();
        ArrayList<Object> allTasks = databaseAccess.getTasksAsObject(true, true);
        databaseAccess.close();

        Collections.shuffle(allTasks);
        randomTasks = new ArrayList<>();
        randomTasks.addAll(allTasks.subList(0, 26));
    }

    private void initLoadDialog(Context context) {
        int llPadding = 30;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(llPadding, llPadding, llPadding, llPadding);
        layout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        layout.setLayoutParams(params);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        TextView text = new TextView(context);
        text.setText("Загрузка...");
        text.setTextColor(Color.parseColor("#000000"));
        text.setTextSize(20);
        text.setLayoutParams(params);

        layout.addView(progressBar);
        layout.addView(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(layout);
        loadDialog = builder.create();
        loadDialog.setTitle("Обновление данных");
        loadDialog.show();

        parseWordTasks();
        parseGrammarTasks();
        parseGameWords();
        initStats();
    }

    private void initErrorDialog(Exception ex) {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Обновление данных");
        errorDialog.setMessage("Возникла ошибка при обновлении данных");
        errorDialog.show();
        Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, ex.getMessage());
    }

    private void parseGrammarTasks() {
        try {
            InputStream stream = getAssets().open("grammar.xml");
            XmlFilesParser parser = new XmlFilesParser();
            List<GrammarTest> grammarTasks = parser.parseGrammarTestsFromXml(stream);
            if (CollectionUtils.isNotEmpty(grammarTasks)) {
                databaseAccess.open();
                databaseAccess.addGrammarTests(grammarTasks);
                databaseAccess.close();
            } else {
                Log.w(LOG, "Collection of grammar tasks are empty");
            }
        } catch (Exception ex) {
            loadDialog.cancel();
            loadDialog.dismiss();
            initErrorDialog(ex);
        }

        loadDialog.cancel();
        loadDialog.dismiss();
    }


    private void parseWordTasks() {
        try {
            InputStream stream = getAssets().open("words.xml");
            XmlFilesParser parser = new XmlFilesParser();
            List<WordTest> wordTests = parser.parseWordTestsFromXml(stream);
            if (CollectionUtils.isNotEmpty(wordTests)) {
                databaseAccess.open();
                databaseAccess.addWordTests(wordTests);
                databaseAccess.close();
            } else {
                Log.w(LOG, "Collection of word tasks are empty");
            }
        } catch (Exception ex) {
            loadDialog.cancel();
            loadDialog.dismiss();
            initErrorDialog(ex);
        }
    }

    private void parseGameWords() {
        try {
            InputStream stream = getAssets().open("game_words.xml");
            XmlFilesParser parser = new XmlFilesParser();
            List<Word> words = parser.parseGameWordsFromXml(stream);
            if (CollectionUtils.isNotEmpty(words)) {
                databaseAccess.open();
                databaseAccess.addGameWords(words);
                databaseAccess.close();
            } else {
                Log.w(LOG, "Collection of game words is empty");
            }
        } catch (Exception ex) {
            loadDialog.cancel();
            loadDialog.dismiss();
            initErrorDialog(ex);
        }
    }

    private void initStats(){
        databaseAccess.open();
        databaseAccess.initStats();
        databaseAccess.close();
    }

    private final ActivityResultLauncher<Intent> mainActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> loadMenu()
    );
}