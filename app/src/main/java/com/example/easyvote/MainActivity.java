package com.example.easyvote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Collator;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    TextView Birthday;
    int year_current;
    int month_current;
    int date_current;
    int month_cal;
    int yeat_cal;
    int age;
    RadioGroup rgroup;
    RadioButton radioButton;
    EditText etEmail, etPassword, etUsername;
    Button button;
    String userID;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    ProgressBar progressBar;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regitration);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        rgroup = findViewById(R.id.rGroup);
        button = findViewById(R.id.reg);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        progressBar = findViewById(R.id.pbar);
        progressBar.setVisibility(View.INVISIBLE);
        Birthday = findViewById(R.id.Birthday);
        calendar = Calendar.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();


        Birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String username = etUsername.getText().toString();

                if(TextUtils.isEmpty(username)){
                    etUsername.setError("User Name is required");
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    etEmail.setError("Email is Required");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    etPassword.setError("Password Required");
                    return;
                }
                if(password.length()<6){
                    etPassword.setError("Password must be longer than 6 characters");
                    return;
                }

                int radioID = rgroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioID);
                String Gender = radioButton.getText().toString();

                if(age==0){
                    Toast.makeText(MainActivity.this,"please enter valid Birthday",Toast.LENGTH_SHORT).show();
                    showDatePickerDialog();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);




                // Creating a new user
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            createCustomUserId(new onIDCreateListner() {
                                @Override
                                public void onIdCreated(String uniqueID) {

                                    userID = fAuth.getCurrentUser().getUid();
                                    DocumentReference documentreference = fstore.collection("users").document(userID);
                                    Map<String,Object> user = new HashMap<>();
                                    user.put("username",username);
                                    user.put("gender",Gender);
                                    user.put("age",age);
                                    user.put("birth_year",year_current);
                                    user.put("birth_month",month_current);
                                    user.put("birth_date",date_current);
                                    user.put("email",email);
                                    user.put("userID",uniqueID);

                                    documentreference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(MainActivity.this, "Registration Successfull",Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(MainActivity.this,Login_Page.class);
                                            startActivity(intent);
                                        }
                                    });

                                }

                                @Override
                                public void onFail() {

                                }
                            });


                        }
                        else{
                            Toast.makeText(MainActivity.this,"Registration Failed",Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    }
                });





            }
        });



    }

    public void createCustomUserId(onIDCreateListner listner){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("users");

        String fieldName = "customID";
        String targetValue = CustomIdGenerator.generateCustomId(8);

        collectionRef.whereEqualTo(fieldName, targetValue)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().isEmpty()){
                                listner.onIdCreated(targetValue);
                            }else{
                                createCustomUserId(listner);
                            }
                        }else{
                            listner.onFail();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        listner.onFail();
                        Toast.makeText(getApplicationContext(),"onfailuire",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showDatePickerDialog(){
        year_current = calendar.get(Calendar.YEAR);
        month_current = calendar.get(Calendar.MONTH);
        date_current =  calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog=new DatePickerDialog(this, this, year_current, month_current, date_current);

        datePickerDialog.show();
    }

    public void onRadioButtonClicked(View v){
        int radioID = rgroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioID);
        Toast.makeText(this,radioButton.getText(),Toast.LENGTH_SHORT).show();
    }


    // check the selecting of birthday and calculate aga according to the input (this will pass the input dates)
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month = month+1;
        String date = dayOfMonth+" / "+ month +"/ "+ year;
        Birthday.setText(date);
        month_cal = month;
        yeat_cal = year;
        if(date_current-dayOfMonth<0){
            month_cal= month - 1;

        }
        if(month_current-month_cal<0){
            yeat_cal = year - 1;
        }

        age = year_current-yeat_cal;
        month_current = month;
        date_current = dayOfMonth;
        year_current = year;
        Toast.makeText(this,"your age is "+age,Toast.LENGTH_SHORT).show();
    }

    public interface onIDCreateListner{
        void onIdCreated(String uniqueID);

        void onFail();
    }
}
