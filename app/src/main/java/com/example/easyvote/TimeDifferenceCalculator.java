package com.example.easyvote;

import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeDifferenceCalculator {

    public static String calculateTimeDifference(String savedDateTimeString) {
        // Parse the saved date and time from the database
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
        Date savedDateTime;
        try {
            savedDateTime = dateFormat.parse(savedDateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid date format"; // Handle the parsing exception
        }

        // Get the current date and time
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Calculate the difference
        long timeDifferenceInMilliseconds =  savedDateTime.getTime() - currentDate.getTime();
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceInMilliseconds);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceInMilliseconds) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceInMilliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceInMilliseconds) % 60;

        // Format the time difference
        if (days > 0) {
            return String.format("%dd %dh", days, hours);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    public static String getCurrentTimeFormatted() {
        // Get the current time
        LocalTime currentTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentTime = LocalTime.now();
        }

        // Define the desired time format
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        }

        // Format the current time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return currentTime.format(formatter);
        }else{
            return "null";
        }

    }


}
