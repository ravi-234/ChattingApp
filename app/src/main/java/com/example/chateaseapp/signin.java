package com.example.chateaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.chateaseapp.databinding.ActivitySigninBinding;
import com.example.chateaseapp.model.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class signin extends AppCompatActivity {


    ActivitySigninBinding binding;
    private FirebaseAuth mauth;
    private FirebaseDatabase database;
    ProgressDialog progressDialog;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mauth.getCurrentUser();
        updateUII(currentUser);
    }
    // [END on_start_check_user]
    private void updateUII(FirebaseUser user)
    {
        if(user!=null)
        {
            Intent intent=new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Log in");
        progressDialog.setMessage("Logging in your Account");


        // Initialize Firebase Auth
        mauth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        database=FirebaseDatabase.getInstance();


        binding.btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.signinEmail.getText().toString().isEmpty())
                {
                    binding.signinEmail.setError("Enter your email");
                    return;
                }

                if(binding.signinPassword.getText().toString().isEmpty())
                {
                    binding.signinPassword.setError("Enter your password");
                    return;

                }
                progressDialog.show();
                mauth.signInWithEmailAndPassword(binding.signinEmail.getText().toString(),binding.signinPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            if(mauth.getCurrentUser()!=null)
                            {
                                Intent intent = new Intent(signin.this , MainActivity.class);
                                startActivity(intent);
                            }
                        }
                        else {
                            Toast.makeText(signin.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        binding.clickSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signin.this, signup.class);
                startActivity(intent);
            }
        });

        if(mauth.getCurrentUser()!=null)
        {
            Intent intent = new Intent(signin.this , SetupProfileActivity.class);
            startActivity(intent);
        }


        binding.forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=binding.signinEmail.getText().toString();
                if(email.isEmpty())
                {
                    binding.signinEmail.setError("Fill your email");
                    return;
                }
                mauth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"Check your email",Toast.LENGTH_SHORT).show();

                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Error:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

    }


}

