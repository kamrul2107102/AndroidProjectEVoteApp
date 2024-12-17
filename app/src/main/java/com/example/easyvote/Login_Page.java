package com.example.easyvote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login_Page extends AppCompatActivity {

    Button buttonLogin;
    EditText etUsername, etPassword;
    TextView createAccount;
    ProgressBar progressBar;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Initialize UI components
        createAccount = findViewById(R.id.create_account);
        etUsername = findViewById(R.id.Username);
        etPassword = findViewById(R.id.Password);
        buttonLogin = findViewById(R.id.login);
        progressBar = findViewById(R.id.pbar2);
        fAuth = FirebaseAuth.getInstance();

        // Set initial progress bar visibility
        progressBar.setVisibility(View.INVISIBLE);

        // Navigate to the registration page
        createAccount.setOnClickListener(v -> {
            startActivity(new Intent(Login_Page.this, MainActivity.class)); // Redirect to SignUp Activity
            finish();
        });

        // Handle login button click
        buttonLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim(); // Use email here (Username UI ID is misleading)
            String password = etPassword.getText().toString().trim();

            // Check for empty fields
            if (TextUtils.isEmpty(email)) {
                etUsername.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                return;
            }

            // Show progress bar while logging in
            progressBar.setVisibility(View.VISIBLE);

            // Authenticate the user
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.INVISIBLE); // Hide progress bar after login attempt
                    if (task.isSuccessful()) {
                        // Redirect to HomePage if login is successful
                        startActivity(new Intent(Login_Page.this, HomePage.class));
                        finish();
                    } else {
                        // Handle authentication failure
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(Login_Page.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            // If the user is already logged in, redirect to HomePage
            startActivity(new Intent(this, HomePage.class));
            finish();
        }
    }
}
