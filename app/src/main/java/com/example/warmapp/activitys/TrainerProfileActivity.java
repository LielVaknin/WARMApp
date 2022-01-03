package com.example.warmapp.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.warmapp.R;
import com.example.warmapp.classes.Review;
import com.example.warmapp.adapters.TrainerProfileAdapter;
import com.example.warmapp.classes.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class TrainerProfileActivity extends AppCompatActivity {

    ImageView trainerImage;
    TextView trainerName, description, phone, mail;
    String userID;
    String userName;
    FirebaseAuth auth;
    RecyclerView reviewsRecyclerView;
    float ratingAv;
    TextView ratingAverageNumber;
    String trainerId;
    Bitmap bmp;
    ImageView imageViewBlur;

    //bottom sheet dialog
    BottomSheetDialog bottomSheetDialog;
    ImageView imageViewAddReview;
    ImageView imageViewCloseBottomSheetDialog;
    RatingBar rateTrainer;
    EditText reviewBox;
    MaterialButton postBtn;

    TrainerProfileAdapter myAdapter;
    ArrayList<Review> reviews;

    float myRating = 0;
    int countReviews = 0;
    float sumRatings = 0;
    String uniqueReviewID = "";
    boolean pressPostBtn = true;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_trainer_profile);
        auth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        trainerName = findViewById(R.id.name);
        description = findViewById(R.id.description);
//        phone = findViewById(R.id.phone_number);
//        mail = findViewById(R.id.mail);
        trainerImage = findViewById(R.id.activity_trainer_profile_image_trainer);
        reviewsRecyclerView = findViewById(R.id.reviews_recyclerView);
        ratingAverageNumber = findViewById(R.id.rating_average_number);
        imageViewAddReview = findViewById(R.id.trainer_profile_edit_image);
        imageViewBlur = findViewById(R.id.image_blur);


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
//        phone.setText(trainerPhone);
//        mail.setText(trainerMail);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference = storageReference.child(trainerId + ".jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                trainerImage.setImageBitmap(bmp);
                Bitmap final_Bitmap = BlurImage(bmp);
                imageViewBlur.setImageBitmap(final_Bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
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

        imageViewAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

//        FirebaseDatabase.getInstance().getReference().child("Users").child(trainerId).child("rating").setValue(sumRatings/countReviews); // lehorid rating meusertrainer class
    }


    private void showBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(TrainerProfileActivity.this, R.style.BottomSheetStyle);
        @SuppressLint("InflateParams") View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_add_review,null, false);

        initViewsBottomSheetDialog(bottomSheetView);

        imageViewCloseBottomSheetDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        rateTrainer.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int rate = (int) rating;
                myRating = ratingBar.getRating();
                String message = null;
                switch (rate) {
                    case 0:
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


        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pressPostBtn) {
                    bottomSheetView.findViewById(R.id.bottom_sheet_add_review_post_button).setEnabled(false);
                    Toast.makeText(TrainerProfileActivity.this, "You have already written a review", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                    return;
                }
                String getUniqueReviewID = FirebaseDatabase.getInstance().getReference("Reviews").push().getKey();
                Review review = new Review(myRating, reviewBox.getText().toString(), userID, userName);
                FirebaseDatabase.getInstance().getReference("Users").child(trainerId).child("reviewsMap").child(Objects.requireNonNull(getUniqueReviewID)).setValue(true);
                FirebaseDatabase.getInstance().getReference("Reviews").child(Objects.requireNonNull(getUniqueReviewID)).setValue(review);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
        //bottomSheetDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private Bitmap BlurImage (Bitmap input) {
        try {
            RenderScript  rsScript = RenderScript.create(getApplicationContext());
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,   Element.U8_4(rsScript));
            blur.setRadius(25f);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        }
        catch (Exception e) {
            return input;
        }

    }

    private void initViewsBottomSheetDialog(View bottomSheetView) {
        imageViewCloseBottomSheetDialog = bottomSheetView.findViewById(R.id.bottom_sheet_add_review_image_view_close);
        rateTrainer = bottomSheetView.findViewById(R.id.bottom_sheet_add_review_rating_bar_trainer);
        reviewBox = bottomSheetView.findViewById(R.id.bottom_sheet_add_review_text_review);
        postBtn = bottomSheetView.findViewById(R.id.bottom_sheet_add_review_post_button);
    }

    @SuppressLint("SetTextI18n")
    public void setAverageRating() {
        ratingAv = sumRatings / countReviews;
        float roundedOneDigit = (float) (Math.round(ratingAv * 10) / 10.0);
        ratingAverageNumber.setText(roundedOneDigit + "");
        FirebaseDatabase.getInstance().getReference("Users").child(trainerId).child("rating").setValue(roundedOneDigit);
    }

    public void setAdapter() {
        myAdapter = new TrainerProfileAdapter(this, reviews);
        reviewsRecyclerView.setAdapter(myAdapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}