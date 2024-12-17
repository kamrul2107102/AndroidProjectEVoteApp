package com.example.easyvote.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyvote.EmployeeListModel;
import com.example.easyvote.R;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EmployeeListAdapter extends RecyclerView.Adapter<EmployeeListAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<EmployeeListModel> arrayList;

    public EmployeeListAdapter(Context context, ArrayList<EmployeeListModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeListModel currentEmployee = arrayList.get(position);
        holder.employeeName.setText(currentEmployee.getEmployeeName());
        holder.employeePhone.setText(currentEmployee.getEmployeePhone());
        holder.employeeAddress.setText(currentEmployee.getEmployeeAddress());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName, employeePhone, employeeAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            employeeName = itemView.findViewById(R.id.employee_name);
            employeePhone = itemView.findViewById(R.id.employee_phone);
            employeeAddress = itemView.findViewById(R.id.employee_address);
        }
    }
}
