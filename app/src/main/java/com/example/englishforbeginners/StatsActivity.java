package com.example.englishforbeginners;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.englishforbeginners.database.DatabaseAccess;
import com.example.englishforbeginners.entity.Stats;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

   private Map<String, String> texts;
   private StatsActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        context = this;
        this.setTitle("Статистика");

        getStats("grammar");
        createBlock("Грамматика");

        getStats("word");
        createBlock("Слова");

        getStats("exam");
        createBlock("Экзамен");

        getStats("game");
        createBlock("Транскрипция");

        getStats("chat");
        createBlock("Чат-бот");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void getStats(String statsType){
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        Stats stats = databaseAccess.getRequiredStats(statsType);

        texts = new LinkedHashMap<>();
        switch (statsType) {
            case "grammar", "word":
                texts.put("Прогресс: ", databaseAccess.getProgressStats(statsType.equals("grammar")));

                texts.put(" \nВсего прохождений тестов: ", String.valueOf(stats.totalTests));
                texts.put("Успешных прохождений: ", String.valueOf(stats.successfulTests));
                texts.put("Провалено тестов: ", String.valueOf(stats.totalTests - stats.successfulTests));
                texts.put("Успешность: ", String.format(Locale.ENGLISH, "%.2f", stats.getSuccessfulTestsPercentage()) + "%\n ");

                texts.put("Всего дано ответов: ", String.valueOf(stats.totalAnswers));
                texts.put("Правильных ответов: ", String.valueOf(stats.correctAnswers));
                texts.put("Ошибок: ", String.valueOf(stats.totalAnswers - stats.correctAnswers));
                texts.put("Успеваемость: ", String.format(Locale.ENGLISH, "%.2f", stats.getCorrectAnswersPercentage()) + "%\n ");

                if (stats.miniExamMark == 0) {
                    texts.put("Мини-экзамен: ", "Не пройден");
                    texts.put("Оценка за мини-экзамен: \"", "-\"");
                }
                else if (stats.miniExamMark < 3){
                    texts.put("Мини-экзамен: ", "Пройден неуспешно");
                    texts.put("Оценка за мини-экзамен: \"", stats.miniExamMark + "\"");
                }
                else {
                    texts.put("Мини-экзамен: ", "Пройден");
                    texts.put("Оценка за мини-экзамен: \"", stats.miniExamMark + "\"");
                }
                texts.put("Количество прохождений: ", String.valueOf(stats.miniExamCompletions));
                break;

            case "exam":
                if (stats.examMark == 0) {
                    texts.put("Экзамен: ", "Не пройден");
                    texts.put("Оценка за экзамен: \"", "-\"");
                }
                else if (stats.examMark < 3){
                    texts.put("Экзамен: ", "Пройден неуспешно");
                    texts.put("Оценка за экзамен: \"", stats.examMark + "\"");
                }
                else {
                    texts.put("Экзамен: ", "Пройден");
                    texts.put("Оценка за экзамен: ", stats.examMark + "\"");
                }
                texts.put("Количество прохождений: ", String.valueOf(stats.examCompletions));
                break;

            case "game":
                texts.put("Всего игр: ", String.valueOf(stats.totalGames));
                texts.put("Побед: ", String.valueOf(stats.successfulGames));
                texts.put("Поражений: ", String.valueOf(stats.totalGames - stats.successfulGames));
                texts.put("Процент побед: ", String.format(Locale.ENGLISH, "%.2f", stats.getSuccessfulGamesPercentage()) + "%\n ");

                texts.put("Общее число попыток: ", String.valueOf(stats.totalTries));
                texts.put("Среднее кол-во попыток на игру: ", String.valueOf(stats.getMeanTries()));
                break;

            default:
                texts.put("Отправлено сообщений: ", stats.messagesSent + "\n");
                break;
        }

        databaseAccess.close();
    }

    @SuppressLint("SetTextI18n")
    private void createBlock(String blockName) {
        LinearLayout parent = findViewById(R.id.statsLayout);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);

        TextView textView = new TextView(context);
        textView.setText(blockName);
        textView.setTextSize(35);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        textView.setPadding(0, 50, 0, 50);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

        linearLayout.addView(textView);
        parent.addView(linearLayout);

        for (String key : texts.keySet()) {
            textView = new TextView(context);
            if (key.contains("Экзамен") || key.contains("Мини-экзамен")) {
                String value = texts.get(key);
                assert value != null;
                SpannableString ss = new SpannableString(key + value);
                if (value.equals("Не пройден")) {
                    ss.setSpan(new ForegroundColorSpan(Color.RED), (ss.length() - value.length()), ss.length(), 0);
                }
                else if (value.equals("Пройден")) {
                    ss.setSpan(new ForegroundColorSpan(Color.GREEN), (ss.length() - value.length()), ss.length(), 0);
                }
                else {
                    ss.setSpan(new ForegroundColorSpan(Color.YELLOW), (ss.length() - value.length()), ss.length(), 0);
                }
                ss.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, (ss.length() - value.length()), 0);
                textView.setText(ss);
            }
            else {
                textView.setText(key + texts.get(key));
                textView.setTextColor(Color.DKGRAY);
            }
            textView.setTextSize(20);
            textView.setTypeface(null, Typeface.BOLD);

            parent.addView(textView);
        }
    }
}