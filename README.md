# GGConnect - Gamer Social Network

GGConnect is a specialized social networking application designed for gamers to connect, find teammates, and build communities based on their favorite games. The app provides a seamless experience for discovering players with similar interests and communicating with them in real-time.

## 🚀 Key Features

*   **Multilingual Support**: Fully localized in **Hebrew (RTL)** and **English (LTR)**, with a dedicated language selection screen using modern `AppCompatDelegate` localization.
*   **Authentication & Account Management**: 
    *   Secure Login and Registration via **Firebase Authentication**.
    *   Account Settings to update email and password.
*   **Dynamic Player Profiles**:
    *   Customizable profiles including Name, Age, Bio, and favorite games.
    *   **Profile Pictures**: Support for uploading and viewing profile images stored as optimized **Base64** strings in Firestore.
    *   In-app profile editing for personal details and game preferences.
*   **Player Discovery & Filtering**:
    *   Real-time search of the player database via **Firestore**.
    *   Advanced filtering by **Username**, **Game**, and **Age Range** using a Material RangeSlider.
*   **Social Connectivity**:
    *   Friend system: Add and remove friends.
    *   **Common Friends**: View mutual connections when visiting other players' profiles.
    *   Friends list view to manage your connections.
*   **Real-time Chat**:
    *   One-on-one messaging system.
    *   Dynamic chat interface with message gravity based on sender (Me vs. Other).
*   **Status Tracking**: Real-time online/offline status indicators for all users.

## 🛠 Tech Stack

*   **Language**: Java
*   **Platform**: Android (Min SDK 24+)
*   **Backend**: 
    *   **Firebase Authentication**: User identity management.
    *   **Firebase Firestore**: Real-time NoSQL database for users, messages, and social connections.
*   **UI Components**:
    *   **Material Design Components**: RangeSliders, CardViews, NavigationDrawers.
    *   **RecyclerView**: Efficient listing of users, friends, and chat messages.
    *   **Glide**: (Optional/Setup) Optimized image loading.
*   **Image Handling**: Custom Base64 encoding/decoding logic for serverless image storage.

## 📋 Project Structure

### Activities
*   `LanguageActivity`: Entry point for choosing the app language.
*   `MainActivity`: Login and Registration hub.
*   `HomeActivity`: Main feed with search, filtering, and navigation drawer.
*   `SetupProfileActivity`: Comprehensive profile creation and game selection.
*   `UserProfileActivity`: Detailed view of personal and other players' profiles.
*   `ChatActivity`: Real-time messaging interface.
*   `FriendsActivity`: Dedicated view for user's friend list.
*   `AccountSettingsActivity`: Security management (Email/Password).
*   `GroupsActivity`: Community hub (Feature coming soon).

### Adapters
*   `UserAdapter`: Handles the display of player cards in search and friend lists.
*   `ChatAdapter`: Manages the real-time message bubble layout and logic.

### Models
*   `User`: Data model representing a player profile.
*   `Message`: Data model for chat interactions.

## ⚙️ Setup Instructions

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/yourusername/GGConnect.git
    ```
2.  **Firebase Configuration**:
    *   Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android App with your package name (`com.example.ggconnect`).
    *   Download the `google-services.json` file and place it in the `app/` directory.
    *   Enable **Email/Password Authentication**.
    *   Enable **Cloud Firestore** and set up your security rules.
3.  **Build the Project**:
    *   Open the project in **Android Studio**.
    *   Sync Gradle files.
    *   Run the app on an emulator or physical device.

---
Developed as a modern Android application with a focus on community and clean architecture.
