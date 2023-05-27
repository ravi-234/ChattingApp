package com.example.chateaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.chateaseapp.databinding.ActivityTopNameSelectChatBinding;
import com.example.chateaseapp.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class TopNameSelectChatActivity extends AppCompatActivity {
    ActivityTopNameSelectChatBinding binding;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityTopNameSelectChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        database=FirebaseDatabase.getInstance();
        String userid=getIntent().getStringExtra("userid");
        String image=getIntent().getStringExtra("profileimage");


        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)   // recommended 3600 ( in seconds )
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        Glide.with(getApplicationContext()).load(image).placeholder(R.drawable.avatar).into(binding.profileImage);
        database.getReference().child("users").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user=snapshot.getValue(Users.class);
                binding.emailtextbox.setText(user.getMail());
                binding.statustextbox.setText(user.getAbout());
                binding.nametextbox.setText(user.getUserName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TopNameSelectChatActivity.this,Chatactivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}