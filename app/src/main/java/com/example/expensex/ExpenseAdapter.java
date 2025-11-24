package com.example.expensex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private ArrayList<Transaction> transactionList;

    public ExpenseAdapter(Context context, ArrayList<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_modern, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Transaction t = transactionList.get(position);

        holder.tvTitle.setText(t.getTitle());
        holder.tvDate.setText(t.getDate());

        // Safely format amount even if it's a string
        try {
            double amt = Double.parseDouble(t.getAmount());
            holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", amt));
        } catch (NumberFormatException e) {
            holder.tvAmount.setText("₹" + t.getAmount());
        }

        // Set icon and color based on type
        if (t.getType() != null && t.getType().equalsIgnoreCase("income")) {
            holder.tvAmount.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.ivCategory.setImageResource(R.drawable.ic_income);
        } else {
            holder.tvAmount.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.ivCategory.setImageResource(R.drawable.ic_expense);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvDate;
        ImageView ivCategory;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            ivCategory = itemView.findViewById(R.id.ivCategory);
        }
    }
}
