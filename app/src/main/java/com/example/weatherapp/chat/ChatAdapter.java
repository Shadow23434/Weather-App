package com.example.weatherapp.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.hourly.Hourly;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter {
    ArrayList<ChatModel> chatModelArrayList;
    private Context context;

    public ChatAdapter(ArrayList<ChatModel> chatModelArrayList, Context context) {
        this.chatModelArrayList = chatModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_mess, parent, false);
                return new UserViewHolder(view);

            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.assistant_mess, parent, false);
                return new AssistantViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatModel chatModel = chatModelArrayList.get(position);
        switch (chatModel.getSender()) {
            case "user":
                ((UserViewHolder)holder).user_mess.setText(chatModel.getMessage());
                break;

            case "assistant":
                ((AssistantViewHolder)holder).assistant_mess.setText(chatModel.getMessage());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (chatModelArrayList.get(position).getSender()) {
            case "user":
                return 0;
            case "assistant":
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public int getItemCount() {
        return chatModelArrayList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView user_mess;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            user_mess = itemView.findViewById(R.id.user_mess);
        }
    }

    public static class AssistantViewHolder extends RecyclerView.ViewHolder {
        TextView assistant_mess;
        public AssistantViewHolder(@NonNull View itemView) {
            super(itemView);
            assistant_mess = itemView.findViewById(R.id.assistant_mess);
        }
    }
}
