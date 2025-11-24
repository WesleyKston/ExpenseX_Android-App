package com.example.expensex;

import java.text.NumberFormat;
import java.util.Locale;

public class Utils {

    // Currency Formatter (Indian-style with 2 decimals)
    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount);
    }
}
