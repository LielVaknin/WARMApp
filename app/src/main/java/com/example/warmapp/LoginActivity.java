package com.example.warmapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout lEmail;
    private TextInputLayout lPassword;
    private TextInputEditText email;
    private TextInputEditText password;
    MaterialButton login;
    TextView signup;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email_input_edit_text);
        lEmail = findViewById(R.id.email_input_layout);
        password = findViewById(R.id.password_input_edit_text);
        lPassword = findViewById(R.id.password_input_layout);
        login = findViewById(R.id.button_login);
        signup = findViewById(R.id.sign_up_txt);

        auth = FirebaseAuth.getInstance();

        //user press login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (txt_email.isEmpty()){
                    lEmail.setError("Enter a email!");
                }else if (txt_password.isEmpty()){
                    lPassword.setError("Enter a password!");
                }else{
                    LoginUserAccount(txt_email,txt_password);
                }
            }
        });

        //go to sign up activity
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    //helper functions
    private void LoginUserAccount(String email, String password) {
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
//    //if user login already
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (auth.getCurrentUser() != null){
//            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    }
}