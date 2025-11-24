package com.example.expensex;

public class Expense {
    private String title;
    private String amount;
    private String date;
    private String category;
    private int splitCount;

    // Required empty constructor for Firebase
    public Expense() { }

    // Convenience constructor
    public Expense(String title, String amount, String date, String category, int splitCount) {
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.splitCount = splitCount;
    }

    // Getters & Setters (Firebase needs these)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSplitCount() { return splitCount; }
    public void setSplitCount(int splitCount) { this.splitCount = splitCount; }
}
