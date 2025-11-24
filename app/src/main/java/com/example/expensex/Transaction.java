package com.example.expensex;

public class Transaction {

    private String title;
    private String amount;
    private String date;
    private String note;
    private String type; // added this field ("income" or "expense")

    public Transaction() {
        // Required empty constructor for Firebase
    }

    public Transaction(String title, String amount, String date, String note, String type) {
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.type = type;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setType(String type) {
        this.type = type;
    }
}
