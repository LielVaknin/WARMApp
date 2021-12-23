package com.example.warmapp.trainerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.warmapp.HomeActivity;
import com.example.warmapp.R;
import com.example.warmapp.AccountActivity;
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.RequestModel;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainee;
import com.example.warmapp.classes.UserTrainer;
import com.example.warmapp.traineeActivities.SearchActivity;
import com.example.warmapp.traineeActivities.requests_trainee_RecyclerViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RequestsActivity extends AppCompatActivity {
    ArrayList<RequestModel>requests;
    DatabaseReference firebaseReference;
    RecyclerView recyclerView;
    TextView hasRequests;
    String userID;
    String userType;
    FirebaseAuth auth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_requests);
        recyclerView=findViewById(R.id.trainer_requests_list);
        progressBar =findViewById(R.id.requests_progress_bar);
        requests=new ArrayList<>();
        auth= FirebaseAuth.getInstance();
        userID=auth.getCurrentUser().getUid();
        BottomNavigationView bottomNavigationView =findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_requests);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.menu_profile:
                        intent = new Intent(RequestsActivity.this, AccountActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_search:
                        intent = new Intent(RequestsActivity.this, SearchActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(RequestsActivity.this, CalendarActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        return true;
                    case R.id.menu_home:
                        intent = new Intent(RequestsActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;



            }
        });

        firebaseReference=FirebaseDatabase.getInstance().getReference();
        getUserType();


    }

    private void getUserType() {
        firebaseReference.child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user =  snapshot.getValue(User.class);
                userType=user.getUserType();
                if(!snapshot.hasChild("requests")){
                    hasRequests=findViewById(R.id.has_requests1);
                    hasRequests.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                else{
                    setUpRequests();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUpRequests() {
        firebaseReference.child("Users").child(userID).child("requests").
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChildren()){
                    /*hasRequests=findViewById(R.id.has_requests1);
                    hasRequests.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);*/
                }
                else {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String requestID = dataSnapshot.getKey();
                        if(userType.equals("trainer")){
                            getRequestDetailsTrainer(requestID);
                        }
                        else{
                            getRequestDetailsTrainee(requestID);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void getRequestDetailsTrainer(String requestID) {
        firebaseReference.child("Requests").child(requestID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Request request=snapshot.getValue(Request.class);
                String traineeID,trainingID,paymentMethod;
                traineeID=request.getTraineeID();
                trainingID=request.getTrainingID();
                paymentMethod=request.getPaymentMethod();
                RequestModel requestModel=new RequestModel();
                requestModel.requestID=requestID;
                requestModel.paymentMethod=paymentMethod;
                getTraineeDetails(traineeID,trainingID,requestModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRequestDetailsTrainee(String requestID) {
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
                getTrainingDetailsTrainee(trainingID,requestModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTraineeDetails(String traineeID, String trainingID,RequestModel requestModel) {
        firebaseReference.child("Users").child(traineeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserTrainee trainee = snapshot.getValue(UserTrainee.class);
                String traineeName= trainee.getFirstName() + " "+trainee.getLastName();
                requestModel.otherUserID=traineeID;
                requestModel.otherUserName=traineeName;
                requestModel.otherUserPhone=trainee.getPhone();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference photoReference=storageReference.child(traineeID+".jpg");
                final long ONE_MEGABYTE = 1024 * 1024;
                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        requestModel.otherUserPhoto=bmp;
                        getTrainingDetailsTrainer(trainingID,requestModel);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        getTrainingDetailsTrainer(trainingID,requestModel);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTrainerDetails(String trainerID,RequestModel requestModel) {
        firebaseReference.child("Users").child(trainerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserTrainer trainer = snapshot.getValue(UserTrainer.class);
                String trainerName= trainer.getFirstName() + " "+trainer.getLastName();
                requestModel.otherUserName=trainerName;
                requestModel.trainerRate=trainer.getRating();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference photoReference=storageReference.child(trainerID+".jpg");
                final long ONE_MEGABYTE = 1024 * 1024;
                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        requestModel.otherUserPhoto=bmp;
                        requests.add(requestModel);
                        showRows();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        requests.add(requestModel);
                        showRows();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTrainingDetailsTrainer(String trainingID, RequestModel requestModel) {
        firebaseReference.child("Trainings").child(trainingID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Training training=snapshot.getValue(Training.class);
                requestModel.training=training;
                requests.add(requestModel);
                showRows();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getTrainingDetailsTrainee(String trainingID, RequestModel requestModel) {
        firebaseReference.child("Trainings").child(trainingID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Training training=snapshot.getValue(Training.class);
                String trainerID;
                requestModel.training=training;
                trainerID= training.getTrainerId();
                getTrainerDetails(trainerID,requestModel);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showRows() {
        if(userType.equals("trainer")){
            requests_trainer_RecyclerViewAdapter adapter = new requests_trainer_RecyclerViewAdapter(this,requests);
            recyclerView.setAdapter(adapter);
        }
        else{
            requests_trainee_RecyclerViewAdapter adapter = new requests_trainee_RecyclerViewAdapter(this,requests);
            recyclerView.setAdapter(adapter);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        progressBar.setVisibility(View.GONE);


    }
}