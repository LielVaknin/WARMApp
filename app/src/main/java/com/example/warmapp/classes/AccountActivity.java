package com.example.warmapp.classes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.warmapp.HomeActivity;
import com.example.warmapp.LoginActivity;
import com.example.warmapp.R;
import com.example.warmapp.traineeActivities.SearchActivity;
import com.example.warmapp.trainerActivities.CalendarActivity;
import com.example.warmapp.trainerActivities.RequestsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {
    FirebaseAuth auth;
    String userType ;
    String userID;
    Button logOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        BottomNavigationView bottomNavigationView =findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_profile);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.menu_profile:
                        return true;
                    case R.id.menu_search:
                        intent = new Intent(AccountActivity.this, SearchActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(AccountActivity.this, CalendarActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        intent = new Intent(AccountActivity.this, RequestsActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_home:
                        intent = new Intent(AccountActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;



            }
        });
        auth= FirebaseAuth.getInstance();

        logOut=findViewById(R.id.log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this,LoginActivity.class);
                startActivity(intent);
                auth.signOut();
                finish();
            }
        });


    }
}