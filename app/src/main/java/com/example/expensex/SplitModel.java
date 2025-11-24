package com.example.expensex;

public class SplitModel {
    private String title, amount, members, eachShare, date;
    private boolean splitEqually;

    public SplitModel() {}

    public SplitModel(String title, String amount, String members, boolean splitEqually, String eachShare, String date) {
        this.title = title;
        this.amount = amount;
        this.members = members;
        this.splitEqually = splitEqually;
        this.eachShare = eachShare;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getAmount() { return amount; }
    public String getMembers() { return members; }
    public boolean isSplitEqually() { return splitEqually; }
    public String getEachShare() { return eachShare; }
    public String getDate() { return date; }
}
