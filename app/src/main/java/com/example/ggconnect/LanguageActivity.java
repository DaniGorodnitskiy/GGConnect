package com.example.ggconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class LanguageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If user is already logged in, skip language selection and go to Home
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_language);

        findViewById(R.id.btnEnglish).setOnClickListener(v -> setAppLocale("en"));
        findViewById(R.id.btnHebrew).setOnClickListener(v -> setAppLocale("iw")); // 'iw' is the code for Hebrew
    }

    private void setAppLocale(String localeCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(localeCode);
        AppCompatDelegate.setApplicationLocales(appLocale);

        // The system will now handle recreating the activity stack with the new locale.
        // We just need to launch the next activity.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}