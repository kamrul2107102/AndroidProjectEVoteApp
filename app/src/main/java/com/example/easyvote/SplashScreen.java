package com.example.easyvote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {

                    // Perform necessary operations
                   PollUtil pollUtil = new PollUtil(SplashScreen.this);

                   pollUtil.updatePollStatus(new PollUtil.OnExpireListner() {
                        @Override
                        public void onExpire() {
                            Toast.makeText(SplashScreen.this, "Your poll is expired", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onActive() {

                        }

                        @Override
                        public void onError() {
                            Toast.makeText(SplashScreen.this, "Updating Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                    pollUtil.updateAllGroupPollStatus(new PollUtil.OnExpireListner() {
                        @Override
                        public void onExpire() {
                            Toast.makeText(SplashScreen.this, "Group poll expired", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onActive() {

                        }

                        @Override
                        public void onError() {
                            Toast.makeText(SplashScreen.this, "Updating Error", Toast.LENGTH_SHORT).show();

                        }
                    });

                    pollUtil.updateAllGroupPollStatus(new PollUtil.OnExpireListner() {
                        @Override
                        public void onExpire() {

                        }

                        @Override
                        public void onActive() {

                        }

                        @Override
                        public void onError() {

                        }
                    });

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SplashScreen.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 2000); // 3 second delay
                }
            });
        } else {
            Intent intent = new Intent(SplashScreen.this, Login_Page.class);
            startActivity(intent);
            finish();
        }
    }



}
