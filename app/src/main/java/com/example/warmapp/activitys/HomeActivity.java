package com.example.warmapp.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.warmapp.R;
import com.example.warmapp.adapters.DynamicTrainingsAdapter;
import com.example.warmapp.adapters.TypesTrainingsAdapter;
import com.example.warmapp.adapters.UpdateRVTrainingsByType;
import com.example.warmapp.adapters.MyTrainingsAdapter;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TypesTrainings;
import com.example.warmapp.classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Objects;


public class HomeActivity extends AppCompatActivity implements UpdateRVTrainingsByType {
    //activity
    private BottomNavigationView bottomNavigationView;
    private CircularImageView circularUserImageView;
    private TextView textViewName;
    private RecyclerView recyclerViewYourTrainings, recyclerViewTypeTrainings, recyclerViewShowTypeTrainings;
    private TypesTrainingsAdapter typesTrainingsAdapter;
    private DynamicTrainingsAdapter dynamicTrainingsAdapter;
    private MyTrainingsAdapter myTrainingsAdapter;
    private ArrayList<Training> userTrainings;
    private ArrayList<TypesTrainings> types;
    private String[] titles;
    private ProgressDialog progressDialog;

    //firebase
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_home);

        initViews();
        setUpBottomNavigation();
        updateUserDetails();
        initTypesList();

    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerViewYourTrainings = findViewById(R.id.home_activity_your_trainings_recycler_view);
        recyclerViewTypeTrainings = findViewById(R.id.home_activity_type_trainings_recycler_view);
        recyclerViewShowTypeTrainings = findViewById(R.id.home_activity_type_trainings_show_recycler_view);
        circularUserImageView = findViewById(R.id.home_activity_circular_image_view);
        textViewName = findViewById(R.id.home_activity_hi_name_text);
    }

    private void setUpBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.menu_profile:
                        intent = new Intent(HomeActivity.this, AccountActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_search:
                        intent = new Intent(HomeActivity.this, SearchActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(HomeActivity.this, CalendarActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        intent = new Intent(HomeActivity.this, RequestsActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_home:
                        intent = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    private void updateUserDetails() {
        FirebaseDatabase.getInstance().getReference("Users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        textViewName.setText("Hi " + Objects.requireNonNull(user).getFirstName() + "!");
                        //TODO::circularUserImageView

//                        showUserTrainings();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showUserTrainings() {
        recyclerViewYourTrainings.setHasFixedSize(true);
        recyclerViewYourTrainings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

//        userTrainings = new ArrayList<>();
//        myTrainingsAdapter = new MyTrainingsAdapter(this, userTrainings, "trainer", userID);
//        recyclerViewYourTrainings.setAdapter(myTrainingsAdapter);

        //over trainings user and get the training from database
        if (user.getTrainings() != null) {
            for (String training : user.getTrainings().keySet()) {
                FirebaseDatabase.getInstance().getReference("Trainings").child(training)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Training training = snapshot.getValue(Training.class);
                                userTrainings.add(training);
                                //Log.d("training", Objects.requireNonNull(training).toString());
                                myTrainingsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }
    }

    private void initTypesList() {
        types = new ArrayList<>();
        titles = getResources().getStringArray(R.array.Titles);
        for (String title : titles) {
            types.add(new TypesTrainings(R.drawable.ic_activity, title));
        }

        typesTrainingsAdapter = new TypesTrainingsAdapter(this,types,this);
        recyclerViewTypeTrainings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTypeTrainings.setAdapter(typesTrainingsAdapter);

//        ArrayList<Training> trainingsType = new ArrayList<>();
//
//        dynamicTrainingsAdapter = new DynamicTrainingsAdapter(this,trainingsType);
        recyclerViewShowTypeTrainings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        recyclerViewShowTypeTrainings.setAdapter(dynamicTrainingsAdapter);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void callback(int position, ArrayList<Training> trainings) {
        dynamicTrainingsAdapter = new DynamicTrainingsAdapter(this,trainings);
        dynamicTrainingsAdapter.notifyDataSetChanged();
        recyclerViewShowTypeTrainings.setAdapter(dynamicTrainingsAdapter);
    }
}