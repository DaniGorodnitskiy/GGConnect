package com.example.ggconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> friendsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        db = FirebaseFirestore.getInstance();
        friendsList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, friendsList);
        recyclerView.setAdapter(userAdapter);

        loadFriends();
    }

    private void loadFriends() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> friendIds = (List<String>) documentSnapshot.get("friends");
                if (friendIds != null && !friendIds.isEmpty()) {
                    db.collection("users").whereIn("uid", friendIds).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    User user = doc.toObject(User.class);
                                    friendsList.add(user);
                                }
                                userAdapter.notifyDataSetChanged();
                            });
                } else {
                    Toast.makeText(this, getString(R.string.no_friends_message), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}