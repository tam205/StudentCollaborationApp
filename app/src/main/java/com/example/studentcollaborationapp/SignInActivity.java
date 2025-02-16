package com.example.studentcollaborationapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    // UI elements
    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvGoToSignUp;
    private ProgressBar progressBar;
    private com.google.android.gms.common.SignInButton btnGoogleSignIn;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your web client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        progressBar = findViewById(R.id.progressBar);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // Email & Password Sign-In
        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(SignInActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            navigateToMainActivity();
                        } else {
                            Log.e(TAG, "Authentication failed: " + task.getException().getMessage());
                            Toast.makeText(SignInActivity.this, "Login Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Navigate to SignUp Activity
        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });

        // Google Sign-In
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // Register for Google Sign-In result
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            try {
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                if (account != null) {
                                    Log.d(TAG, "Google Sign-In successful: " + account.getEmail());
                                    firebaseAuthWithGoogle(account.getIdToken());
                                } else {
                                    Log.e(TAG, "Google Sign-In failed: Account is null.");
                                    Toast.makeText(SignInActivity.this, "Google Sign-In failed: Account is null.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ApiException e) {
                                Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode() + " - " + e.getMessage());
                                switch (e.getStatusCode()) {
                                    case 10: // DEVELOPER_ERROR
                                        Log.e(TAG, "Developer error: Check SHA-1, Web Client ID, and Firebase setup.");
                                        break;
                                    case 12500: // INTERNAL_ERROR
                                        Log.e(TAG, "Internal error: Ensure Google Play Services is up-to-date.");
                                        break;
                                    default:
                                        Log.e(TAG, "Unknown error code: " + e.getStatusCode());
                                }
                                Toast.makeText(SignInActivity.this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Google Sign-In canceled or failed.");
                            Toast.makeText(SignInActivity.this, "Google Sign-In canceled or failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        if (signInIntent != null) {
            Log.d(TAG, "Launching Google Sign-In Intent.");
            signInLauncher.launch(signInIntent);
        } else {
            Log.e(TAG, "Google Sign-In Intent is null");
            Toast.makeText(this, "Google Sign-In Intent is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Log.e(TAG, "ID Token is null");
            Toast.makeText(this, "ID Token is null", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Received ID Token: " + idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Authentication successful with Google.");
                        navigateToMainActivity();
                    } else {
                        Log.e(TAG, "Firebase Authentication failed: " + task.getException().getMessage());
                        if (task.getException() != null) {
                            Log.e(TAG, "Error details: ", task.getException());
                        }
                        Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Signed in as: " + user.getEmail());
            Toast.makeText(this, "Welcome, " + user.getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close SignInActivity
        } else {
            Log.e(TAG, "User is null after authentication.");
            Toast.makeText(this, "Failed to retrieve user information.", Toast.LENGTH_SHORT).show();
        }
    }
}