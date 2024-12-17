package com.example.easyvote.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.easyvote.R;

import java.util.ArrayList;

public class PollListAdaptor extends BaseAdapter {

    private ArrayList<String> options;
    private ArrayList<String> optionNumber;

    private LayoutInflater inflater;
    private Context context;

    // Constructor
    public PollListAdaptor(Context context, ArrayList<String> options, ArrayList<String> optionNumber){
        this.context = context;
        this.options = options;
        this.optionNumber = optionNumber;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int position) {
        return options.get(position);  // Return the option at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;  // Return the position as the item ID (this can be used for further customization)
    }

    // ViewHolder pattern for better performance
    static class ViewHolder {
        TextView textView;
        TextView textView2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // Reuse view if it's already available (to avoid inflating it every time)
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.poll_list_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = convertView.findViewById(R.id.options);
            viewHolder.textView2 = convertView.findViewById(R.id.number);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();  // Use the cached view
        }

        // Set the data for the row
        viewHolder.textView.setText(options.get(position));
        viewHolder.textView2.setText(optionNumber.get(position));

        return convertView;
    }

    // Method to update the list data and refresh the ListView
    public void updateOptions(ArrayList<String> options, ArrayList<String> optionNumber) {
        this.options = options;
        this.optionNumber = optionNumber;
        notifyDataSetChanged();  // Refresh the ListView
    }
}
