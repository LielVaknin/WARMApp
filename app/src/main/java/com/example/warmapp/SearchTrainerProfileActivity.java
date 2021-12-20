package com.example.warmapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;

public class SearchTrainerProfileActivity extends AppCompatActivity {

    EditText searchTrainer;
    RecyclerView filteredTrainers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_trainer_profile);

        searchTrainer = findViewById(R.id.search_trainer_bar);
        filteredTrainers = findViewById(R.id.trainers_recyclerview);

    }
}