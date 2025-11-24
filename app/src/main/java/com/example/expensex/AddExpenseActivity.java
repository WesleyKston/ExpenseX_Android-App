package com.example.expensex;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class AddExpenseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottomsheet_add_transaction);
    }
}
