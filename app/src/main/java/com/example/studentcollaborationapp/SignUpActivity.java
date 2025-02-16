package com.example.studentcollaborationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etFullName; // Added etFullName
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private TextView tvGoToSignIn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Bind Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName); // Added binding for full name
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        tvGoToSignIn = findViewById(R.id.tvGoToSignIn);

        // Sign Up Button Click Listener
        btnSignUp.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim(); // Get full name

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
                Toast.makeText(SignUpActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;


            }

            progressBar.setVisibility(View.VISIBLE);

            // Create User in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                // Save User Details in Firebase Realtime Database
                                String userId = user.getUid();
                                User userProfile = new User(fullName, email); // Create User Object
                                databaseReference.child(userId).setValue(userProfile)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                                                // Clear the current session and redirect to SignInActivity
                                                auth.signOut(); // Optional: Ensure no logged-in session persists
                                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                                finish(); // Close the SignUpActivity
                                            } else {
                                                Toast.makeText(SignUpActivity.this, "Failed to save user details", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

// User Model Class
class User {
    private String fullName;
    private String email;
    private String profileImageUrl;

    public User() {} // Default Constructor required for Firebase

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
