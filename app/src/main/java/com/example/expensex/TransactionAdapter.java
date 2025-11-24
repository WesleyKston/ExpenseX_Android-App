package com.example.expensex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_modern, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactionList.get(position);

        String title = t.getTitle() != null ? t.getTitle() : "Untitled";
        String date = t.getDate() != null ? t.getDate() : "Unknown date";
        String amountStr = t.getAmount() != null ? t.getAmount() : "0";

        double amount = 0.0;
        try {
            amount = Double.parseDouble(amountStr.trim());
        } catch (Exception ignored) {}

        holder.tvTitle.setText(title);
        holder.tvDate.setText(date);
        holder.tvAmount.setText("₹" + String.format("%.2f", amount));

        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("trip")) {
            holder.ivCategory.setImageResource(R.drawable.bg_icon_trip);
        } else if (lowerTitle.contains("food") || lowerTitle.contains("dinner")) {
            holder.ivCategory.setImageResource(R.drawable.bg_icon_food);
        } else if (lowerTitle.contains("service")) {
            holder.ivCategory.setImageResource(R.drawable.bg_icon_services);
        } else {
            holder.ivCategory.setImageResource(R.drawable.bg_icon_food);
        }
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvAmount;
        ImageView ivCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivCategory = itemView.findViewById(R.id.ivCategory);
        }
    }
}
