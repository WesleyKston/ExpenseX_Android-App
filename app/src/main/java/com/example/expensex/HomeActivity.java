package com.example.expensex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class HomeActivity extends BaseActivity {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recentTransactionsRecycler;
    private ArrayList<Transaction> transactionList;
    private TransactionAdapter transactionAdapter;
    private View cardAddBill;
    private TextView tvTotalBill, tvSplitExpense;
    private DatabaseReference billsRef, splitsRef;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvTotalBill = findViewById(R.id.tvTotalBill);
        tvSplitExpense = findViewById(R.id.tvSplitExpense);

        billsRef = FirebaseDatabase.getInstance().getReference("Bills");
        splitsRef = FirebaseDatabase.getInstance().getReference("Splits");

        setupBottomNav(R.id.nav_home);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Bills").child(auth.getUid());

        // Initialize Views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        cardAddBill = findViewById(R.id.cardAddBill);
        recentTransactionsRecycler = findViewById(R.id.recentTransactionsRecycler);

        // RecyclerView setup
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        recentTransactionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentTransactionsRecycler.setAdapter(transactionAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recentTransactionsRecycler.addItemDecoration(divider);

        // Load Firebase data
        loadTransactions();
        loadDashboardTotals();

        // Add Bill click
        cardAddBill.setOnClickListener(v -> openAddBillBottomSheet());

        // Profile click(Move to Settings Screen)
        ImageView ivProfile = findViewById(R.id.ivProfile);

        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });

    }

    // Bottom Sheet for adding new bill
    private void openAddBillBottomSheet() {
        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(HomeActivity.this, R.style.BottomSheetTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_add_bill, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText etBillTitle = sheetView.findViewById(R.id.etBillTitle);
        EditText etBillAmount = sheetView.findViewById(R.id.etBillAmount);
        EditText etSplitCount = sheetView.findViewById(R.id.etSplitCount);
        EditText etBillNote = sheetView.findViewById(R.id.etBillNote);
        Button btnSaveBill = sheetView.findViewById(R.id.btnSaveBill);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("Bills").child(auth.getUid());

        btnSaveBill.setOnClickListener(v -> {
            String title = etBillTitle.getText().toString().trim();
            String amount = etBillAmount.getText().toString().trim();
            String split = etSplitCount.getText().toString().trim();
            String note = etBillNote.getText().toString().trim();

            if (title.isEmpty() || amount.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Please enter title and amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (split.isEmpty()) split = "1";

            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            HashMap<String, Object> expense = new HashMap<>();
            expense.put("title", title);
            expense.put("amount", amount);
            expense.put("splitCount", split);
            expense.put("note", note);
            expense.put("date", date);
            expense.put("type", "expense");


            dbRef.push().setValue(expense)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Bill added successfully", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        } else {
                            Toast.makeText(HomeActivity.this, "Failed to add bill", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        bottomSheetDialog.show();
    }

    // Load bills dynamically
    // Load the most recent transactions (like Track screen)
    private void loadTransactions() {
        transactionList.clear();

        DatabaseReference billsRef = FirebaseDatabase.getInstance()
                .getReference("Bills");

        billsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    // Case 1: Direct transaction object
                    if (ds.hasChild("title") && ds.hasChild("amount")) {
                        Transaction t = ds.getValue(Transaction.class);
                        if (t != null) transactionList.add(t);
                    }

                    // Case 2: Nested transaction (under user ID)
                    else {
                        for (DataSnapshot inner : ds.getChildren()) {
                            if (inner.hasChild("title") && inner.hasChild("amount")) {
                                Transaction t = inner.getValue(Transaction.class);
                                if (t != null) transactionList.add(t);
                            } else {
                                for (DataSnapshot nested : inner.getChildren()) {
                                    if (nested.hasChild("title") && nested.hasChild("amount")) {
                                        Transaction t = nested.getValue(Transaction.class);
                                        if (t != null) transactionList.add(t);
                                    }
                                }
                            }
                        }
                    }
                }

                Collections.reverse(transactionList);
                transactionAdapter.notifyDataSetChanged();

                View emptyLayout = findViewById(R.id.emptyStateLayout);
                if (transactionList.isEmpty()) {
                    recentTransactionsRecycler.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                } else {
                    recentTransactionsRecycler.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Load dashboard totals safely
    // Load dashboard totals safely
    private void loadDashboardTotals() {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        DatabaseReference billsRef = FirebaseDatabase.getInstance()
                .getReference("Bills");

        DatabaseReference splitsRef = FirebaseDatabase.getInstance()
                .getReference("Splits")
                .child(currentUserId);

        // TOTAL EXPENSE (matches Track screen's purple card)
        billsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalExpense = 0.0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t == null) continue;

                    try {
                        String amountStr = t.getAmount();
                        String type = t.getType() != null ? t.getType() : "expense";

                        if (type.equalsIgnoreCase("expense") && amountStr != null && !amountStr.isEmpty()) {
                            totalExpense += Double.parseDouble(amountStr.trim());
                        }
                    } catch (Exception ignored) {}
                }

                tvTotalBill.setText("₹" + Utils.formatCurrency(totalExpense));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalBill.setText("₹0.00");
            }
        });

        // YOUR SHARE (matches Split screen’s green card)
        splitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double yourShare = 0.0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    SplitModel split = ds.getValue(SplitModel.class);
                    if (split == null) continue;

                    try {
                        String amountStr = split.getAmount();
                        if (amountStr != null && !amountStr.trim().isEmpty()) {
                            double amount = Double.parseDouble(amountStr.trim());
                            int members = 1;
                            if (split.getMembers() != null && !split.getMembers().isEmpty()) {
                                members = split.getMembers().split(",").length;
                            }
                            yourShare += (amount / members);
                        }
                    } catch (Exception ignored) {}
                }

                tvSplitExpense.setText("₹" + Utils.formatCurrency(yourShare));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvSplitExpense.setText("₹0.00");
            }
        });
    }






}
