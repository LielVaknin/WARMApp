package com.example.warmapp.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.warmapp.R;
import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AccountActivity extends AppCompatActivity {
    FirebaseAuth auth;
    String userType ;
    String userID;
    Button logOut;
    DatabaseReference databaseReference,userReference;
    TextView firstName,lastName,email,changePassword,phone,description,descriptionTitle;
    MaterialCardView firstNameCard,lastNameCard,emailCard,changePasswordCard,phoneCard,descriptionCard;
    ImageView iconDescription;
    String descriptionText;
    FloatingActionButton changePhoto;
    ImageView profilePhoto;
    ActivityResultLauncher<Intent> launchCameraActivity;
    ActivityResultLauncher<Intent> launchSelectGalleryActivity;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap userImage;
    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_account);
        getIntents();
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
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(AccountActivity.this, CalendarActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        intent = new Intent(AccountActivity.this, RequestsActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_home:
                        intent = new Intent(AccountActivity.this, HomeActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;



            }
        });
        databaseReference= FirebaseDatabase.getInstance().getReference();

        auth= FirebaseAuth.getInstance();
        userID= auth.getCurrentUser().getUid();
        userReference=databaseReference.child("Users").child(userID);
        firstName=findViewById(R.id.activity_account_first_name_arrow);
        firstNameCard=findViewById(R.id.activity_account_first_name_material_card_view);
        lastName=findViewById(R.id.activity_account_last_name_arrow);
        lastNameCard=findViewById(R.id.activity_account_last_name_material_card_view);
        email=findViewById(R.id.activity_account_mail_arrow);
        emailCard=findViewById(R.id.activity_account_mail_material_card_view);
        description=findViewById(R.id.activity_account_description_arrow);
        descriptionCard=findViewById(R.id.activity_account_description_material_card_view);
        descriptionTitle=findViewById(R.id.activity_account_text_view_description);
        iconDescription=findViewById(R.id.activity_account_image_view_description);
        phone=findViewById(R.id.activity_account_phone_arrow);
        phoneCard = findViewById(R.id.activity_account_phone_material_card_view);
        changePasswordCard= findViewById(R.id.activity_account_password_material_card_view);
        changePhoto= findViewById(R.id.activity_account_change_photo);
        profilePhoto=findViewById(R.id.activity_account_profile_photo);
        if(userImage!=null) {
            profilePhoto.setImageBitmap(userImage);
        }
        getPersonalDetails();
        launchCameraActivity= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Uri uri;
                            Intent data = result.getData();
                            Bundle extras = data.getExtras();
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path = MediaStore.Images.Media.insertImage(AccountActivity.this.getContentResolver(), imageBitmap, "Title", null);
                            uri= Uri.parse(path);
                            profilePhoto.setImageBitmap(imageBitmap);
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                            storageReference.child(userID+".jpg").putFile(uri);

                            // your operation....
                        }
                    }
                });
        launchSelectGalleryActivity= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Uri uri;
                            Intent data = result.getData();
                            //Bundle extras = data.getExtras();
                            uri=data.getData();
                            Bitmap imageBitmap = null;
                            try {
                                imageBitmap = MediaStore.Images.Media.getBitmap(AccountActivity.this.getContentResolver(), uri);
                                profilePhoto.setImageBitmap(imageBitmap);
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                storageReference.child(userID+".jpg").putFile(uri);
                            } catch (IOException e) {
                                Toast.makeText(AccountActivity.this,"Can't Upload Image",Toast.LENGTH_SHORT).show();
                                //e.printStackTrace();
                            }
                            /*ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path = MediaStore.Images.Media.insertImage(AccountActivity.this.getContentResolver(), imageBitmap, "Title", null);
                            uri= Uri.parse(path);
                               */

                            // your operation....
                        }
                    }
                });
        logOut=findViewById(R.id.activity_account_button_log_out);
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

    @SuppressLint("SetTextI18n")
    private void getIntents() {
        byte[] byteArray = getIntent().getByteArrayExtra("userImage");
        if(byteArray!=null) {
            userImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        }
        userName = getIntent().getStringExtra("firstName");
    }

    private void sendToIntent(Intent intent) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(userImage!=null) {
            userImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("userImage",byteArray);
        }

        intent.putExtra("firstName", userName);

    }

    private void changeProfilePhoto(){
        changePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                PopupMenu changeProfileMenu= new PopupMenu(AccountActivity.this,v);
                changeProfileMenu.inflate(R.menu.add_profile_photo_menu);
                changeProfileMenu.show();
                changeProfileMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.account_menu_camera:
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                launchCameraActivity.launch(takePictureIntent);
                                return true;
                            case R.id.account_menu_upload:
                                Intent selectPicture = new Intent();
                                selectPicture.setType("image/*");
                                selectPicture.setAction(Intent.ACTION_PICK);
                                launchSelectGalleryActivity.launch(selectPicture);


                        }
                        return false;

                    }
                });

            }
        });
    }

    private void getPersonalDetails() {

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userType=user.getUserType();
                firstName.setText(user.getFirstName());
                lastName.setText(user.getLastName());
                email.setText(user.getMail());
                phone.setText(user.getPhone());
                if(userType.equals("trainer")){
                    descriptionText=snapshot.getValue(UserTrainer.class).getDescription();
                    //description.setText(descriptionText);
                    descriptionCard.setVisibility(View.VISIBLE);
                }
//                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
//                StorageReference photoReference=storageReference.child(userID+".jpg");
//                final long ONE_MEGABYTE = 1024 * 1024;
//                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                        profilePhoto.setImageBitmap(bmp);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//
//                    }
//                });


                changeProfilePhoto();
                setButtonsListeners();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setButtonsListeners() {
        firstNameCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView =
                        LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_change_personal_details, null);
                TextView title = dialogView.findViewById(R.id.change_details_title);
                title.setText("Change Your First Name");
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dialogView);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Save", null);
                AlertDialog alert = builder.create();
                alert.show();

                TextInputLayout changeDetails = dialogView.findViewById(R.id.change_details);
                TextInputEditText newDetails=dialogView.findViewById(R.id.change_details_edit);
                newDetails.setText(firstName.getText().toString());
                Button positiveButton =alert.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(newDetails.getText().toString().equals("")){
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"No Changes Detected",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String newFirstName =newDetails.getText().toString();
                            userReference.child("firstName").setValue(newDetails.getText().toString());
                            firstName.setText(newFirstName);
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"Your Changes Saved",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        lastNameCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView =
                        LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_change_personal_details, null);
                TextView title = dialogView.findViewById(R.id.change_details_title);
                title.setText("Change Your Last Name");
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dialogView);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Save", null);
                AlertDialog alert = builder.create();
                alert.show();

                TextInputEditText newDetails=dialogView.findViewById(R.id.change_details_edit);
                newDetails.setText(lastName.getText().toString());
                Button positiveButton =alert.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(newDetails.getText().toString().equals("")){
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"No Changes Detected",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String newLastName =newDetails.getText().toString();
                            userReference.child("lastName").setValue(newDetails.getText().toString());
                            lastName.setText(newLastName);
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"Your Changes Saved",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        emailCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView =
                        LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_change_personal_details, null);
                TextView title = dialogView.findViewById(R.id.change_details_title);
                title.setText("Change Your Email Address");
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dialogView);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Save", null);
                AlertDialog alert = builder.create();
                alert.show();

                TextInputEditText newDetails=dialogView.findViewById(R.id.change_details_edit);
                newDetails.setText(email.getText().toString());
                Button positiveButton =alert.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(newDetails.getText().toString().equals("")){
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"No Changes Detected",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String newEmail =newDetails.getText().toString();
                                auth.getCurrentUser().updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            userReference.child("mail").setValue(newDetails.getText().toString());
                                            userReference.child("mail").setValue(newDetails.getText().toString());
                                            email.setText(newEmail);
                                            alert.dismiss();
                                            Toast.makeText(AccountActivity.this,"Your Changes Saved",Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            if(task.getException()!=null){
                                                if (task.getException().getClass()==FirebaseAuthInvalidCredentialsException.class){
                                                    Toast.makeText(AccountActivity.this,"Invalid Email Address",Toast.LENGTH_SHORT).show();
                                                }else if(task.getException().getClass()==FirebaseAuthUserCollisionException.class){
                                                    Toast.makeText(AccountActivity.this,"Email Address Is Already Taken",Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                        }
                                    }
                                });


                        }
                    }
                });
            }
        });

        phoneCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView =
                        LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_change_personal_details, null);
                TextView title = dialogView.findViewById(R.id.change_details_title);
                title.setText("Change Your Phone Number");
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dialogView);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Save", null);
                AlertDialog alert = builder.create();
                alert.show();

                TextInputLayout changeDetails = dialogView.findViewById(R.id.change_details);
                TextInputEditText newDetails=dialogView.findViewById(R.id.change_details_edit);
                newDetails.setText(phone.getText().toString());
                Button positiveButton =alert.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(newDetails.getText().toString().equals("")){
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"No Changes Detected",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String newPhone =newDetails.getText().toString();
                            userReference.child("phone").setValue(newDetails.getText().toString());
                            phone.setText(newPhone);
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"Your Changes Saved",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        descriptionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView =
                        LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_change_personal_details, null);
                TextView title = dialogView.findViewById(R.id.change_details_title);
                title.setText("Change Your Description");
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(dialogView);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Save", null);
                AlertDialog alert = builder.create();
                alert.show();

                TextInputLayout changeDetails = dialogView.findViewById(R.id.change_details);
                TextInputEditText newDetails=dialogView.findViewById(R.id.change_details_edit);
                newDetails.setText(descriptionText);
                Button positiveButton =alert.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(newDetails.getText().toString().equals("")){
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"No Changes Detected",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String newDescription =newDetails.getText().toString();
                            userReference.child("description").setValue(newDetails.getText().toString());
                            //description.setText(newDescription);
                            alert.dismiss();
                            Toast.makeText(AccountActivity.this,"Your Changes Saved",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        setChangePasswordListener();
    }

    private void setChangePasswordListener() {
        changePasswordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView =
                        LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_change_password_dialog, null);
                TextView title = dialogView.findViewById(R.id.change_password_title);
                title.setText("Change Your Password");
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dialogView);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Change", null);
                AlertDialog alert = builder.create();
                alert.show();
                TextInputEditText currentPassword =dialogView.findViewById(R.id.current_password_edit);
                TextInputEditText newPassword =dialogView.findViewById(R.id.new_password_edit);
                TextInputEditText newPasswordVerification =dialogView.findViewById(R.id.new_password_verification_edit);
                Button positiveButton =alert.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String txt_current_password = currentPassword.getText().toString();
                        String txt_password = newPassword.getText().toString();
                        String txt_password_verification = newPasswordVerification.getText().toString();
                        if(txt_current_password.isEmpty()){
                            currentPassword.setError("Field is required");
                        }
                        if(txt_password.isEmpty()){
                                newPassword.setError("Field is required");
                        }
                        if(txt_password_verification.isEmpty()){
                                newPasswordVerification.setError("Field is required");
                        }else if (!txt_password_verification.equals(txt_password)){
                            newPasswordVerification.setError("Password does not mach");
                        }
                        else if (!txt_current_password.isEmpty() || !txt_password.isEmpty() ){
                            auth.signInWithEmailAndPassword(auth.getCurrentUser().getEmail(), txt_current_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        currentPassword.setError("Current password is wrong");
                                    } else {
                                        auth.getCurrentUser().updatePassword(txt_password);
                                        userReference.child("password").setValue(txt_password);
                                        alert.dismiss();
                                        Toast.makeText(AccountActivity.this, "Your password was changed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });

            }
        });
    }


}