package com.example.warmapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainee;
import com.example.warmapp.classes.UserTrainer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private TextInputEditText firstName;
    private TextInputEditText lastName;
    private TextInputEditText phone;
    private TextInputEditText email;
    private TextInputLayout lPassword;
    private TextInputEditText Password;
    private TextInputLayout lPasswordVerification;
    private TextInputEditText passwordVerification;
    TextView login;
    Button signUp;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_sign_up);

        radioGroup = findViewById(R.id.radioGroup);
        firstName = findViewById(R.id.edit_first_name);
        lastName = findViewById(R.id.edit_last_name);
        phone = findViewById(R.id.edit_phone);
        email = findViewById(R.id.edit_email);
        lPassword = findViewById(R.id.layout_password);
        Password = findViewById(R.id.edit_password);
        lPasswordVerification = findViewById(R.id.layout_password_verification);
        passwordVerification = findViewById(R.id.edit_password_verification);
        login = findViewById(R.id.login_txt);
        signUp = findViewById(R.id.button_sign_up);

        //firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //set strong password
        Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String Password = s.toString();
                if (Password.length() >= 8) {
                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher = pattern.matcher(Password);
                    boolean isPwContainsSpeChar = matcher.find();
                    if (isPwContainsSpeChar) {
                        lPassword.setHelperText("Strong Password");
                        lPassword.setError("");
                    } else {
                        lPassword.setHelperText("");
                        lPassword.setError("Weak Password Enter Minimum 1 Special Character");
                    }
                } else {
                    lPassword.setHelperText("Enter Minimum 8 Character");
                    lPassword.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //signup button
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioId = radioGroup.getCheckedRadioButtonId();
                String txt_first_Name = firstName.getText().toString();
                String txt_last_name = lastName.getText().toString();
                String txt_phone = phone.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = Password.getText().toString();
                String txt_password_verification = passwordVerification.getText().toString();

                if (txt_first_Name.isEmpty() || txt_last_name.isEmpty() || txt_phone.isEmpty() || radioId == -1 ||
                        txt_email.isEmpty() || txt_password.isEmpty() || txt_password_verification.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Not all fields are full", Toast.LENGTH_LONG).show();
                }  else if (!txt_password_verification.equals(txt_password)){
                    lPasswordVerification.setError("Password does not mach");
                } else {
                    radioButton = findViewById(radioId);
                    String selected_user = radioButton.getText().toString();
                    RegisterUserAccount(txt_first_Name, txt_last_name, txt_phone, txt_email, txt_password, selected_user);//auth signup
                }
            }
        });

        //back to login activity
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //helper functions
    private void RegisterUserAccount(String firstName, String lastName, String phone, String email, String pass, String userType) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userID = auth.getCurrentUser().getUid();
                            if (userType.equals("trainer")) {
                                UserTrainer user = new UserTrainer(firstName, lastName, email, pass, phone,userType);
                                databaseReference.child(userID).setValue(user);
                                databaseReference.child(userID).child("userType").setValue(userType);
                            } else {
                                UserTrainee user = new UserTrainee(firstName, lastName, email, pass, phone,userType);
                                databaseReference.child(userID).setValue(user);
                                databaseReference.child(userID).child("userType").setValue(userType);
                            }
                            Toast.makeText(SignUpActivity.this, "Registering User successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registering User failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}