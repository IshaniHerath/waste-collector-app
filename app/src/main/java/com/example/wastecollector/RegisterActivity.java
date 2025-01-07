package com.example.wastecollector;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;


public class RegisterActivity extends AppCompatActivity {
    private TextView createAccountTextView;
    private ImageButton btn_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.register_page);

        createAccountTextView = findViewById(R.id.text_login_link);
        btn_menu = findViewById(R.id.btn_menu);


        createAccountTextView.setOnClickListener(v -> {
            // Navigate to the Login page
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btn_menu.setOnClickListener(this::showPopupMenu);
    }

    // Popup Menu
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Go Back");
        popupMenu.getMenu().add("Logout");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Go Back":
                    onBackPressed(); // Navigate back
                    return true;
                case "Logout":
                    logoutUser();
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void logoutUser() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}