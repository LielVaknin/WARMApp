package com.example.warmapp.traineeActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.warmapp.LoginActivity;
import com.example.warmapp.R;
import com.example.warmapp.SignUpActivity;
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.RequestModel;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainee;
import com.example.warmapp.classes.UserTrainer;
import com.example.warmapp.trainerActivities.CalendarActivity;
import com.example.warmapp.trainerActivities.requests_trainer_RecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TraineeRequestsActivity extends AppCompatActivity {

    ArrayList<RequestModel> requests;
    DatabaseReference firebaseReference;
    FirebaseAuth auth;
    RecyclerView recyclerView;
    TextView hasRequests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_requests);
        auth = FirebaseAuth.getInstance();
        //String userID=auth.getCurrentUser().getUid();
        String userID="mEMcSemGPIMgWbP9anZ668m5KHH2";

        recyclerView = findViewById(R.id.trainee_requests_list);
        requests=new ArrayList<>();
        firebaseReference= FirebaseDatabase.getInstance().getReference();
        BottomNavigationView bottomNavigationView =findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_requests);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
               @Override
               public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                   Intent intent;
                   switch (item.getItemId()){
                       case R.id.menu_profile:

                        /*intent = new Intent(TraineeRequestsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();*/
                           return true;
                       case R.id.menu_search:
                           intent = new Intent(TraineeRequestsActivity.this, SearchActivity.class);
                           startActivity(intent);
                           finish();
                           return true;
                       case R.id.menu_schedule:
                           intent = new Intent(TraineeRequestsActivity.this, CalendarActivity.class);
                           startActivity(intent);
                           finish();
                           return true;
                       case R.id.menu_requests:
                           return true;
                       case R.id.menu_home:
                           return true;
                   }
                return false;



               }
           });



        setUpRequests(userID);

    }

    private void setUpRequests(String userID) {
        firebaseReference.child("Users").child(userID).child("requests").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.hasChildren()){
                            hasRequests=findViewById(R.id.has_requests2);
                            hasRequests.setVisibility(View.VISIBLE);
                        }
                        else{
                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                String requestID = dataSnapshot.getKey();
                                getRequestDetails(requestID);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void getRequestDetails(String requestID) {
        firebaseReference.child("Requests").child(requestID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Request request=snapshot.getValue(Request.class);
                String trainingID,paymentMethod;
                trainingID=request.getTrainingID();
                paymentMethod=request.getPaymentMethod();
                RequestModel requestModel=new RequestModel();
                requestModel.requestID=requestID;
                requestModel.paymentMethod=paymentMethod;
                getTrainingDetails(trainingID,requestModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTrainerDetails(String traineeID,RequestModel requestModel) {
        firebaseReference.child("Users").child(traineeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserTrainer trainer = snapshot.getValue(UserTrainer.class);
                String trainerName= trainer.getFirstName() + " "+trainer.getLastName();
                requestModel.otherUserName=trainerName;
                requestModel.trainerRate=trainer.getRating();
                requests.add(requestModel);
                showRows();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTrainingDetails(String trainingID, RequestModel requestModel) {
        firebaseReference.child("Trainings").child(trainingID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Training training=snapshot.getValue(Training.class);
                String trainingTitle,trainingDate,trainingTime,trainerID;
                trainingTitle=training.getTitle();
                trainingDate=training.getDate();
                trainingTime=training.getStartTraining()+"-"+training.getEndTraining();
                requestModel.trainingTitle=trainingTitle;
                requestModel.trainingDate=trainingDate;
                requestModel.trainingTime=trainingTime;
                trainerID= training.getTrainerId();
                requestModel.otherUserID=trainerID;
                getTrainerDetails(trainerID,requestModel);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showRows() {
        requests_trainee_RecyclerViewAdapter adapter = new requests_trainee_RecyclerViewAdapter(this,requests);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }
}