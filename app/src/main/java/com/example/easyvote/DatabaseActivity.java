package com.example.easyvote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DatabaseActivity extends AppCompatActivity {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("root/employees");
    private EditText employeeName, employeePhone, employeeAddress;
    private Button addButton, updateButton, deleteButton, viewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_databse_fragment);


        // Link UI components
        employeeName = findViewById(R.id.employee_name);
        employeePhone = findViewById(R.id.employee_phone_number);
        employeeAddress = findViewById(R.id.employee_address);
        addButton = findViewById(R.id.add_button);
        updateButton = findViewById(R.id.update_button);
        deleteButton = findViewById(R.id.delete_button);
        viewButton = findViewById(R.id.read_button);

        // Add Button Click Listener
        addButton.setOnClickListener(v -> {
            String name = employeeName.getText().toString().trim();
            String phone = employeePhone.getText().toString().trim();
            String address = employeeAddress.getText().toString().trim();

            if (validateInputs(name, phone, address)) {
                addEmployee(name, phone, address);
            }
        });

        // Update Button Click Listener
        updateButton.setOnClickListener(v -> {
            String name = employeeName.getText().toString().trim();
            String phone = employeePhone.getText().toString().trim();
            String address = employeeAddress.getText().toString().trim();

            if (validateInputs(name, phone, address)) {
                updateEmployee(name, phone, address);
            }
        });

        // Delete Button Click Listener
        deleteButton.setOnClickListener(v -> {
            String name = employeeName.getText().toString().trim();
            String phone = employeePhone.getText().toString().trim();
            String address = employeeAddress.getText().toString().trim();

            if (validateInputs(name, phone, address)) {
                deleteEmployee(name, phone, address);
            }
        });

        // View Button Click Listener
        viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(DatabaseActivity.this, ReadActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInputs(String name, String phone, String address) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (address.isEmpty()) {
            Toast.makeText(this, "Address field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addEmployee(String name, String phone, String address) {
        String id = databaseReference.push().getKey();
        if (id != null) {
            Employee employee = new Employee(id, name, phone, address);
            Toast.makeText(DatabaseActivity.this, employee.getName()+"added", Toast.LENGTH_SHORT).show();
            databaseReference.child(id).setValue(employee);
//                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Employee added successfully", Toast.LENGTH_SHORT).show())
//                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add employee: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateEmployee(String name, String phone, String address) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = childSnapshot.getValue(Employee.class);
                    if (employee != null && Objects.equals(employee.getName(), name)) {
                        employee.setPhone(phone);
                        employee.setAddress(address);
                        databaseReference.child(employee.getId()).setValue(employee)
                                .addOnSuccessListener(aVoid -> Toast.makeText(DatabaseActivity.this, "Employee updated successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(DatabaseActivity.this, "Failed to update employee: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        return;
                    }
                }
                Toast.makeText(DatabaseActivity.this, "Voter not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DatabaseActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteEmployee(String name, String phone, String address) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Employee employee = childSnapshot.getValue(Employee.class);
                    if (employee != null && Objects.equals(employee.getName(), name) && Objects.equals(employee.getPhone(), phone) && Objects.equals(employee.getAddress(), address)) {
                        databaseReference.child(employee.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(DatabaseActivity.this, "Employee deleted successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(DatabaseActivity.this, "Failed to delete employee: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        return;
                    }
                }
                Toast.makeText(DatabaseActivity.this, "Voter not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DatabaseActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
