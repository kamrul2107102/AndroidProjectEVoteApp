package com.example.easyvote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class HomePage extends AppCompatActivity {

    // declaring the class members
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    private static final String PREF_WORK_MANAGER_INITIALIZED = "workManagerInitialized";
    private boolean isWorkManagerInitialized;







    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // initialization of firebase class members and view class members
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = fAuth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(HomePage.this, Login_Page.class);
            startActivity(intent);
            finish();
        }

        setWorkManager();



        LinearLayout linearLayout = findViewById(R.id.mainLayout);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        replacefragment(new HomeFragment());
        toolbar.setTitle("Ongoing Votes");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();


                drawerLayout.closeDrawer(GravityCompat.START);

                switch (id){
                    case R.id.nav_home:
                        toolbar.setTitle("Vote List");

                        replacefragment(new HomeFragment());
                        break;
                    case R.id.nav_groups:
                        toolbar.setTitle("My Groups");
                        replacefragment(new GroupFragment());
                        break;

                    case R.id.ppl:
                        toolbar.setTitle("Voter List");
                        replacefragment(new VoterListFragment());
                        break;

                    case R.id.logout:
                        new AlertDialog.Builder(HomePage.this)
                                .setMessage("Are you sure want to logout")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        logout();
                                        finish();
                                    }
                                })
                                .setNegativeButton("No",null)
                                .show();
                        break;


                    case R.id.nav_share:
                        Intent intent = new Intent(HomePage.this,SharedPolls.class);
                        startActivity(intent);

                }

                return true;
            }
        });



        try {
            userID = fAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            Toast.makeText(HomePage.this, e.toString(), Toast.LENGTH_SHORT).show();
        }



        DocumentReference documentreference = fStore.collection("users").document(userID);
        documentreference.addSnapshotListener(HomePage.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle the error
                    Toast.makeText(HomePage.this,error.getMessage().toString(),Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {

                    NavigationView navigationView = findViewById(R.id.nav_view);

                    // Get the header view
                    View headerView = navigationView.getHeaderView(0);

                    // Find the TextView inside the header view by its id
                    TextView navHeaderNameText = headerView.findViewById(R.id.Unameheader);
                    TextView navHeaderEmailText = headerView.findViewById(R.id.userID);

                    // Change the text
                    navHeaderNameText.setText(documentSnapshot.getString("username"));
                    navHeaderEmailText.setText("UID - "+documentSnapshot.getString("userID"));

                }
            }
        });




    }



    public void logout(){
        FirebaseAuth.getInstance().signOut(); //logout
        Intent intent = new Intent(HomePage.this,Login_Page.class);
        startActivity(intent);
    }


    //fragments loading method
    private void replacefragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame,fragment);
        fragmentTransaction.commit();

    }


    private void schedulePeriodicNotification() {
        // Create network constraints
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Create a PeriodicWorkRequest with the network constraints for the notification task
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        // Enqueue the periodic task using WorkManager
        WorkManager.getInstance(this).enqueue(periodicWorkRequest);
    }



    private void setWorkManager(){
        // Check if WorkManager is already initialized
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        isWorkManagerInitialized = preferences.getBoolean(PREF_WORK_MANAGER_INITIALIZED, false);

        if (!isWorkManagerInitialized) {
            // Configure WorkManager here
            schedulePeriodicNotification();

            // Update the flag to indicate that WorkManager has been initialized
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_WORK_MANAGER_INITIALIZED, true);
            editor.apply();
        }
    }


}