package com.example.ggconnect;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
        // משיגים את ה-ID שלי כדי לדעת אילו הודעות הן שלי
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.tvContent.setText(message.getContent());

        // --- הלוגיקה של ימין/שמאל ---

        if (message.getSenderId().equals(currentUserId)) {
            // אם אני שלחתי: צד ימין + רקע כחול
            holder.container.setGravity(Gravity.RIGHT); // או END
            holder.tvContent.setBackgroundColor(Color.parseColor("#007BFF")); // כחול
            holder.tvContent.setTextColor(Color.WHITE);
        } else {
            // אם מישהו אחר שלח: צד שמאל + רקע אפור
            holder.container.setGravity(Gravity.LEFT); // או START
            holder.tvContent.setBackgroundColor(Color.parseColor("#E0E0E0")); // אפור
            holder.tvContent.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        LinearLayout container;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            container = itemView.findViewById(R.id.messageContainer);
        }
    }
}