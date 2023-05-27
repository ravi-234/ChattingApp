package com.example.chateaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.hapticfeedback.HapticFeedback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.chateaseapp.adapter.TopStatusAdapter;
import com.example.chateaseapp.adapter.UsersAdapter;
import com.example.chateaseapp.databinding.ActivityMainBinding;
import com.example.chateaseapp.model.Status;
import com.example.chateaseapp.model.UserStatus;
import com.example.chateaseapp.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<Users> users;
    UsersAdapter usersAdapter;
    TopStatusAdapter topStatusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;
    Users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);
        binding.recyclerView.setAdapter(usersAdapter);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading image....");
        dialog.setCancelable(false);

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)   // recommended 3600 ( in seconds )
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                           // boolean updated = task.getResult();
                          // String toolbarcolor = mFirebaseRemoteConfig.getString("toolbarcolor");
                            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarcolor)));
                            String toolbarimage = mFirebaseRemoteConfig.getString("toolbarimage2");
                            //binding.imageView3.setVisibility(View.VISIBLE);
                            //Glide.with(MainActivity.this).load(toolbarimage).into(binding.imageView3);
                            Glide.with(MainActivity.this).load(toolbarimage).into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    getSupportActionBar().setBackgroundDrawable(resource);
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                        } else
                        {
                            Toast.makeText(MainActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                       // displayWelcomeMessage();
                    }

                });


        userStatuses = new ArrayList<>();
        database.getReference().child("users")  //For getting user for stories
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(Users.class);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference()
                .child("stories")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        UserStatus userStatus = new UserStatus();
                        userStatus.setName(dataSnapshot.child("username").getValue(String.class));
                        userStatus.setProfileImage(dataSnapshot.child("profilepic").getValue(String.class));
                        userStatus.setLastUpdated(dataSnapshot.child("lastupdated").getValue(long.class));

                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statussnapshot : dataSnapshot.child("statuses").getChildren())
                        {
                            Status status_sample = statussnapshot.getValue(Status.class);
                            statuses.add(status_sample);
                        }
                        userStatus.setStatuses(statuses);
                        userStatuses.add(userStatus);
                    }
                    topStatusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        topStatusAdapter = new TopStatusAdapter(this, userStatuses);
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //binding.statuslister.setLayoutManager(linearLayoutManager);
        binding.statuslister.setAdapter(topStatusAdapter);

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.Chats:
                        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.Status:
                        Intent intent2 = new Intent();
                        intent2.setType("image/*");
                        intent2.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent2 , 75);
                        break;

                    case R.id.settings:
                        Intent intent3=new Intent(MainActivity.this,UpdateProfileActivity.class);
                        startActivity(intent3);
                        break;
                }
                return true;
            }
        });

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    Users user = snapshot1.getValue(Users.class);
                    users.add(user);
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token" , token);
                        database.getReference()
                                .child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);
                       // Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            dialog.show();
            if(data.getData()!=null){
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference storageReference = storage.getReference().child("status").child(date.getTime() + "");
                storageReference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getUserName());
                                    userStatus.setProfileImage(user.getProfilePic());
                                    userStatus.setLastUpdated(date.getTime());

                                    HashMap<String ,Object> obj = new HashMap<>();
                                    obj.put("username" , userStatus.getName());
                                    obj.put("profilepic" , userStatus.getProfileImage());
                                    obj.put("lastupdated" , userStatus.getLastUpdated());

                                    String imageurl = uri.toString();
                                    Status status = new Status(imageurl , userStatus.getLastUpdated());

                                    database.getReference()
                                            .child("stories")
                                                    .child(FirebaseAuth.getInstance().getUid())
                                                            .updateChildren(obj);

                                    database.getReference()
                                                    .child("stories")
                                                            .child(FirebaseAuth.getInstance().getUid())
                                                                    .child("statuses")
                                                                        .push()
                                                                            .setValue(status);

                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        String currentid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentid).setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentid).setValue("offline");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.group:
               //Toast.makeText(this, "group clicked", Toast.LENGTH_SHORT).show();
               Intent intent1 = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(intent1);
                break;
            case R.id.logout:
               // String currentid = FirebaseAuth.getInstance().getUid();
               // database.getReference().child("presence").child(currentid).setValue("offline");
                try {
                    String currentid = FirebaseAuth.getInstance().getUid();
                    database.getReference().child("presence").child(currentid).setValue("offline");
                    Intent intent = new Intent(MainActivity.this, signup.class);
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override                                              //Three dots menu options
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu , menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"Closing app",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}