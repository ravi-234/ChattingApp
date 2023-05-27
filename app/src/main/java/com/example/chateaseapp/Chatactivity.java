package com.example.chateaseapp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chateaseapp.adapter.MessageAdapter;
import com.example.chateaseapp.databinding.ActivityChatactivityBinding;
import com.example.chateaseapp.model.Message;
import com.example.chateaseapp.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
public class Chatactivity extends AppCompatActivity
{
    ArrayList<Message> message;
    MessageAdapter messageAdapter;
    ActivityChatactivityBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    private final int req_code=100;
    private final int request_code=200;

    public String lastoriginalmessage;
    String sendername;
    ProgressDialog dialog;
    String senderuid , receiveuid , senderRoom , recieveRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityChatactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Sending image....");

        String name = getIntent().getStringExtra("name");
        String Token = getIntent().getStringExtra("token");
        String profileimage = getIntent().getStringExtra("profilepic");
        senderuid = FirebaseAuth.getInstance().getUid();
        receiveuid = getIntent().getStringExtra("uid");

        senderRoom = senderuid + receiveuid;
        recieveRoom = receiveuid + senderuid;

        //Toast.makeText(this, Token, Toast.LENGTH_SHORT).show();

        // setting of remote configuration from firebase

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)   // recommended 3600 ( in seconds )
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()){
                    String toolbarimage = mFirebaseRemoteConfig.getString("toolbarimage");

                    binding.chatimage.setVisibility(View.VISIBLE);
                    Glide.with(Chatactivity.this).load(toolbarimage).into(binding.chatimage);
                }
            }
        });

        database.getReference().child("users")  //For getting user for stories
                .child(senderuid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                         Users user = snapshot.getValue(Users.class);
                         sendername = snapshot.child("userName").getValue(String.class);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference()
                .child("presence")
                .child(receiveuid)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()){
                        if(status.equals("offline")){
                            binding.statusonline.setVisibility(View.GONE);
                        }
                        else {
                            binding.statusonline.setText(status);
                            binding.statusonline.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getSupportActionBar().hide();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Glide.with(getApplicationContext()).load(profileimage).placeholder(R.drawable.avatar).into(binding.profileImage);
        binding.username.setText(name);
        //ActionbarChatDetail work ->End

        binding.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3=new Intent(getApplicationContext(),TopNameSelectChatActivity.class);
                intent3.putExtra("userid",receiveuid);
                intent3.putExtra("username",name);
                intent3.putExtra("profileimage",profileimage);
                startActivity(intent3);
            }
        });

        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent2);
                //finish();
            }
        });

        message = new ArrayList<>();
        messageAdapter = new MessageAdapter(this , message,senderRoom,recieveRoom);
        binding.recyclerView.setAdapter(messageAdapter);

        database.getReference()
                .child("chats")
                        .child(senderRoom)
                                .child("messages")
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                message.clear();
                                                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                                    Message message1 = snapshot1.getValue(Message.class);
                                                    message1.setMessageid(snapshot1.getKey());
                                                    message.add(message1);
                                                    //binding.scroll.fullScroll(ScrollView.FOCUS_DOWN);
                                                }
                                                messageAdapter.notifyDataSetChanged();
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

        binding.mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Need to Speak");
                try{
                    startActivityForResult(intent,req_code);

                }
                catch (ActivityNotFoundException e)
                {
                    Toast.makeText(getApplicationContext(),"Sorry,Your device not supporting",Toast.LENGTH_LONG).show();
                }

            }
        });

        binding.translation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent3=new Intent(getApplicationContext(),GoogleTranslator.class);
                startActivityForResult(intent3,request_code);
                Toast.makeText(Chatactivity.this, "Translation clicked", Toast.LENGTH_SHORT).show();
            }
        });

        binding.sendbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                String message_txt = binding.messagebox.getText().toString().replaceAll("\\s+$", "");
                Date date = new Date();
                KeyGenerator keyGenerator = null;
                try {
                    keyGenerator = KeyGenerator.getInstance("AES");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                SecureRandom secureRandom = new SecureRandom();
                keyGenerator.init(128, secureRandom);
                SecretKey secretKey = keyGenerator.generateKey();

                // Encrypt the message
                String messagetxt;

                try {
                    byte[] encryptedBytes = encrypt(message_txt, secretKey);
                    messagetxt = new String(encryptedBytes, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Message message1 = new Message(messagetxt, senderuid, date.getTime()); // message prepared for pushing in database
                message1.setOrgmessage(message_txt);
                binding.messagebox.setText(null);
                String randomKey = database.getReference().push().getKey();

                //binding.scroll.fullScroll(ScrollView.FOCUS_DOWN);
                HashMap<String, Object> lastmsgobj = new HashMap<>();
                lastmsgobj.put("lastmssg", message1.getOrgmessage());
                lastmsgobj.put("lastdate", date.getTime());

                lastoriginalmessage = message1.getMessage();
                //message1.setLastmessage(message1.getOrgmessage());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastmsgobj);
                database.getReference().child("chats").child(recieveRoom).updateChildren(lastmsgobj);
                //binding.scroll.fullScroll(ScrollView.FOCUS_DOWN);

                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message1).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference()
                                        .child("chats")
                                        .child(recieveRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                sendNotification(sendername, message1.getOrgmessage(), Token);
                                            }
                                        });
                            }
                        });
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                int request_code = 50;
                startActivityForResult(intent ,50);
            }
        });

        final Handler handler = new Handler();
        binding.messagebox.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderuid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }
            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderuid).setValue("online");
                }
            };
        });
    }

    public static byte[] encrypt(String message, SecretKey secretKey) throws Exception {
        // Create a cipher object and initialize it in encryption mode
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Convert the message to a byte array and encrypt it
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        return cipher.doFinal(messageBytes);
    }

    void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to",token);
            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Chatactivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "Key=AAAAMeQ-EXc:APA91bEXcdgSrr25-Xw0RZVeLOGMZJKg_dOqbFhAJ6ahloRUTBmjBtns01R2XabVSbI6ZGcy9UYMtTfuif8GaGTmUuSpKsPGHidq4DZfVeq1mpDDBa1aGGQLsX9mLlnwxbb1OSbKajmV";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);
                    return map;
                }
            };

            queue.add(request);
        } catch (Exception ex) {

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case req_code:
                if(resultCode==RESULT_OK&&data!=null)
                {
                    ArrayList result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    binding.messagebox.setText((String)result.get(0));
                }
                break;
            case request_code:
                if(resultCode==RESULT_OK&&data!=null)
                {
                    String translatedmsg=data.getStringExtra("translatedmsg");
                    binding.messagebox.setText(translatedmsg);

                }
                break;

            case 50:
                if(data!=null){
                    if (data.getData()!=null){
                        Calendar calendar = Calendar.getInstance();
                        StorageReference storageReference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");

                        dialog.show();
                        storageReference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String filepath = uri.toString();
                                            String messagetxt = binding.messagebox.getText().toString();
                                            Date date = new Date();
                                            Message message1 = new Message(messagetxt ,senderuid ,date.getTime()); // message prepared for pushing in database

                                            message1.setImageUrl(filepath);
                                            message1.setMessage("photo");
                                            binding.messagebox.setText(null);

                                            String randomKey=database.getReference().push().getKey();


                                            HashMap<String ,Object> lastmsgobj = new HashMap<>();
                                            lastmsgobj.put("lastmssg" , message1.getMessage());
                                            lastmsgobj.put("lastdate" , date.getTime());

                                            database.getReference().child("chats").child(senderRoom).updateChildren(lastmsgobj);
                                            database.getReference().child("chats").child(recieveRoom).updateChildren(lastmsgobj);

                                            database.getReference()
                                                    .child("chats")
                                                    .child(senderRoom)
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(message1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            database.getReference()
                                                                    .child("chats")
                                                                    .child(recieveRoom)
                                                                    .child("messages")
                                                                    .child(randomKey)
                                                                    .setValue(message1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {

                                                                            dialog.dismiss();

                                                                        }

                                                                    });


                                                          // Toast.makeText(Chatactivity.this, filepath, Toast.LENGTH_SHORT).show();
                                                        }

                                                    });

                                        }
                                    });
                                }
                            }
                        });
                    }
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
}