package com.example.ggconnect;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetupProfileActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etBio;
    private CheckBox cbFortnite, cbStardewvalley, cbFifa, cbMinecraft, cbLoL, cbMapleStory, cbCSGO, cbValorant;
    private Button btnSaveProfile;
    private LinearLayout layoutPersonalDetails;
    private TextView tvTitle;

    private ImageView ivProfileImage;
    private String encodedImage = null; 

    private FirebaseFirestore db;
    private boolean isEditingGamesOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        db = FirebaseFirestore.getInstance();
        isEditingGamesOnly = getIntent().getBooleanExtra("EDIT_GAMES_ONLY", false);

        tvTitle = findViewById(R.id.tvTitle);
        layoutPersonalDetails = findViewById(R.id.layoutPersonalDetails);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etBio = findViewById(R.id.etBio);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        cbFortnite = findViewById(R.id.cbFortnite);
        cbStardewvalley = findViewById(R.id.cbStardewvalley);
        cbFifa = findViewById(R.id.cbFifa);
        cbMinecraft = findViewById(R.id.cbMinecraft);
        cbLoL = findViewById(R.id.cbLoL);
        cbMapleStory = findViewById(R.id.cbmaplestory);
        cbCSGO = findViewById(R.id.cbCSGO);
        cbValorant = findViewById(R.id.cbValorant);

        if (isEditingGamesOnly) {
            if (tvTitle != null) tvTitle.setText(getString(R.string.edit_games));
            if (layoutPersonalDetails != null) layoutPersonalDetails.setVisibility(View.GONE);
            if (ivProfileImage != null) ivProfileImage.setVisibility(View.GONE);
            loadExistingGames();
        } else {
            if (ivProfileImage != null) {
                ivProfileImage.setOnClickListener(v -> mGetContent.launch("image/*"));
            }
        }

        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivProfileImage.setImageURI(uri);
                    ivProfileImage.setPadding(0, 0, 0, 0);
                    ivProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    encodedImage = encodeImage(uri);
                }
            });

    private String encodeImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Resize bitmap to avoid Firestore document limits (1MB)
            int width = 400;
            int height = (int) (bitmap.getHeight() * (400.0 / bitmap.getWidth()));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadExistingGames() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> games = (List<String>) documentSnapshot.get("games");
                if (games != null) {
                    if (games.contains("Fortnite")) cbFortnite.setChecked(true);
                    if (games.contains("Stardew Valley")) cbStardewvalley.setChecked(true);
                    if (games.contains("FIFA")) cbFifa.setChecked(true);
                    if (games.contains("Minecraft")) cbMinecraft.setChecked(true);
                    if (games.contains("League of Legends")) cbLoL.setChecked(true);
                    if (games.contains("MapleStory")) cbMapleStory.setChecked(true);
                    if (games.contains("CSGO")) cbCSGO.setChecked(true);
                    if (games.contains("Valorant")) cbValorant.setChecked(true);
                }
            }
        });
    }

    private void saveUserProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> selectedGames = new ArrayList<>();
        if (cbFortnite.isChecked()) selectedGames.add("Fortnite");
        if (cbStardewvalley.isChecked()) selectedGames.add("Stardew Valley");
        if (cbFifa.isChecked()) selectedGames.add("FIFA");
        if (cbMinecraft.isChecked()) selectedGames.add("Minecraft");
        if (cbLoL.isChecked()) selectedGames.add("League of Legends");
        if (cbMapleStory.isChecked()) selectedGames.add("MapleStory");
        if (cbCSGO.isChecked()) selectedGames.add("CSGO");
        if (cbValorant.isChecked()) selectedGames.add("Valorant");

        if (selectedGames.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_games), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditingGamesOnly) {
            db.collection("users").document(userId)
                    .update("games", selectedGames)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, getString(R.string.games_updated), Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            String name = etFullName.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            if (name.isEmpty() || age.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("name", name);
            userProfile.put("age", age);
            userProfile.put("bio", bio);
            userProfile.put("games", selectedGames);
            userProfile.put("uid", userId);
            userProfile.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            userProfile.put("online", true);

            if (encodedImage != null) {
                userProfile.put("profileImageUrl", encodedImage);
            }

            db.collection("users").document(userId)
                    .set(userProfile)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SetupProfileActivity.this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(SetupProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}