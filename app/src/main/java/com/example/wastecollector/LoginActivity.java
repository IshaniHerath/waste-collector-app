package com.example.wastecollector;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_page);

        // Initialize views
        EditText usernameEditText = findViewById(R.id.edit_username);
        EditText passwordEditText = findViewById(R.id.edit_password);
        Button loginButton = findViewById(R.id.button_login);
        TextView createAccountTextView = findViewById(R.id.text_create_account);


        // Set on click listener for login button
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Check if the username and password match the hardcoded values
            if ("izzy".equals(username) && "123".equals(password)) {
                // If credentials are correct, navigate to DashboardActivity
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish(); // Optional: finish the LoginActivity so user can't go back to it
            } else {
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        createAccountTextView.setOnClickListener(v -> {
            // Navigate to Register page
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }
}