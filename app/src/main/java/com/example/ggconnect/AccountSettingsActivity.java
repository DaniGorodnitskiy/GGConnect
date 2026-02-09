package com.example.ggconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountSettingsActivity extends AppCompatActivity {

    private EditText etNewEmail, etNewPassword;
    private Button btnUpdateEmail, btnUpdatePassword;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        etNewEmail = findViewById(R.id.etNewEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnUpdateEmail = findViewById(R.id.btnUpdateEmail);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        btnUpdateEmail.setOnClickListener(v -> {
            String newEmail = etNewEmail.getText().toString().trim();
            if (!newEmail.isEmpty()) {
                user.updateEmail(newEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountSettingsActivity.this, getString(R.string.email_updated), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountSettingsActivity.this, getString(R.string.error_reauth), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnUpdatePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                user.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountSettingsActivity.this, getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountSettingsActivity.this, getString(R.string.error_reauth), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}