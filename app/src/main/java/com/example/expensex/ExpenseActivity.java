package com.example.expensex;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ExpenseActivity extends BaseActivity {

    private TextView tvGreeting, tvSelectedMonth, tvTotalAmount, tvExpenseAmount, tvIncomeAmount;
    private RecyclerView expenseRecyclerView;
    private LinearLayout monthSelector, cardExpense, cardIncome, emptyStateLayout;
    private ExpenseAdapter expenseAdapter;
    private ArrayList<Transaction> transactionList = new ArrayList<>();

    private FirebaseAuth auth;
    private DatabaseReference dbRef, userRef;
    private String selectedMonthYear, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        setupBottomNav(R.id.nav_track);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("Bills");
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        // Initialize views
        tvGreeting = findViewById(R.id.tvGreeting);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount);
        tvIncomeAmount = findViewById(R.id.tvIncomeAmount);
        monthSelector = findViewById(R.id.monthSelector);
        cardExpense = findViewById(R.id.cardExpense);
        cardIncome = findViewById(R.id.cardIncome);
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this, transactionList);
        expenseRecyclerView.setAdapter(expenseAdapter);

        // Load user name
        loadUserName();

        // Default month
        selectedMonthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvSelectedMonth.setText(selectedMonthYear);

        // Load bills
        loadTransactionsForMonth(selectedMonthYear);

        // Listeners
        monthSelector.setOnClickListener(v -> showMonthPicker());
        cardExpense.setOnClickListener(v -> openAddExpenseBottomSheet());
        cardIncome.setOnClickListener(v -> openAddIncomeBottomSheet());

    }



    // Load username from Firebase
    private void loadUserName() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    tvGreeting.setText("Hello, " + name + "!");
                } else {
                    tvGreeting.setText("Hello, User!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvGreeting.setText("Hello!");
            }
        });
    }

    private void showMonthPicker() {
        final String[] months = new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);

        ArrayList<String> monthYearList = new ArrayList<>();
        for (int year = currentYear - 1; year <= currentYear + 1; year++) {
            for (String m : months) {
                monthYearList.add(m + " " + year);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Month")
                .setItems(monthYearList.toArray(new String[0]), (dialog, which) -> {
                    selectedMonthYear = monthYearList.get(which);
                    tvSelectedMonth.setText(selectedMonthYear);
                    loadTransactionsForMonth(selectedMonthYear);
                })
                .show();
    }

    private void loadTransactionsForMonth(String monthYear) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                double total = 0.0, totalExpense = 0.0, totalIncome = 0.0;
                String selectedMonthShort = monthYear.split(" ")[0].substring(0, 3);

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t == null || t.getDate() == null) continue;

                    if (t.getDate().contains(selectedMonthShort)) {
                        transactionList.add(t);

                        try {
                            double amt = Double.parseDouble(t.getAmount());
                            String type = t.getType() != null ? t.getType() : "expense";

                            if (type.equalsIgnoreCase("income")) {
                                totalIncome += amt;
                                total += amt;
                            } else {
                                totalExpense += amt;
                                total -= amt;
                            }

                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (transactionList.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    expenseRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    expenseRecyclerView.setVisibility(View.VISIBLE);
                }

                tvTotalAmount.setText("₹" + Utils.formatCurrency(total));
                tvIncomeAmount.setText("₹" + Utils.formatCurrency(totalIncome));
                tvExpenseAmount.setText("₹" + Utils.formatCurrency(totalExpense));

                expenseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpenseActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddExpenseBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_add_transaction, null);
        dialog.setContentView(view);

        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etNote = view.findViewById(R.id.etNote);
        TextView tvDate = view.findViewById(R.id.tvDate);
        Button btnSave = view.findViewById(R.id.btnSave);

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvDate.setText(date);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (title.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> expense = new HashMap<>();
            expense.put("title", title);
            expense.put("amount", amount);
            expense.put("note", note);
            expense.put("date", date);
            expense.put("type", "expense");

            dbRef.push().setValue(expense)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void openAddIncomeBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_add_income, null);
        dialog.setContentView(view);

        EditText etSource = view.findViewById(R.id.etIncomeSource);
        EditText etAmount = view.findViewById(R.id.etIncomeAmount);
        TextView tvDate = view.findViewById(R.id.tvDate);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvDate.setText(date);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String source = etSource.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();

            if (source.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> income = new HashMap<>();
            income.put("title", source);
            income.put("amount", amount);
            income.put("note", "Income added");
            income.put("date", date);
            income.put("type", "income");

            dbRef.push().setValue(income)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Income added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}
