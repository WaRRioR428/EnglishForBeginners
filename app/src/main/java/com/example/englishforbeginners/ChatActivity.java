package com.example.englishforbeginners;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.englishforbeginners.chat.MessageModel;
import com.example.englishforbeginners.chat.MessageRVAdapter;
import com.example.englishforbeginners.database.DatabaseAccess;
import com.example.englishforbeginners.entity.Stats;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatActivity extends AppCompatActivity {

    private EditText userMsgEdt;
    private final String BOT_KEY = "bot";

    private ArrayList<MessageModel> messageModelArrayList;
    private MessageRVAdapter messageRVAdapter;

    private int messagesSent;
    private ChatActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagesSent = 0;
        context = this;
        this.setTitle("Чат-бот");

        messageModelArrayList = new ArrayList<>();
        messageRVAdapter = new MessageRVAdapter(messageModelArrayList);

        RecyclerView chatsRV = findViewById(R.id.idRVChats);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this, RecyclerView.VERTICAL, false);
        chatsRV.setLayoutManager(linearLayoutManager);
        chatsRV.setAdapter(messageRVAdapter);

        ImageButton sendMsgIB = findViewById(R.id.idIBSend);
        sendMsgIB.setOnClickListener(v -> {
            String message = userMsgEdt.getText().toString();
            if (message.isEmpty()) {
                Toast.makeText(ChatActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(message);
            userMsgEdt.setText("");
        });

        userMsgEdt = findViewById(R.id.idEdtMessage);

        RequestQueue mRequestQueue = Volley.newRequestQueue(ChatActivity.this);
        mRequestQueue.getCache().clear();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
                databaseAccess.open();
                Stats stats = databaseAccess.getRequiredStats("chat");
                stats.messagesSent = stats.messagesSent + messagesSent;
                databaseAccess.updateRequiredStats(stats, "chat");
                databaseAccess.close();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendMessage(String userMsg) {
        String url = "http://api.brainshop.ai/get?bid=183519&key=6d3Tf3219fyPLUHd&uid=uid&msg=" + userMsg;
        messagesSent++;

        messageModelArrayList.add(new MessageModel(userMsg, "user"));
        messageRVAdapter.notifyDataSetChanged();

        RequestQueue queue = Volley.newRequestQueue(ChatActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String botResponse = response.getString("cnt");
                messageModelArrayList.add(new MessageModel(botResponse, BOT_KEY));
                messageRVAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Logger.getLogger(ChatActivity.class.getName()).log(Level.SEVERE, e.getMessage());
                messageModelArrayList.add(new MessageModel("No response", BOT_KEY));
                messageRVAdapter.notifyDataSetChanged();
            }
        }, error -> {
            messageModelArrayList.add(new MessageModel("Sorry no response found", BOT_KEY));
            messageRVAdapter.notifyDataSetChanged();
            Toast.makeText(ChatActivity.this, "No response from the bot..", Toast.LENGTH_SHORT).show();
        });
        queue.add(jsonObjectRequest);
    }
}