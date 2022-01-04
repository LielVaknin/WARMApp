package com.example.warmapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.warmapp.R;
import com.example.warmapp.adapters.DynamicTrainingsAdapter;
import com.example.warmapp.adapters.TypesTrainingsAdapter;
import com.example.warmapp.adapters.UpdateRVTrainingsByType;
import com.example.warmapp.adapters.MyTrainingsAdapter;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.example.warmapp.classes.TypesTrainings;
import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
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
    private ArrayList<TrainingModel> userTrainings;
    private ArrayList<TypesTrainings> types;
    private String[] titles;
    private ProgressBar progressBarMyTrainings;

    //firebase
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
    private User user;
    private Bitmap userImage;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_home);

        initViews();
        getIntents();
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
        progressBarMyTrainings = findViewById(R.id.home_activity_your_trainings_progress_bar);
    }

    @SuppressLint("SetTextI18n")
    private void getIntents() {
        byte[] byteArray = getIntent().getByteArrayExtra("userImage");
        if(byteArray!=null) {
            userImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            if (userImage != null) {
                circularUserImageView.setImageBitmap(userImage);
            }
        }

        userName = getIntent().getStringExtra("firstName");
        textViewName.setText("Hi " + userName + "!");
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
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_search:
                        intent = new Intent(HomeActivity.this, SearchActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(HomeActivity.this, CalendarActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        intent = new Intent(HomeActivity.this, RequestsActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_home:
                        intent = new Intent(HomeActivity.this, HomeActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    private void sendToIntent(Intent intent) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(userImage!=null){
            userImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("firstName", userName);
            intent.putExtra("userImage", byteArray);
        }

    }

    private void updateUserDetails() {
        progressBarMyTrainings.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("Users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        if(snapshot.hasChild("trainings")){
                            showUserTrainings();
                        }
                        else{
                            progressBarMyTrainings.setVisibility(View.GONE);
                        }
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

        userTrainings = new ArrayList<>();
        myTrainingsAdapter = new MyTrainingsAdapter(this, userTrainings, user.getUserType(), userID);
        recyclerViewYourTrainings.setAdapter(myTrainingsAdapter);

        //over trainings user and get the training from database
        int countTrainings = 0;
        if (user.getTrainings() != null) {
            for (String training : user.getTrainings().keySet()) {
                countTrainings++;
                int finalCountTrainings = countTrainings;
                FirebaseDatabase.getInstance().getReference("Trainings").child(training)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Training training = snapshot.getValue(Training.class);
                                getTrainerName(Objects.requireNonNull(training),finalCountTrainings);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }
    }

    private void getTrainerName(Training training, int finalCountTrainings) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(training.getTrainerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserTrainer userTrainer = snapshot.getValue(UserTrainer.class);
                        setTrainingModel(training,userTrainer,finalCountTrainings);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setTrainingModel(Training training, UserTrainer userTrainer, int finalCountTrainings) {
        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference().child(training.getTrainerId() + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(byte[] bytes) {
                        String trainerName = userTrainer.getFirstName() + " " + userTrainer.getLastName();
                        Bitmap trainerImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        TrainingModel trainingModel = new TrainingModel(training,training.getTrainingID(),trainerName,trainerImage,"");
                        userTrainings.add(trainingModel);
                        if (finalCountTrainings == user.getTrainings().size()){
                            progressBarMyTrainings.setVisibility(View.GONE);
                            myTrainingsAdapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    private void initTypesList() {
        types = new ArrayList<>();
        titles = getResources().getStringArray(R.array.Titles);
        int imageType;
        for (int i = 0; i < titles.length; i++){
            imageType = this.getResources().getIdentifier("sport_" + i, "drawable", this.getPackageName());
            types.add(new TypesTrainings(imageType, titles[i]));
        }

        typesTrainingsAdapter = new TypesTrainingsAdapter(this,types,this);
        recyclerViewTypeTrainings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTypeTrainings.setAdapter(typesTrainingsAdapter);

        ArrayList<TrainingModel> trainingsType = new ArrayList<>();

        dynamicTrainingsAdapter = new DynamicTrainingsAdapter(this,trainingsType);
        recyclerViewShowTypeTrainings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewShowTypeTrainings.setAdapter(dynamicTrainingsAdapter);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void callback(int position, ArrayList<TrainingModel> trainings) {
        dynamicTrainingsAdapter = new DynamicTrainingsAdapter(this,trainings);
        dynamicTrainingsAdapter.notifyDataSetChanged();
        recyclerViewShowTypeTrainings.setAdapter(dynamicTrainingsAdapter);
    }
}