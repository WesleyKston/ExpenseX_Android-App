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

public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.SplitViewHolder> {

    private Context context;
    private ArrayList<SplitModel> list;

    public SplitAdapter(Context context, ArrayList<SplitModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SplitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_split_bill, parent, false);
        return new SplitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitViewHolder holder, int position) {
        SplitModel s = list.get(position);

        holder.tvTitle.setText(s.getTitle());
        holder.tvMembers.setText(s.getMembers());
        holder.tvDate.setText(s.getDate());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%s", s.getAmount()));
        holder.tvEachShare.setText(String.format(Locale.getDefault(), "Each ₹%s", s.getEachShare()));

        // Optional: Tint the icon
        holder.ivIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_blue_dark));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SplitViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMembers, tvDate, tvAmount, tvEachShare;
        ImageView ivIcon;

        public SplitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSplitTitle);
            tvMembers = itemView.findViewById(R.id.tvSplitMembers);
            tvDate = itemView.findViewById(R.id.tvSplitDate);
            tvAmount = itemView.findViewById(R.id.tvSplitAmount);
            tvEachShare = itemView.findViewById(R.id.tvEachShare);
            ivIcon = itemView.findViewById(R.id.ivSplitIcon);
        }
    }
}
