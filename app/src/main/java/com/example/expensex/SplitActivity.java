package com.example.expensex;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
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

public class SplitActivity extends BaseActivity {

    private Button btnAddBill;
    private RecyclerView recyclerViewSplits;
    private ArrayList<SplitModel> splitList;
    private SplitAdapter splitAdapter;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split);

        setupBottomNav(R.id.nav_split);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("Splits").child(currentUserId);

        btnAddBill = findViewById(R.id.btnAddBill);
        recyclerViewSplits = findViewById(R.id.recyclerViewSplits);

        splitList = new ArrayList<>();
        recyclerViewSplits.setLayoutManager(new LinearLayoutManager(this));
        splitAdapter = new SplitAdapter(this, splitList);
        recyclerViewSplits.setAdapter(splitAdapter);

        loadSplitData();

        btnAddBill.setOnClickListener(v -> openAddSplitBottomSheet());


    }

    private void loadSplitData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                splitList.clear();
                double totalBill = 0.0;
                double myShare = 0.0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    SplitModel split = ds.getValue(SplitModel.class);
                    if (split != null) {
                        splitList.add(split);

                        try {
                            double amount = Double.parseDouble(split.getAmount());
                            totalBill += amount;

                            // Calculate your share based on member count
                            int memberCount = 1;
                            if (split.getMembers() != null && !split.getMembers().isEmpty()) {
                                memberCount = split.getMembers().split(",").length;
                            }

                            // Assume current user is part of all splits (you can filter later)
                            myShare += (amount / memberCount);

                        } catch (NumberFormatException ignored) {}
                    }
                }

                // Update UI
                TextView tvTotalBill = findViewById(R.id.tvTotalBill);
                TextView tvYourShare = findViewById(R.id.tvYourShare);

                tvTotalBill.setText("₹" + Utils.formatCurrency(totalBill));
                tvYourShare.setText("₹" + Utils.formatCurrency(myShare));

                splitAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SplitActivity.this, "Failed to load split data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openAddSplitBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_add_split_bill, null);
        dialog.setContentView(view);

        TextInputEditText etBillTitle = view.findViewById(R.id.etBillTitle);
        TextInputEditText etTotalAmount = view.findViewById(R.id.etTotalAmount);
        TextInputEditText etMembers = view.findViewById(R.id.etMembers);
        com.google.android.material.switchmaterial.SwitchMaterial switchSplitEqually =
                view.findViewById(R.id.switchSplitEqually);
        Button btnAddSplit = view.findViewById(R.id.btnAddSplit);

        // Date for record
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        // 🔹 Handle member selection
        etMembers.setOnClickListener(v -> openMemberSelectionBottomSheet(etMembers));

        btnAddSplit.setOnClickListener(v -> {
            String title = etBillTitle.getText().toString().trim();
            String totalAmount = etTotalAmount.getText().toString().trim();
            String members = etMembers.getText().toString().trim();

            if (title.isEmpty() || totalAmount.isEmpty() || members.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int count = members.split(",").length;
            double eachShare = 0;
            try {
                double total = Double.parseDouble(totalAmount);
                if (switchSplitEqually.isChecked()) {
                    eachShare = total / count;
                }
            } catch (NumberFormatException ignored) {}

            HashMap<String, Object> splitData = new HashMap<>();
            splitData.put("title", title);
            splitData.put("amount", totalAmount);
            splitData.put("members", members);
            splitData.put("splitEqually", switchSplitEqually.isChecked());
            splitData.put("eachShare", String.format(Locale.getDefault(), "%.2f", eachShare));
            splitData.put("date", date);

            dbRef.push().setValue(splitData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Split bill added!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add bill", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void openMemberSelectionBottomSheet(TextInputEditText etMembers) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_select_members, null);
        dialog.setContentView(view);

        Button btnConfirmMembers = view.findViewById(R.id.btnConfirmMembers);
        ArrayList<String> selectedMembers = new ArrayList<>();

        // Handle each checkbox (Nathan, Michele, Jugram, Ryan)
        view.findViewById(R.id.checkNathan).setOnClickListener(v -> toggleMember(v, "Nathan", selectedMembers));
        view.findViewById(R.id.checkMichele).setOnClickListener(v -> toggleMember(v, "Michele", selectedMembers));
        view.findViewById(R.id.checkJugram).setOnClickListener(v -> toggleMember(v, "Jugram", selectedMembers));
        view.findViewById(R.id.checkRyan).setOnClickListener(v -> toggleMember(v, "Ryan", selectedMembers));

        btnConfirmMembers.setOnClickListener(v -> {
            etMembers.setText(String.join(", ", selectedMembers));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void toggleMember(View v, String name, ArrayList<String> list) {
        if (v instanceof android.widget.CheckBox) {
            android.widget.CheckBox checkBox = (android.widget.CheckBox) v;
            if (checkBox.isChecked()) {
                list.add(name);
            } else {
                list.remove(name);
            }
        }
    }
}
