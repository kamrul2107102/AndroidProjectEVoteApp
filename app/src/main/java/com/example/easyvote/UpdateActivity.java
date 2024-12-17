package com.example.easyvote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private EditText updatedName, updatedPhone, updatedAddress;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("root/employees");

        // Get passed employee ID
        String id = getIntent().getStringExtra("id");

        // Link UI elements
        updatedName = findViewById(R.id.updated_employee_name);
        updatedPhone = findViewById(R.id.updated_employee_phone_number);
        updatedAddress = findViewById(R.id.updated_employee_address);
        updateButton = findViewById(R.id.update_button);

        // Handle update button click
        updateButton.setOnClickListener(v -> {
            String name = updatedName.getText().toString().trim();
            String phone = updatedPhone.getText().toString().trim();
            String address = updatedAddress.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty() && !address.isEmpty() && id != null) {
                Employee employee = new Employee(id, name, phone, address);
                databaseReference.child(id).setValue(employee)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(UpdateActivity.this, "Employee updated!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UpdateActivity.this, MainActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(UpdateActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(UpdateActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
