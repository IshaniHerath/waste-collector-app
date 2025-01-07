package com.example.wastecollector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class RouteActivity extends AppCompatActivity {
    private ImageButton btn_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.route_page);

        btn_menu = findViewById(R.id.btn_menu);

        btn_menu.setOnClickListener(v -> showPopupMenu(v));
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
        Intent intent = new Intent(RouteActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}