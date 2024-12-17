package com.example.easyvote.adaptors;


import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;


public class AgeRangeXAxisValueFormatter extends ValueFormatter implements IAxisValueFormatter {
    private int[] ageRanges;

    public AgeRangeXAxisValueFormatter(int[] ageRanges) {
        this.ageRanges = ageRanges;
    }

    @Override
    public String getFormattedValue(float value) {
        int index = (int) value;

        if (index >= 0 && index < ageRanges.length - 1) {
            // Return the age range label
            return String.valueOf(ageRanges[index]+10);
        } else if (index == ageRanges.length - 1) {
            // Handle the last label
            return String.valueOf(ageRanges[index - 1])+10 + "+";
        }

        return ""; // out-of-bounds values
    }
}
