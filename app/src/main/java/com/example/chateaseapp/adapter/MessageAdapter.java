package com.example.chateaseapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chateaseapp.R;
import com.example.chateaseapp.databinding.DeleteDialogBinding;
import com.example.chateaseapp.databinding.ItemReceiveBinding;
import com.example.chateaseapp.databinding.ItemSentBinding;
import com.example.chateaseapp.model.Message;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Message> messages;
    String senderroom,receiverroom;

    final int ITEM_SENT =1;
    final int ITEM_RECIEVE=2;
    FirebaseRemoteConfig remoteConfig;

    public MessageAdapter(Context context, ArrayList<Message> messages, String senderroom, String receiverroom) {
        this.context = context;
        this.messages = messages;
        this.senderroom = senderroom;
        this.receiverroom = receiverroom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent,parent,false);
            return new SentViewholder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive , parent ,false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        int reactions[]=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();
        ReactionPopup popup = new ReactionPopup(context, config, (positionn) -> {
            if (positionn<0){
                return false;
            }
           if(holder.getClass()==SentViewholder.class)
            {
                SentViewholder sentViewholder = (SentViewholder) holder;
                sentViewholder.binding.feeling.setImageResource(reactions[positionn]);
                sentViewholder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else{
                RecieverViewHolder recieverViewHolder = (RecieverViewHolder) holder;
                recieverViewHolder.binding.feeling.setImageResource(reactions[positionn]);
                recieverViewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            message.setFeeling(positionn);

            FirebaseDatabase.getInstance().getReference().child("chats").child(senderroom).child("messages").child(message.getMessageid()).setValue(message);

            FirebaseDatabase.getInstance().getReference().child("chats").child(receiverroom).child("messages").child(message.getMessageid()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass() == SentViewholder.class )
        {
            SentViewholder sentViewholder = (SentViewholder) holder;
            if(message.getMessage().equals("photo")){
                sentViewholder.binding.image.setVisibility(View.VISIBLE);
                sentViewholder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.avatar)
                        .into(sentViewholder.binding.image);
            }

            ((SentViewholder) holder).binding.message.setText(message.getOrgmessage());
            if(message.getFeeling()>=0)
            {
                sentViewholder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                sentViewholder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else {
                sentViewholder.binding.feeling.setVisibility(View.GONE);
            }

            sentViewholder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            sentViewholder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            sentViewholder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog,null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    if(remoteConfig.getBoolean("isEveryoneDeletionEnabled")) {
                        binding.everyone.setVisibility(View.VISIBLE);
                    } else {
                        binding.everyone.setVisibility(View.GONE);
                    }

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(message.getMessageid()).setValue(message);

                            binding.delete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("public")
                                            .child(message.getMessageid()).setValue(null);

                                    dialog.dismiss();
                                }
                            });
                            binding.cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    });
                    return false;
                }
            });
        }
        else {
            RecieverViewHolder recieverViewHolder = (RecieverViewHolder) holder;
            if(message.getMessage().equals("photo")){
                recieverViewHolder.binding.image.setVisibility(View.VISIBLE);
                recieverViewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.avatar)
                        .into(recieverViewHolder.binding.image);
            }
            ((RecieverViewHolder) holder).binding.message.setText(message.getOrgmessage());

            if(message.getFeeling()>=0)
            {
                recieverViewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                recieverViewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else {
                recieverViewHolder.binding.feeling.setVisibility(View.GONE);
            }
            recieverViewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);

                    return false;
                }
            });

            recieverViewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);

                    return false;
                }
            });
            recieverViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog,null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(message.getMessageid()).setValue(message);

                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(message.getMessageid()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                    return false;
                }
            });

        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderid())){
            return ITEM_SENT;
        }
        else {
            return ITEM_RECIEVE;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewholder extends RecyclerView.ViewHolder{
        ItemSentBinding binding;
        public SentViewholder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }
    public class RecieverViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveBinding binding;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }

}
