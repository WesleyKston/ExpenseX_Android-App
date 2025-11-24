package com.example.expensex;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNav(int selectedItemId) {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView == null) return;

        // Set listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                if (!(BaseActivity.this instanceof HomeActivity)) {
                    Intent intent = new Intent(BaseActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (id == R.id.nav_track) {
                if (!(BaseActivity.this instanceof ExpenseActivity)) {
                    Intent intent = new Intent(BaseActivity.this, ExpenseActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (id == R.id.nav_split) {
                if (!(BaseActivity.this instanceof SplitActivity)) {
                    Intent intent = new Intent(BaseActivity.this, SplitActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
                return true;
            }

            return false;
        });

        // Fix highlight issue by deferring selection until UI resumes
        bottomNavigationView.post(() -> bottomNavigationView.setSelectedItemId(selectedItemId));
    }

    // This ensures correct icon stays highlighted even when activity is reused (FLAG_ACTIVITY_REORDER_TO_FRONT)
    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            if (this instanceof HomeActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            } else if (this instanceof ExpenseActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_track);
            } else if (this instanceof SplitActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_split);
            }
        }
    }
}
