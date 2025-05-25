package com.example.englishforbeginners;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.englishforbeginners.entity.ConstructSentenceTask;
import com.example.englishforbeginners.entity.MissingWordSentenceTask;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TheoryActivity extends AppCompatActivity {
    private TheoryActivity context;
    private List<Object> randomTasks;

    private boolean hasWordTest;
    private int testID;
    private String testTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theory);

        context = this;

        Bundle extras = this.getIntent().getExtras();
        assert extras != null;
        testTheme = extras.getString("theme");
        randomTasks = (List<Object>) extras.getSerializable("tasks");
        if (!(testTheme.equals("Транскрипция") || testTheme.equals("Экзамен") || testTheme.equals("Мини-экзамен"))) {
            testID = extras.getInt("testID");
        }

        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setText((testTheme.equals("Экзамен") || testTheme.equals("Транскрипция")) ? "Вернуться в меню" : "Назад к списку");
        View.OnClickListener onClickButtonBack = v -> finishActivity();
        buttonBack.setOnClickListener(onClickButtonBack);

        checkIfWordExam();
        loadActivity(testTheme);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishActivity();
            }
        });
    }

    private void checkIfWordExam(){
        if (testTheme.equals("Транскрипция")) {
            hasWordTest = false;
            return;
        }

        for (Object task: randomTasks) {
            if (task instanceof ConstructSentenceTask || task instanceof MissingWordSentenceTask) {
                hasWordTest = false;
                return;
            }
        }
        hasWordTest = true;
    }

    private void loadActivity(String theme) {
        context.setTitle(theme);
        switch (testTheme) {
            case "Экзамен" -> theme = "exam_theory";
            case "Мини-экзамен" -> theme = "mini_exam_theory";
            case "Транскрипция" -> theme = "game_theory";
        }

        ImageView theoryImage = findViewById(R.id.theoryImage);
        String imageName = theme.replaceAll("[^A-Za-zА-Яа-я0-9]", " ").trim().replaceAll("\\s+", "_").toLowerCase();
        int resID = getDrawableByString(imageName);

        if (resID != -1) {
            theoryImage.setImageResource(resID);
        }
        else {
            theoryImage.setVisibility(View.GONE);
        }

        View.OnClickListener onClickImage = v -> {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = (displayMetrics.heightPixels) * 2 / 3;

            AlertDialog.Builder alertTheory = new AlertDialog.Builder(context);
            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.theory_alert_dialog, null);

            ImageView alertImage = view.findViewById(R.id.theoryDialogImage);
            alertImage.setImageResource(resID);
            alertImage.getLayoutParams().height = height;
            alertImage.setScaleType(ImageView.ScaleType.FIT_XY);
            alertTheory.setView(view);

            AlertDialog dialog = alertTheory.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(view, 0, 0, 0, 0);
            dialog.show();
        };
        theoryImage.setOnClickListener(onClickImage);

        Button buttonStart = findViewById(R.id.buttonStart);
        View.OnClickListener onClickButtonStart = v -> {
            Intent intent;
            if (testTheme.equals("Транскрипция")) {
                intent = new Intent(context, TranscriptionGameActivity.class);
            }
            else {
                intent = new Intent(context, TestActivity.class);
                intent.putExtra("tasks", (Serializable) randomTasks);
                intent.putExtra("theme", testTheme);
                intent.putExtra("theoryImageName", imageName);
                if (!(testTheme.equals("Экзамен") || testTheme.equals("Мини-экзамен"))) {
                    intent.putExtra("testID", testID);
                }
            }
            testActivityResult.launch(intent);
        };
        buttonStart.setOnClickListener(onClickButtonStart);

        if (theme.equals("game_theory")) {
            buttonStart.setText("Начать игру");
        }
        else if (theme.contains("exam")) {
            buttonStart.setText("Начать экзамен");
        }
    }

    private static int getDrawableByString(String name) {
        try {
            Field idField = R.drawable.class.getDeclaredField(name);
            return idField.getInt(idField);
        }
        catch (Exception e) {
            Logger.getLogger(TheoryActivity.class.getName()).log(Level.SEVERE, e.getMessage());
            return -1;
        }
    }

    private final ActivityResultLauncher<Intent> testActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    finishActivity();
                }
            }
    );

    private void finishActivity(){
        Intent intent = new Intent();
        intent.putExtra("wordsFlag", hasWordTest);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finishActivity();
        return true;
    }
}