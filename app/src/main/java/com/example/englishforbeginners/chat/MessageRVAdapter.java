package com.example.englishforbeginners.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishforbeginners.R;

import java.util.ArrayList;

public class MessageRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<MessageModel> messageModelArrayList;


    public MessageRVAdapter(ArrayList<MessageModel> messageModelArrayList) {
        this.messageModelArrayList = messageModelArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_msg, parent, false);
            return new UserViewHolder(view);
        }
        else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_msg, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel model = messageModelArrayList.get(position);
        switch (model.sender()) {
            case "user" ->
                    ((UserViewHolder) holder).userTV.setText(model.message());
            case "bot" ->
                    ((BotViewHolder) holder).botTV.setText(model.message());
        }
    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return switch (messageModelArrayList.get(position).sender()) {
            case "user" -> 0;
            case "bot" -> 1;
            default -> -1;
        };
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userTV;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userTV = itemView.findViewById(R.id.idTVUser);
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botTV;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botTV = itemView.findViewById(R.id.idTVBot);
        }
    }
}

