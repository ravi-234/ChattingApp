package com.example.chateaseapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chateaseapp.Chatactivity;
import com.example.chateaseapp.R;
import com.example.chateaseapp.databinding.RowConversationBinding;
import com.example.chateaseapp.model.Message;
import com.example.chateaseapp.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    ArrayList<Users> users;

    public UsersAdapter(Context context, ArrayList<Users> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        Users user = users.get(position);
        holder.binding.username.setText(user.getUserName());
        Glide.with(context).load(user.getProfilePic()).placeholder(R.drawable.avatar).into(holder.binding.profileimg);

        String senderid = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderid + user.getUserId();

        //Message message = new Message();
        FirebaseDatabase.getInstance()
                .getReference()
                .child("chats")
                .child(senderRoom).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String lastmssg = snapshot.child("lastmssg").getValue(String.class);
                            Long lastDate = snapshot.child("lastdate").getValue(Long.class);

                           SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            //String formattedDate = dateFormat.format(new Date(lastDate));
                            holder.binding.lastmessage.setText(lastmssg);
                            holder.binding.msgtime.setText(dateFormat.format(new Date(lastDate)));
                        }
                        else {
                            holder.binding.lastmessage.setText("tap to chat");
                            holder.binding.msgtime.setText(null);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , Chatactivity.class);
                intent.putExtra("name" , user.getUserName());
                intent.putExtra("uid" , user.getUserId());
                intent.putExtra("profilepic" , user.getProfilePic());
                intent.putExtra("token" , user.getToken());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{
        RowConversationBinding binding;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding =RowConversationBinding.bind(itemView);
        }
    }

}
