package com.example.englishforbeginners;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.englishforbeginners.database.DatabaseAccess;
import com.example.englishforbeginners.entity.Stats;
import com.example.englishforbeginners.entity.Word;

import java.util.Arrays;
import java.util.Locale;

public class TranscriptionGameActivity extends AppCompatActivity {

    private TranscriptionGameActivity context;
    private Word word;
    private TextView triesCount;
    private TextView question;
    private TextView transcription;
    private LinearLayout triesLayout;
    private EditText input;
    private Button buttonApply;
    private ImageButton listenButton;

    private TextToSpeech TTS;
    private boolean ttsEnabled;
    private InputFilter filter;
    private String inputWord;
    private String dlPath;
    private int tries;
    private boolean taskEnd;

    MediaPlayer mpWrong;
    MediaPlayer mpSuccess;
    MediaPlayer mpFail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcription_game);

        context = this;
        this.setTitle("Игра");

        mpWrong = MediaPlayer.create(this, R.raw.wrong);
        mpSuccess = MediaPlayer.create(this, R.raw.success);
        mpFail = MediaPlayer.create(this, R.raw.fail);

        triesCount = findViewById(R.id.triesCount);

        triesLayout = findViewById(R.id.scrollLayout);

        question = findViewById(R.id.question);
        question.setText("Отгадайте слово по транскрипции:");

        transcription = findViewById(R.id.transcription);

        buttonApply = findViewById(R.id.buttonApply);

        listenButton = findViewById(R.id.imageListen);
        View.OnClickListener onImageClick = v -> speak();
        listenButton.setOnClickListener(onImageClick);

        input = findViewById(R.id.textField);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!taskEnd) {
                    buttonApply.setEnabled(!s.toString().trim().isEmpty());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        filter = (source, start, end, dEst, dStart, dEnd) -> {
            for (int i = start; i < end; i++) {
                char ch = source.charAt(i);
                if (!Character.isLetter(ch)) {
                    String newSource = source.toString();
                    newSource = newSource.substring(0, i) + newSource.substring(i + 1, end);
                    return newSource;
                }
            }
            return null;
        };

        TTS = new TextToSpeech(this, initStatus -> {
            if (initStatus == TextToSpeech.SUCCESS) {
                if (TTS.isLanguageAvailable(new Locale(Locale.getDefault().getLanguage()))
                        == TextToSpeech.LANG_AVAILABLE) {
                    TTS.setLanguage(Locale.ENGLISH);
                }
                TTS.setPitch(1.3f);
                TTS.setSpeechRate(0.7f);
                ttsEnabled = true;
            }
            else if (initStatus == TextToSpeech.ERROR) {
                Toast.makeText(context, R.string.tts_error, Toast.LENGTH_LONG).show();
                ttsEnabled = false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mpFail.release();
                mpSuccess.release();
                mpWrong.release();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        prepareGame();
        createTestFlow();
    }

    private void speak() {
        if (!ttsEnabled) return;
        String utteranceId = String.valueOf(this.hashCode());
        TTS.speak(word.word(), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private void getRandomWordFromDB(){
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        word = databaseAccess.getRandomGameWord();
        databaseAccess.close();
    }

    private void prepareGame(){
        taskEnd = false;
        tries = 1;

        View.OnClickListener onClickButtonApply = v -> {
            hideKeyboard(context);
            inputWord = input.getText().toString().toLowerCase();
            if (inputWord.equals(word.word().toLowerCase())) {
                dlPath = "";
                for (int i = 0; i < inputWord.length(); i++) {
                    dlPath += "G;";
                }
                dlPath = dlPath.substring(0, dlPath.length() - 1);
                triesLayout.addView(createTextView(), 0);
                showEndScreen(true);
            }
            else {
                calculateDL();
                triesLayout.addView(createTextView(), 0);
                tries++;
                if (tries < 6) mpWrong.start();
                createTestFlow();
            }
        };
        buttonApply.setOnClickListener(onClickButtonApply);
        buttonApply.setText("Проверить");

        triesCount.setVisibility(View.VISIBLE);
        question.setVisibility(View.VISIBLE);
        listenButton.setVisibility(View.VISIBLE);
        input.setVisibility(View.VISIBLE);

        triesLayout.removeAllViews();

        getRandomWordFromDB();
        transcription.setText(word.transcription());
        transcription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        input.setFilters(new InputFilter[] {filter, new InputFilter.LengthFilter(10)});
    }

    @SuppressLint("SetTextI18n")
    private void createTestFlow(){
        if (tries < 6) {
            triesCount.setText("Попытка " + tries + "/5");
            input.setText("");
        }
        else {
            showEndScreen(false);
        }
    }

    @SuppressLint("SetTextI18n")
    private void showEndScreen(boolean success) {
        taskEnd = true;
        triesCount.setVisibility(View.GONE);
        question.setVisibility(View.GONE);
        listenButton.setVisibility(View.GONE);
        input.setVisibility(View.GONE);
        transcription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        transcription.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        buttonApply.setEnabled(false);
        buttonApply.setText("Сыграть снова");

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        Stats stats = databaseAccess.getRequiredStats("game");
        stats.totalGames++;
        stats.totalTries = stats.totalTries + ((tries == 6) ? (tries - 1) : tries);
        if (success) {
            mpSuccess.start();
            transcription.setText("Поздравляем!\nВы справились!");
            stats.successfulGames++;
        }
        else {
            mpFail.start();
            transcription.setText("Вы проиграли.\nЗагаданное слово:\n" + word.word());
        }
        databaseAccess.updateRequiredStats(stats, "game");
        databaseAccess.close();

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            buttonApply.setEnabled(true);
            View.OnClickListener onClickButtonApply = v -> {
                prepareGame();
                createTestFlow();
            };
            buttonApply.setOnClickListener(onClickButtonApply);
        }, 4000);
    }

    private TextView createTextView(){
        TextView textView = new TextView(context);
        char[] textChars = inputWord.toCharArray();
        SpannableStringBuilder ssb = new SpannableStringBuilder("");
        String[] path = dlPath.split(";");

        int charIndex = textChars.length - 1;
        for (String direction : path) {
            switch (direction) {
                case "G" -> {
                    ssb.insert(0, (textChars[charIndex] + " ").toUpperCase());
                    ssb.setSpan(new BackgroundColorSpan(Color.GREEN), 0, 1, 0);
                    ssb.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 1, 2, 0);
                    charIndex--;
                }
                case "DTL" -> {
                    ssb.insert(0, (String.valueOf(textChars[charIndex - 1]) + textChars[charIndex] + " ").toUpperCase());
                    ssb.setSpan(new BackgroundColorSpan(Color.CYAN), 0, 2, 0);
                    ssb.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 2, 3, 0);
                    charIndex -= 2;
                }
                case "TL" -> {
                    ssb.insert(0, (textChars[charIndex] + " ").toUpperCase());
                    ssb.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, 1, 0);
                    ssb.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 1, 2, 0);
                    charIndex--;
                }
                case "L" -> {
                    ssb.insert(0, (textChars[charIndex] + " ").toUpperCase());
                    ssb.setSpan(new BackgroundColorSpan(Color.RED), 0, 1, 0);
                    ssb.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 1, 2, 0);
                    charIndex--;
                }
                default -> {
                    ssb.insert(0, "? ");
                    ssb.setSpan(new BackgroundColorSpan(Color.WHITE), 0, 1, 0);
                    ssb.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 1, 2, 0);
                }
            }
        }

        textView.setText(ssb);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 25, 0, 25);
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    private void calculateDL() {
        char[] originalWord = word.word().toLowerCase().toCharArray();
        char[] userWord = inputWord.toCharArray();
        dlPath = "";

        int[][] dlTable = new int[originalWord.length + 1][userWord.length + 1];
        dlTable[0][0] = 0;

        for (int i = 1; i < originalWord.length + 1; i++) {
            dlTable[i][0] = i;
        }

        for (int j = 1; j < userWord.length + 1; j++) {
            dlTable[0][j] = j;
        }

        for (int i = 1; i < originalWord.length + 1; i++) {
            for (int j = 1; j < userWord.length + 1; j++) {
                int[] minArr;
                if (i > 1 && j > 1 && originalWord[i-1] == userWord[j-2] && originalWord[i-2] == userWord[j-1]) {
                    minArr = new int[]{dlTable[i - 1][j] + 1, dlTable[i][j - 1] + 1, dlTable[i - 1][j - 1] + areEqualChars(originalWord[i-1], userWord[j-1]), dlTable[i - 2][j - 2] + 1};
                }
                else {
                    minArr = new int[]{dlTable[i - 1][j] + 1, dlTable[i][j - 1] + 1, dlTable[i - 1][j - 1] + areEqualChars(originalWord[i-1], userWord[j-1])};
                }
                Arrays.sort(minArr);
                dlTable[i][j] = minArr[0];
            }
        }

        int i = originalWord.length;
        int j = userWord.length;
        while (i != 0 || j != 0) {
            if (i == 0) {
                while (j != 0) {
                    dlPath += "L;";
                    j--;
                }
                break;
            }

            if (j == 0) {
                while (i != 0) {
                    dlPath += "T;";
                    i--;
                }
                break;
            }

            int[] minArr;
            if (i > 1 && j > 1 && originalWord[i-1] == userWord[j-2] && originalWord[i-2] == userWord[j-1]) {
                minArr = new int[] {dlTable[i - 1][j], dlTable[i][j - 1], dlTable[i - 1][j - 1], dlTable[i - 2][j - 2]};
                Arrays.sort(minArr);
                if (minArr[0] == dlTable[i - 2][j - 2]) {
                    if (originalWord[i-1] == originalWord[i-2]) {
                        dlPath += "G;G;";
                    }
                    else {
                        dlPath += "DTL;";
                    }
                    i = i - 2;
                    j = j - 2;
                    continue;
                }
            }
            else {
                minArr = new int[]{dlTable[i - 1][j], dlTable[i][j - 1], dlTable[i - 1][j - 1]};
                Arrays.sort(minArr);
            }

            if (minArr[0] == dlTable[i - 1][j - 1]) {
                if (originalWord[i-1] == userWord[j-1]) {
                    dlPath += "G;";
                }
                else {
                    dlPath += "TL;";
                }
                i--;
                j--;
            }
            else if (minArr[0] == dlTable[i][j - 1]) {
                dlPath += "L;";
                j--;
            }
            else {
                dlPath += "T;";
                i--;
            }
        }

        dlPath = dlPath.substring(0, dlPath.length() - 1);
    }

    private static int areEqualChars(char first, char second){
        return (first == second) ? 0 : 1;
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}