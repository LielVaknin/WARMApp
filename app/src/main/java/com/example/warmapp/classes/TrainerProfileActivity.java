package com.example.warmapp.classes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.warmapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

public class TrainerProfileActivity extends AppCompatActivity {

    ImageView trainerImage;
    TextView trainerName, description, phone, mail;
    Button postBtn;
    RatingBar rateTrainer;
    EditText reviewBox;
    String userID;
    String userName;
    FirebaseAuth auth;
    RecyclerView reviewsRecyclerView;
    float ratingAv;
    RatingBar ratingAverage;
    TextView ratingAverageNumber;
    String trainerId;

    TrainerProfileAdapter myAdapter;
    ArrayList<Review> reviews;

    float myRating = 0;
    int countReviews = 0;
    float sumRatings = 0;
    String uniqueReviewID = "";
    boolean pressPostBtn = true;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);
        auth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        postBtn = findViewById(R.id.post_btn);
        rateTrainer = findViewById(R.id.rate_trainer);
        reviewBox = findViewById(R.id.search_trainer_edtxt);
        trainerName = findViewById(R.id.name);
        description = findViewById(R.id.description);
        phone = findViewById(R.id.phone_number);
        mail = findViewById(R.id.mail);
        trainerImage = findViewById(R.id.trainer_image);
        reviewsRecyclerView = findViewById(R.id.reviews_recyclerView);
        ratingAverage = findViewById(R.id.rating_average);
        ratingAverageNumber = findViewById(R.id.rating_average_number);

        reviews = new ArrayList<>();

        Intent intent = getIntent();
        trainerId = intent.getStringExtra("trainerId");
        String fName = intent.getStringExtra("firstName");
        String lName = intent.getStringExtra("lastName");
        String trainerDescription = intent.getStringExtra("description");
        String trainerPhone = intent.getStringExtra("phone");
        String trainerMail = intent.getStringExtra("mail");

        trainerName.setText(fName + " " + lName);
        description.setText(trainerDescription);
        phone.setText(trainerPhone);
        mail.setText(trainerMail);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference = storageReference.child(trainerId + ".jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                trainerImage.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
            }
        });


        rateTrainer.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int rate = (int) rating;
                myRating = ratingBar.getRating();
                String message = null;
                switch (rate) {
                    case 1:
                        message = "Sorry to hear that! :(";
                        break;
                    case 2:
                        message = "You always accept suggestions!";
                        break;
                    case 3:
                        message = "Good enough";
                        break;
                    case 4:
                        message = "Great! Thank you!";
                        break;
                    case 5:
                        message = "Awesome! You are the best!";
                        break;
                }

                Toast.makeText(TrainerProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = Objects.requireNonNull(snapshot.getValue(User.class)).getFirstName() + " " + Objects.requireNonNull(snapshot.getValue(User.class)).getLastName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(trainerId).child("reviewsMap").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                for (DataSnapshot dataSnapshot : snapshot1.getChildren()) {
                    uniqueReviewID = dataSnapshot.getKey();
                    FirebaseDatabase.getInstance().getReference().child("Reviews").child(Objects.requireNonNull(uniqueReviewID))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Review review = snapshot.getValue(Review.class);
                                    assert review != null;
                                    if (review.getTraineeId().equals(userID)) {
                                        pressPostBtn = false;
                                    }
                                    sumRatings += review.getRating();
                                    countReviews++;
                                    reviews.add(review);
                                    setAdapter();
                                    if (countReviews == snapshot1.getChildrenCount()) {
                                        setAverageRating();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pressPostBtn) {
                    postBtn.setEnabled(false);
                    Toast.makeText(TrainerProfileActivity.this, "You have already written a review", Toast.LENGTH_SHORT).show();
                    return;
                }
                String getUniqueReviewID = FirebaseDatabase.getInstance().getReference("Reviews").push().getKey();
                Review review = new Review(myRating, reviewBox.getText().toString(), userID, userName);
                FirebaseDatabase.getInstance().getReference("Users").child(trainerId).child("reviewsMap").child(Objects.requireNonNull(getUniqueReviewID)).setValue(true);
                FirebaseDatabase.getInstance().getReference("Reviews").child(Objects.requireNonNull(getUniqueReviewID)).setValue(review);
            }
        });


        ratingAverage.setEnabled(false);
//        FirebaseDatabase.getInstance().getReference().child("Users").child(trainerId).child("rating").setValue(sumRatings/countReviews); // lehorid rating meusertrainer class
    }

    @SuppressLint("SetTextI18n")
    public void setAverageRating() {
        ratingAv = sumRatings / countReviews;
        float roundedOneDigit = (float) (Math.round(ratingAv * 10) / 10.0);
        ratingAverage.setRating(roundedOneDigit);
        ratingAverageNumber.setText(roundedOneDigit + "");
        FirebaseDatabase.getInstance().getReference("Users").child(trainerId).child("rating").setValue(roundedOneDigit);
    }

    public void setAdapter() {
        myAdapter = new TrainerProfileAdapter(this, reviews);
        reviewsRecyclerView.setAdapter(myAdapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.addItemDecoration(new DividerItemDecoration(reviewsRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }
}