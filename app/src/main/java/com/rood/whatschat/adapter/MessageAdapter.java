package com.rood.whatschat.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.rood.whatschat.R;
import com.rood.whatschat.model.Message;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessages;
    private FirebaseAuth mAuth;

    // Param. Constructor
    public MessageAdapter(List<Message> mMessages) {
        this.mMessages = mMessages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Message message = mMessages.get(position);

        String currentId = mAuth.getUid();

        // Chane Message View
        if (message.getFrom().equals(currentId)){
            holder.messageView.setBackgroundResource(R.drawable.message_text_bg_sender);
            holder.messageView.setTextColor(Color.BLACK);


        } else {
            holder.messageView.setBackgroundResource(R.drawable.message_text_bg);
            holder.messageView.setTextColor(Color.WHITE);
        }

        holder.setMessageView(message.getMessage());
        //holder.setTimeView(message.getTime());

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }



    //////// VIEW HOLDER CLASS
    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageView;
        //public TextView timeView;
        public CircleImageView profileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageView = itemView.findViewById(R.id.message_single_text);
            profileImage = itemView.findViewById(R.id.message_single_profile);
            //timeView = itemView.findViewById(R.id.message_single_time);

        }

        public void setMessageView(String s) {
            messageView.setText(s);
        }

        public void setTimeView(String s) {
            // timeView.setText(s)
        }
    }
}
