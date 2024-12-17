package com.example.easyvote.adaptors;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.easyvote.R;

import java.util.ArrayList;

public class PollListAdaptor extends BaseAdapter {

    ArrayList<String> options;
    ArrayList<String> optionNumber;

    LayoutInflater inflater;
    Context context;
    public PollListAdaptor(Context context, ArrayList<String> options, ArrayList<String> optionNumber){
        this.context=context;
        this.options = options;
        this.optionNumber = optionNumber;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.poll_list_layout, null);
        TextView textView = (TextView) convertView.findViewById(R.id.options);
        TextView textView2 = (TextView) convertView.findViewById(R.id.number);
        textView.setText(options.get(position));
        textView2.setText(optionNumber.get(position));

       /* // Setting the background of the row to a custom drawable pregramatically
        convertView.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_background2));*/



        return convertView;
    }
}


