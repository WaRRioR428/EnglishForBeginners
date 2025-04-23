package com.example.englishforbeginners;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatActivity extends AppCompatActivity {

    private EditText userInput;

    private ArrayList<MessageModel> messageModelArrayList;
    private MessageRVAdapter messageRVAdapter;
    private RecyclerView chatRV;

    private String conversation;
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

        chatRV = findViewById(R.id.chatView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this, RecyclerView.VERTICAL, false);
        chatRV.setLayoutManager(linearLayoutManager);
        chatRV.setAdapter(messageRVAdapter);

        ImageButton sendMsgIB = findViewById(R.id.buttonSend);
        sendMsgIB.setOnClickListener(v -> {
            String message = userInput.getText().toString();
            if (message.trim().isEmpty()) {
                Toast.makeText(ChatActivity.this, "Введите ваше сообщение...", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(message);
            userInput.setText("");
        });

        userInput = findViewById(R.id.userInput);
        userInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(100)});

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

        sendMessage("");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendMessage(String userMsg) {
        String url = "https://www.botlibre.com/rest/json/chat";
        messagesSent++;

        if (!userMsg.isEmpty()) {
            displayMessage(userMsg, "user");
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("application", "4232796170982947466");
            postData.put("instance", "56537269");
            postData.put("message", userMsg);
            if (!(conversation == null)) {
                postData.put("conversation", conversation);
            }

        } catch (JSONException ex) {
            Logger.getLogger(ChatActivity.class.getName()).log(Level.SEVERE, ex.getMessage());
        }

        RequestQueue queue = Volley.newRequestQueue(ChatActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, response -> {
            try {
                if (conversation == null) {
                    conversation = response.getString("conversation");
                }
                displayMessage(response.getString("message"), "bot");
            }
            catch (JSONException e) {
                Logger.getLogger(ChatActivity.class.getName()).log(Level.SEVERE, e.getMessage());
                displayMessage("No response", "bot");
            }
        }, error -> {
            displayMessage("Sorry no response found", "bot");
            Toast.makeText(ChatActivity.this, "No response from the bot..", Toast.LENGTH_SHORT).show();
        });
        queue.add(jsonObjectRequest);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayMessage(String message, String sender) {
        messageModelArrayList.add(new MessageModel(message, sender));
        messageRVAdapter.notifyDataSetChanged();
        chatRV.post(() -> chatRV.scrollToPosition(messageRVAdapter.getItemCount() - 1));
    }
}