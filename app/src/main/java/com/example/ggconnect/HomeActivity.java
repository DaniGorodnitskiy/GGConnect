package com.example.ggconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> allUsersList;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu, btnFilterToggle;
    private SearchView searchView;
    
    // Filter Components
    private CardView filterPanel;
    private RadioGroup rgFilterType;
    private RangeSlider ageSlider;
    private TextView tvAgeRangeLabel;
    
    private float minAge = 0;
    private float maxAge = 100;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        allUsersList = new ArrayList<>();

        // Setup Drawer and Navigation
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnMenu = findViewById(R.id.btnMenu);
        
        navigationView.setNavigationItemSelectedListener(this);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        updateNavHeader();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        // Setup Search & Filters
        searchView = findViewById(R.id.searchView);
        btnFilterToggle = findViewById(R.id.btnFilterToggle);
        filterPanel = findViewById(R.id.filterPanel);
        rgFilterType = findViewById(R.id.rgFilterType);
        ageSlider = findViewById(R.id.ageSlider);
        tvAgeRangeLabel = findViewById(R.id.tvAgeRangeLabel);

        btnFilterToggle.setOnClickListener(v -> {
            if (filterPanel.getVisibility() == View.VISIBLE) {
                filterPanel.setVisibility(View.GONE);
            } else {
                filterPanel.setVisibility(View.VISIBLE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilters();
                return true;
            }
        });

        rgFilterType.setOnCheckedChangeListener((group, checkedId) -> applyFilters());

        ageSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minAge = values.get(0);
            maxAge = values.get(1);
            tvAgeRangeLabel.setText(getString(R.string.age_range_label) + (int)minAge + " - " + (int)maxAge);
            applyFilters();
        });
    }

    private void applyFilters() {
        List<User> filteredList = new ArrayList<>();
        String query = currentSearchQuery.toLowerCase().trim();
        boolean searchByUsername = (rgFilterType.getCheckedRadioButtonId() == R.id.rbUsername);

        for (User user : allUsersList) {
            boolean matchesSearch = false;
            
            // 1. Search Logic
            if (query.isEmpty()) {
                matchesSearch = true;
            } else if (searchByUsername) {
                if (user.getName() != null && user.getName().toLowerCase().contains(query)) {
                    matchesSearch = true;
                }
            } else {
                if (user.getGames() != null) {
                    for (String game : user.getGames()) {
                        if (game.toLowerCase().contains(query)) {
                            matchesSearch = true;
                            break;
                        }
                    }
                }
            }

            // 2. Age Logic
            boolean matchesAge = true;
            if (user.getAge() != null && !user.getAge().isEmpty()) {
                try {
                    int userAge = Integer.parseInt(user.getAge());
                    if (userAge < minAge || userAge > maxAge) {
                        matchesAge = false;
                    }
                } catch (NumberFormatException e) {
                    // If age isn't a number, skip age filtering for this user
                }
            }

            if (matchesSearch && matchesAge) {
                filteredList.add(user);
            }
        }
        userAdapter.setFilteredList(filteredList);
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView navName = headerView.findViewById(R.id.nav_header_name);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            navEmail.setText(email);

            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    navName.setText(name != null ? name : getString(R.string.user_default_name));
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            startActivity(intent);
        } else if (id == R.id.nav_friends) {
            startActivity(new Intent(HomeActivity.this, FriendsActivity.class));
        } else if (id == R.id.nav_groups) {
            startActivity(new Intent(HomeActivity.this, GroupsActivity.class));
        } else if (id == R.id.nav_account_settings) {
            startActivity(new Intent(HomeActivity.this, AccountSettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            setUserOnlineStatus(false);
            FirebaseAuth.getInstance().signOut();
        }
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            setUserOnlineStatus(true);
            loadUsersFromFirestore();
        } else {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userListener != null) userListener.remove();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            setUserOnlineStatus(false);
        }
    }

    private void setUserOnlineStatus(boolean isOnline) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId)
                .update("online", isOnline)
                .addOnFailureListener(e -> Log.e("HomeActivity", "Failed status update", e));
    }

    private void loadUsersFromFirestore() {
        if (userListener != null) userListener.remove();
        userListener = db.collection("users").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null || queryDocumentSnapshots == null || FirebaseAuth.getInstance().getCurrentUser() == null) return;
            userList.clear();
            allUsersList.clear();
            String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    if (user.getUid() == null) user.setUid(document.getId());
                    if (!user.getUid().equals(myId)) {
                        userList.add(user);
                        allUsersList.add(user);
                    }
                }
            }
            applyFilters();
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}