package com.example.studentcollaborationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // Navigation Drawer Components
    private TextView tvWelcome, tvUserEmail, tvNavFullName, tvNavEmail;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    // File Upload Components
    private EditText etFileName, etFileUrl, etDescription;
    private Button btnUpload;
    // Firebase Components
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views for Navigation Drawer
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Initialize Views for File Upload
        etFileName = findViewById(R.id.etFileName);
        etFileUrl = findViewById(R.id.etFileUrl);
        etDescription = findViewById(R.id.etDescription);
        btnUpload = findViewById(R.id.btnUpload);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set up ActionBarDrawerToggle for the Navigation Drawer
        toggle = new ActionBarDrawerToggle(
                this, // Activity
                drawerLayout, // DrawerLayout
                R.string.open, // Open drawer description
                R.string.close // Close drawer description
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Enable the hamburger icon in the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Set up Navigation Drawer Header
        try {
            View headerView = navigationView.getHeaderView(0);
            tvNavFullName = headerView.findViewById(R.id.tvNavFullName);
            tvNavEmail = headerView.findViewById(R.id.tvNavEmail);
        } catch (Exception e) {
            tvWelcome.setText("Error setting up Navigation Drawer: " + e.getMessage());
        }

        // Check if the user is logged in
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Redirect to sign-in screen if the user is not logged in
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
            return;
        }
        String userId = user.getUid();
        String userEmail = user.getEmail();

        // Display user's email in the main content and Navigation Drawer
        tvUserEmail.setText("Welcome, " + userEmail);
        if (tvNavEmail != null) {
            tvNavEmail.setText(userEmail);
        }

        // Fetch and display the full name from Firebase
        databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User userProfile = snapshot.getValue(User.class);
                    if (userProfile != null) {
                        String fullName = userProfile.getFullName();
                        // Update UI with user details
                        tvWelcome.setText("Welcome, " + fullName);
                        if (tvNavFullName != null) {
                            tvNavFullName.setText(fullName);
                        }
                    } else {
                        Log.w("MainActivity", "User data is null");
                    }
                } else {
                    Log.w("MainActivity", "No data found for user ID: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    Log.e("MainActivity", "Permission denied: " + error.getMessage());
                    Toast.makeText(MainActivity.this, "Permission denied. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("MainActivity", "Error fetching user data: " + error.getMessage());
                    tvWelcome.setText("Error fetching user data: " + error.getMessage());
                }
            }
        });

        // Handle Navigation Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set Upload Button Click Listener
        btnUpload.setOnClickListener(v -> uploadFile());

        // Add Notification Permission Check (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to handle file upload
    private void uploadFile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values
        String fileName = etFileName.getText().toString().trim();
        String fileUrl = etFileUrl.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fileUrl) || !fileUrl.startsWith("http")) {
            Toast.makeText(this, "Please enter a valid file URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique file ID
        String fileId = databaseReference.child("Files").push().getKey();
        if (fileId == null) {
            Toast.makeText(this, "Failed to generate file ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create file metadata
        HashMap<String, Object> fileData = new HashMap<>();
        fileData.put("fileName", fileName);
        fileData.put("fileUrl", fileUrl);
        fileData.put("description", description);
        fileData.put("uploadedBy", user.getUid());
        fileData.put("timestamp", getCurrentTimestamp());

        // Save to Firebase Realtime Database
        databaseReference.child("Files").child(fileId).setValue(fileData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "File uploaded successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Log.e("Upload Error", e.getMessage());
                    Toast.makeText(this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Clear input fields after successful upload
    private void clearFields() {
        etFileName.setText("");
        etFileUrl.setText("");
        etDescription.setText("");
    }

    // Helper method to get current timestamp
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}