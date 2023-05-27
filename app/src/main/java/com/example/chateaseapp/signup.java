package com.example.chateaseapp;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chateaseapp.databinding.ActivitySignupBinding;
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

public class signup extends AppCompatActivity {

    ActivitySignupBinding binding;
    private FirebaseAuth mauth;
    private FirebaseDatabase database;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We are creating your Account");
        getSupportActionBar().hide();


        mauth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        database = FirebaseDatabase.getInstance();
        //button=findViewById(R.id.btn);

        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.signupUsername.getText().toString().isEmpty()) {
                    binding.signupUsername.setError("Fill username");
                    return;
                }
                if (binding.signupEmail.getText().toString().isEmpty()) {
                    binding.signupEmail.setError("Fill your email");
                    return;
                }
                if (binding.signupPassword.getText().toString().isEmpty()) {
                    binding.signupPassword.setError("Fill your password");
                    return;
                }
                if(binding.confirmPassword.getText().toString().isEmpty())
                {
                    binding.confirmPassword.setError("Reenter your password");
                    return;
                }

                if(!binding.signupPassword.getText().toString().equals(binding.confirmPassword.getText().toString()))
                {
                    binding.signupPassword.setError("Password doesn't match");
                    binding.confirmPassword.setError("Password doesn't match");
                    return;

                }
                progressDialog.show();
                mauth.createUserWithEmailAndPassword(binding.signupEmail.getText().toString(), binding.signupPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Users user = new Users(binding.signupUsername.getText().toString(), binding.signupEmail.getText().toString(), binding.signupPassword.getText().toString());
                            String id = task.getResult().getUser().getUid();
                            database.getReference().child("users").child(id).setValue(user);
                            Intent intent = new Intent(signup.this, SetupProfileActivity.class);
                            startActivity(intent);
                            Toast.makeText(signup.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(signup.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        binding.alreadyhaveaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signup.this, signin.class);
                startActivity(intent);
            }
        });
    }
}