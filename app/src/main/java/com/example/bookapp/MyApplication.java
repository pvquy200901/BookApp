package com.example.bookapp;
import java.util.Calendar;
import java.util.Locale;

import android.app.Application;
import android.text.format.DateFormat;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp){

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);

        String date = DateFormat.format("dd/MM/yyyy",cal).toString();
        return date;
    }
}
