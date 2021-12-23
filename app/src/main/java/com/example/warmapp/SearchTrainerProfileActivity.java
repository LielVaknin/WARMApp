package com.example.warmapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.warmapp.classes.SearchTrainerProfileAdapter;
import com.example.warmapp.classes.UserTrainer;
import com.example.warmapp.traineeActivities.SearchActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class SearchTrainerProfileActivity extends AppCompatActivity {

    EditText searchTrainerEt;
    Button searchTrainerBtn;
    Button switchToTrainingSearchBtn;
    RecyclerView filteredTrainers;

    SearchTrainerProfileAdapter myAdapter;
    ArrayList<UserTrainer> trainers;
    ArrayList<String> trainersId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_search_trainer_profile);

        searchTrainerEt = findViewById(R.id.search_trainer_edtxt);
        filteredTrainers = findViewById(R.id.trainers_recyclerview);
        searchTrainerBtn = findViewById(R.id.search_trainer_btn);
        switchToTrainingSearchBtn = findViewById(R.id.switch_to_training_search);

        trainers = new ArrayList<>();
        trainersId = new ArrayList<>();
        searchTrainerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        trainers.clear();
                        trainersId.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UserTrainer userTrainer = dataSnapshot.getValue(UserTrainer.class);
                            String userId = dataSnapshot.getKey();
                            assert userTrainer != null;
                            String userTrainerFirstName = userTrainer.getFirstName();
                            String userTrainerLastName = userTrainer.getLastName();
                            String trainerNameSelected = findTrainerNameSelected();
                            if (userTrainer.getUserType().equals("trainer") && ((userTrainerFirstName + " " + userTrainerLastName).toLowerCase().startsWith(trainerNameSelected.toLowerCase()) || trainerNameSelected.toLowerCase().equals((userTrainerFirstName + " " + userTrainerLastName).toLowerCase()) ||  userTrainerFirstName.toLowerCase().startsWith(trainerNameSelected.toLowerCase()) || userTrainerLastName.toLowerCase().endsWith(trainerNameSelected.toLowerCase()))) {
                                trainers.add(userTrainer);
                                trainersId.add(userId);
                                setAdapter();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        switchToTrainingSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchTrainerProfileActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    public String findTrainerNameSelected() {
        String name = searchTrainerEt.getText().toString();
        return name;
    }


    public void setAdapter() {
        myAdapter = new SearchTrainerProfileAdapter(this, trainers, trainersId);
        filteredTrainers.setAdapter(myAdapter);
        filteredTrainers.setLayoutManager(new LinearLayoutManager(this));
        filteredTrainers.addItemDecoration(new DividerItemDecoration(filteredTrainers.getContext(), DividerItemDecoration.VERTICAL));
    }
}