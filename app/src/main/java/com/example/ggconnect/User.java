package com.example.ggconnect;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String uid;
    private String email;
    private String age;
    private String bio;
    private List<String> games;
    private boolean online;
    private List<String> friends; // רשימת ה-ID של החברים
    private String profileImageUrl; // שדה חדש לתמונת פרופיל (Base64)

    public User() {
        this.friends = new ArrayList<>();
    }

    public User(String name, String uid, String email, String age, String bio, List<String> games, String profileImageUrl) {
        this.name = name;
        this.uid = uid;
        this.email = email;
        this.age = age;
        this.bio = bio;
        this.games = games;
        this.online = false;
        this.friends = new ArrayList<>();
        this.profileImageUrl = profileImageUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getAge() { return age; }
    public String getBio() { return bio; }
    public List<String> getGames() { return games; }
    public boolean isOnline() { return online; }
    public String getProfileImageUrl() { return profileImageUrl; } // Getter חדש

    public List<String> getFriends() {
        if (friends == null) friends = new ArrayList<>();
        return friends;
    }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setUid(String uid) { this.uid = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(String age) { this.age = age; }
    public void setBio(String bio) { this.bio = bio; }
    public void setGames(List<String> games) { this.games = games; }
    public void setOnline(boolean online) { this.online = online; }
    public void setFriends(List<String> friends) { this.friends = friends; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; } // Setter חדש
}