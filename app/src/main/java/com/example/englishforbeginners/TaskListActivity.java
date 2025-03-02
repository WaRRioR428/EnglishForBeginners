package com.example.englishforbeginners;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.englishforbeginners.database.DatabaseAccess;
import com.example.englishforbeginners.entity.ConstructSentenceTask;
import com.example.englishforbeginners.entity.GrammarTest;
import com.example.englishforbeginners.entity.MissingWordSentenceTask;
import com.example.englishforbeginners.entity.Word;
import com.example.englishforbeginners.entity.WordTest;

public class TaskListActivity extends AppCompatActivity {
    private DatabaseAccess databaseAccess;
    private TaskListActivity listContext;
    private Boolean wordsFlag;
    private List<Object> randomTasks;
    private boolean isBlockComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        this.databaseAccess = DatabaseAccess.getInstance(this);
        listContext = this;

        Bundle extras = this.getIntent().getExtras();
        try {
            assert extras != null;
            wordsFlag = extras.getBoolean("wordsFlag");
            if (wordsFlag) {
                this.setTitle("Изучение слов");
                loadWordTaskList();
            } else {
                this.setTitle("Изучение грамматики");
                loadGrammarTaskList();
            }
        }
        catch (Exception ex) {
            Logger.getLogger(TaskListActivity.class.getName()).log(Level.SEVERE, ex.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void loadWordTaskList() {
        databaseAccess.open();
        List<WordTest> wordTests = databaseAccess.getAllWordTests();
        databaseAccess.close();

        LinearLayout parent = findViewById(R.id.listLayout);
        if (parent.getChildCount() > 0) {
            parent.removeAllViews();
        }

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < wordTests.size(); i++) {
            if (!wordTests.get(i).getTheme().equals("Мини-экзамен")) {
                if (i % 2 == 0) {
                    parent.addView(linearLayout);
                    linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                }
                Button wordTask = new Button(this);
                wordTask.setText(wordTests.get(i).getTheme());
                wordTask.setMinHeight(250);
                wordTask.setMinWidth(250);
                wordTask.setMaxHeight(250);
                wordTask.setMaxWidth(250);
                if (wordTests.get(i).getStatus() != 0) {
                    wordTask.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_green));
                } else {
                    wordTask.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_word_list));
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 5.0f;
                params.setMarginStart(30);
                params.setMarginEnd(30);
                params.setMargins(30, 20, 30, 20);
                wordTask.setLayoutParams(params);
                wordTask.setOnClickListener(getOnClickWordButtonListener(wordTests.get(i)));

                linearLayout.addView(wordTask);
            }
        }

        parent.addView(linearLayout);
        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button miniExam = new Button(this);
        miniExam.setText("Мини-экзамен");
        miniExam.setMinHeight(250);
        miniExam.setMinWidth(250);
        miniExam.setMaxHeight(250);
        miniExam.setMaxWidth(250);

        databaseAccess.open();
        isBlockComplete = databaseAccess.isBlockComplete(false);
        if (!isBlockComplete) {
            miniExam.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_disabled));
        }
        else if (databaseAccess.getAllWordTests().get(wordTests.size() - 1).getStatus() != 0) {
            miniExam.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_green));
        }
        else {
            miniExam.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_word_list));
        }
        databaseAccess.close();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 5.0f;
        params.setMarginStart(30);
        params.setMarginEnd(30);
        params.setMargins(30, 20, 30, 20);
        miniExam.setLayoutParams(params);

        View.OnClickListener onClickMiniExamButton = v -> {
            if (isBlockComplete) {
                getRandomTasks();
                Intent intent = new Intent(listContext, TheoryActivity.class);
                intent.putExtra("tasks", (Serializable) randomTasks);
                intent.putExtra("theme", "Мини-экзамен");
                activityResult.launch(intent);
            }
            else {
                Toast.makeText(this, "Пройдите все темы данного блока.", Toast.LENGTH_LONG).show();
            }
        };
        miniExam.setOnClickListener(onClickMiniExamButton);

        linearLayout.addView(miniExam);
        parent.addView(linearLayout);
    }

    public void loadGrammarTaskList() {
        databaseAccess.open();
        List<GrammarTest> grammarTests = databaseAccess.getAllGrammarTests();
        databaseAccess.close();

        LinearLayout parent = findViewById(R.id.listLayout);
        if (parent.getChildCount() > 0) {
            parent.removeAllViews();
        }

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < grammarTests.size(); i++) {
            if (!grammarTests.get(i).getTheme().equals("Мини-экзамен")) {
                if (i % 2 == 0) {
                    parent.addView(linearLayout);
                    linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                }
                Button grammarTask = new Button(this);
                grammarTask.setText(grammarTests.get(i).getTheme());
                grammarTask.setMinHeight(250);
                grammarTask.setMinWidth(250);
                grammarTask.setMaxHeight(250);
                grammarTask.setMaxWidth(250);
                if (grammarTests.get(i).getStatus() != 0) {
                    grammarTask.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_green));
                } else {
                    grammarTask.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_word_list));
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 5.0f;
                params.setMarginStart(30);
                params.setMarginEnd(30);
                params.setMargins(30, 20, 30, 20);
                grammarTask.setLayoutParams(params);
                grammarTask.setOnClickListener(getOnClickGrammarButtonListener(grammarTests.get(i)));

                linearLayout.addView(grammarTask);
            }
        }

        parent.addView(linearLayout);
        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button miniExam = new Button(this);
        miniExam.setText("Мини-экзамен");
        miniExam.setMinHeight(250);
        miniExam.setMinWidth(250);
        miniExam.setMaxHeight(250);
        miniExam.setMaxWidth(250);

        databaseAccess.open();
        isBlockComplete = databaseAccess.isBlockComplete(true);
        if (!isBlockComplete) {
            miniExam.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_disabled));
        }
        else if (databaseAccess.getAllGrammarTests().get(grammarTests.size() - 1).getStatus() != 0) {
            miniExam.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_green));
        } else {
            miniExam.setBackground(ContextCompat.getDrawable(this, R.drawable.round_button_word_list));
        }
        databaseAccess.close();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 5.0f;
        params.setMarginStart(30);
        params.setMarginEnd(30);
        params.setMargins(30, 20, 30, 20);
        miniExam.setLayoutParams(params);

        View.OnClickListener onClickMiniExamButton = v -> {
            if (isBlockComplete) {
                getRandomTasks();
                Intent intent = new Intent(listContext, TheoryActivity.class);
                intent.putExtra("tasks", (Serializable) randomTasks);
                intent.putExtra("theme", "Мини-экзамен");
                activityResult.launch(intent);
            }
            else {
                Toast.makeText(this, "Пройдите все темы данного блока.", Toast.LENGTH_LONG).show();
            }
        };
        miniExam.setOnClickListener(onClickMiniExamButton);

        linearLayout.addView(miniExam);

        parent.addView(linearLayout);
    }

    private View.OnClickListener getOnClickWordButtonListener(WordTest wordTest) {
        return v -> {
            convertWordTasksToObject(wordTest);
            Intent intent = new Intent(listContext, TheoryActivity.class);
            intent.putExtra("tasks", (Serializable) randomTasks);
            intent.putExtra("testID", wordTest.getId());
            intent.putExtra("theme", wordTest.getTheme());
            activityResult.launch(intent);
        };
    }

    private View.OnClickListener getOnClickGrammarButtonListener(GrammarTest grammarTest) {
        return v -> {
            convertGrammarTasksToObject(grammarTest);
            Intent intent = new Intent(listContext, TheoryActivity.class);
            intent.putExtra("tasks", (Serializable) randomTasks);
            intent.putExtra("testID", grammarTest.getId());
            intent.putExtra("theme", grammarTest.getTheme());
            activityResult.launch(intent);
        };
    }

    private void getRandomTasks(){
        databaseAccess.open();
        ArrayList<Object> allTasks = databaseAccess.getTasksAsObject(!wordsFlag, wordsFlag);
        databaseAccess.close();

        Collections.shuffle(allTasks);
        randomTasks = new ArrayList<>();
        randomTasks.addAll(allTasks.subList(0, 26));
    }

    private void convertGrammarTasksToObject(GrammarTest gt){
        randomTasks = new ArrayList<>();
        List<ConstructSentenceTask> cst = gt.getConstructSentenceTasks();
        List<MissingWordSentenceTask> mwt = gt.getMissingWordTasks();

        randomTasks.addAll(cst);
        randomTasks.addAll(mwt);
        Collections.shuffle(randomTasks);
    }

    private void convertWordTasksToObject(WordTest wt) {
        randomTasks = new ArrayList<>();
        List<Word> words = wt.getWords();

        randomTasks.addAll(words);
        Collections.shuffle(randomTasks);
    }

    private final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                assert data != null;
                wordsFlag = Objects.requireNonNull(data.getExtras()).getBoolean("wordsFlag");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (wordsFlag) {
                        this.setTitle("Изучение слов");
                        loadWordTaskList();
                    }
                    else {
                        this.setTitle("Изучение грамматики");
                        loadGrammarTaskList();
                    }
                }
            }
    );
}