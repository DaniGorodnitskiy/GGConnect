package com.example.ggconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String chatPartnerName;
    private String chatPartnerId;
    private String currentUserId;
    private String chatRoomId;

    private TextView tvChatHeader;
    private EditText etMessage;
    private ImageButton btnSend;
    private RecyclerView recyclerChat;

    // משתנים חדשים לתצוגה
    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatPartnerName = getIntent().getStringExtra("userName");
        chatPartnerId = getIntent().getStringExtra("userId");
        chatRoomId = getChatRoomId(currentUserId, chatPartnerId);

        tvChatHeader = findViewById(R.id.tvChatHeader);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerChat = findViewById(R.id.recyclerChat);

        if (chatPartnerName != null) {
            tvChatHeader.setText(chatPartnerName);
        }

        // --- 1. הגדרת הרשימה (Adapter) ---
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        // --- 2. האזנה להודעות בזמן אמת ---
        listenForMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    // הפונקציה שמאזינה לשינויים ב-Firebase
    private void listenForMessages() {
        db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING) // הודעות לפי סדר כרונולוגי
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        // בדיקה אילו שינויים קרו (הודעה חדשה נוספה?)
                        for (DocumentChange change : value.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                Message message = change.getDocument().toObject(Message.class);
                                messageList.add(message);
                            }
                        }
                        // עדכון המסך וגלילה למטה להודעה האחרונה
                        chatAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            recyclerChat.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        Message message = new Message(currentUserId, chatPartnerId, messageText);

        db.collection("chats").document(chatRoomId).collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> etMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "שגיאה", Toast.LENGTH_SHORT).show());
    }

    private String getChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) return userId1 + "_" + userId2;
        else return userId2 + "_" + userId1;
    }
}