package com.example.ggconnect;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvName, tvAge, tvBio;
    private Button btnAction, btnAddFriend, btnRemoveFriend;
    private ImageButton btnEditName, btnEditAge, btnEditBio, btnEditGames;
    private ImageView ivProfileImage; 
    private LinearLayout profileGamesContainer;
    private RecyclerView rvUserFriends, rvCommonFriends;
    private UserAdapter friendsAdapter, commonFriendsAdapter;
    private List<User> userFriendsList = new ArrayList<>();
    private List<User> commonFriendsList = new ArrayList<>();

    private String profileUserId; 
    private String currentUserId; 
    private User viewedUser;      
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivProfileImage.setImageURI(uri);
                    ivProfileImage.setPadding(0, 0, 0, 0);
                    ivProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    uploadProfileImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();

        tvName = findViewById(R.id.tvProfileName);
        tvAge = findViewById(R.id.tvProfileAge);
        tvBio = findViewById(R.id.tvProfileBio);
        btnAction = findViewById(R.id.btnAction);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnRemoveFriend = findViewById(R.id.btnRemoveFriend);
        ivProfileImage = findViewById(R.id.ivProfileImageHeader); 
        profileGamesContainer = findViewById(R.id.profileGamesContainer);
        
        btnEditName = findViewById(R.id.btnEditName);
        btnEditAge = findViewById(R.id.btnEditAge);
        btnEditBio = findViewById(R.id.btnEditBio);
        btnEditGames = findViewById(R.id.btnEditGames);

        rvUserFriends = findViewById(R.id.rvUserFriends);
        rvCommonFriends = findViewById(R.id.rvCommonFriends);

        rvUserFriends.setLayoutManager(new LinearLayoutManager(this));
        friendsAdapter = new UserAdapter(this, userFriendsList);
        rvUserFriends.setAdapter(friendsAdapter);

        rvCommonFriends.setLayoutManager(new LinearLayoutManager(this));
        commonFriendsAdapter = new UserAdapter(this, commonFriendsList);
        rvCommonFriends.setAdapter(commonFriendsAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profileUserId = getIntent().getStringExtra("userId");

        if (profileUserId == null) {
            finish();
            return;
        }

        loadUserProfile();
    }

    private void loadUserProfile() {
        db.collection("users").document(profileUserId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        viewedUser = documentSnapshot.toObject(User.class);
                        if (viewedUser != null) {
                            if (viewedUser.getUid() == null) viewedUser.setUid(documentSnapshot.getId());
                            updateUI();
                            loadFriendsList();
                        }
                    }
                });
    }

    private void updateUI() {
        tvName.setText(viewedUser.getName() != null ? viewedUser.getName() : getString(R.string.no_name));
        String age = viewedUser.getAge();
        if (age != null && !age.trim().isEmpty()) {
            tvAge.setText(getString(R.string.age_prefix) + age);
            tvAge.setVisibility(View.VISIBLE);
        } else {
            tvAge.setText(getString(R.string.add_age));
        }

        tvBio.setText(viewedUser.getBio() != null && !viewedUser.getBio().isEmpty() ? viewedUser.getBio() : getString(R.string.no_bio));

        if (viewedUser.getProfileImageUrl() != null && !viewedUser.getProfileImageUrl().isEmpty() && ivProfileImage != null) {
            try {
                byte[] bytes = Base64.decode(viewedUser.getProfileImageUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ivProfileImage.setImageBitmap(bitmap);
                ivProfileImage.setPadding(0, 0, 0, 0); 
                ivProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        profileGamesContainer.removeAllViews();
        if (viewedUser.getGames() != null && !viewedUser.getGames().isEmpty()) {
            for (String gameName : viewedUser.getGames()) {
                addGameIcon(profileGamesContainer, gameName);
            }
        }

        if (profileUserId.equals(currentUserId)) {
            btnAction.setVisibility(View.GONE);
            btnAddFriend.setVisibility(View.GONE);
            btnRemoveFriend.setVisibility(View.GONE);
            findViewById(R.id.cardCommonFriends).setVisibility(View.GONE);
            
            btnEditName.setVisibility(View.VISIBLE);
            btnEditAge.setVisibility(View.VISIBLE);
            btnEditBio.setVisibility(View.VISIBLE);
            btnEditGames.setVisibility(View.VISIBLE);

            ivProfileImage.setOnClickListener(v -> mGetContent.launch("image/*"));

            btnEditName.setOnClickListener(v -> showEditDialog(getString(R.string.field_name), "name", viewedUser.getName(), InputType.TYPE_CLASS_TEXT));
            btnEditAge.setOnClickListener(v -> showEditDialog(getString(R.string.field_age), "age", viewedUser.getAge(), InputType.TYPE_CLASS_NUMBER));
            btnEditBio.setOnClickListener(v -> showEditDialog(getString(R.string.field_bio), "bio", viewedUser.getBio(), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
            btnEditGames.setOnClickListener(v -> {
                Intent intent = new Intent(this, SetupProfileActivity.class);
                intent.putExtra("EDIT_GAMES_ONLY", true);
                startActivity(intent);
            });

        } else {
            btnAction.setVisibility(View.VISIBLE);
            btnEditName.setVisibility(View.GONE);
            btnEditAge.setVisibility(View.GONE);
            btnEditBio.setVisibility(View.GONE);
            btnEditGames.setVisibility(View.GONE);
            ivProfileImage.setOnClickListener(null); 
            checkFriendshipStatus();
            
            btnAction.setText(getString(R.string.send_message));
            btnAction.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("userId", viewedUser.getUid());
                intent.putExtra("userName", viewedUser.getName());
                startActivity(intent);
            });
        }
    }

    private void uploadProfileImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Resize for Firestore
            int width = 400;
            int height = (int) (bitmap.getHeight() * (400.0 / bitmap.getWidth()));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(bytes, Base64.NO_WRAP);

            db.collection("users").document(currentUserId)
                    .update("profileImageUrl", encodedImage)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadFriendsList() {
        if (viewedUser.getFriends() != null && !viewedUser.getFriends().isEmpty()) {
            db.collection("users").whereIn("uid", viewedUser.getFriends()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                userFriendsList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        if (user.getUid() == null) user.setUid(doc.getId());
                        userFriendsList.add(user);
                    }
                }
                friendsAdapter.notifyDataSetChanged();
                calculateCommonFriends();
            });
        } else {
            userFriendsList.clear();
            friendsAdapter.notifyDataSetChanged();
        }
    }

    private void calculateCommonFriends() {
        if (profileUserId.equals(currentUserId)) return;

        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> myFriends = (List<String>) documentSnapshot.get("friends");
                if (myFriends != null && viewedUser.getFriends() != null) {
                    List<String> commonIds = new ArrayList<>(myFriends);
                    commonIds.retainAll(viewedUser.getFriends());

                    if (!commonIds.isEmpty()) {
                        findViewById(R.id.cardCommonFriends).setVisibility(View.VISIBLE);
                        db.collection("users").whereIn("uid", commonIds).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            commonFriendsList.clear();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                if (user != null) {
                                    if (user.getUid() == null) user.setUid(doc.getId());
                                    commonFriendsList.add(user);
                                }
                            }
                            commonFriendsAdapter.notifyDataSetChanged();
                        });
                    } else {
                        findViewById(R.id.cardCommonFriends).setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void addGameIcon(LinearLayout container, String gameName) {
        ImageView imageView = new ImageView(this);
        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        int marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(marginInPx, 0, marginInPx, 0);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String fileName = gameName.toLowerCase().trim().replace(" ", "_").replace("-", "_").replace(":", "").replace("'", "");
        if (fileName.contains("stardew")) fileName = "stardew_valley";
        else if (fileName.contains("maple")) fileName = "maplestory";
        else if (fileName.contains("counter") || fileName.contains("csgo")) fileName = "csgo";
        else if (fileName.contains("league") || fileName.contains("lol")) fileName = "league_of_legends";

        int resId = getResources().getIdentifier(fileName, "drawable", getPackageName());
        if (resId != 0) {
            imageView.setImageResource(resId);
            container.addView(imageView);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_help);
            container.addView(imageView);
        }
    }

    private void showEditDialog(String title, String fieldKey, String currentValue, int inputType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_prefix) + title);
        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setText(currentValue);
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {
            String newValue = input.getText().toString();
            db.collection("users").document(currentUserId).update(fieldKey, newValue)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void checkFriendshipStatus() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> myFriends = (List<String>) documentSnapshot.get("friends");
                if (myFriends != null && myFriends.contains(profileUserId)) {
                    btnAddFriend.setVisibility(View.GONE);
                    btnRemoveFriend.setVisibility(View.VISIBLE);
                    btnRemoveFriend.setOnClickListener(v -> removeFriend());
                } else {
                    btnAddFriend.setVisibility(View.VISIBLE);
                    btnRemoveFriend.setVisibility(View.GONE);
                    btnAddFriend.setOnClickListener(v -> addFriend());
                }
            }
        });
    }

    private void addFriend() {
        db.collection("users").document(currentUserId).update("friends", FieldValue.arrayUnion(profileUserId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(profileUserId).update("friends", FieldValue.arrayUnion(currentUserId))
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, getString(R.string.friend_added), Toast.LENGTH_SHORT).show();
                                checkFriendshipStatus();
                            });
                });
    }

    private void removeFriend() {
        db.collection("users").document(currentUserId).update("friends", FieldValue.arrayRemove(profileUserId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(profileUserId).update("friends", FieldValue.arrayRemove(currentUserId))
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, getString(R.string.friend_removed), Toast.LENGTH_SHORT).show();
                                checkFriendshipStatus();
                            });
                });
    }
}