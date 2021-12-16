package com.example.warmapp.traineeActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;
import com.example.warmapp.classes.RequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class requests_trainee_RecyclerViewAdapter extends RecyclerView.Adapter<requests_trainee_RecyclerViewAdapter.myViewHolder> {
    Context context;
    ArrayList<RequestModel> requests;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    String userID;


    public requests_trainee_RecyclerViewAdapter(Context context, ArrayList<RequestModel> requests){
        this.context=context;
        this.requests=requests;
        databaseReference= FirebaseDatabase.getInstance().getReference();
        auth= FirebaseAuth.getInstance();
        userID=auth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public requests_trainee_RecyclerViewAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trainee_request_row,parent,false);
        return new requests_trainee_RecyclerViewAdapter.myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull requests_trainee_RecyclerViewAdapter.myViewHolder holder, int position) {

        holder.trainerName.setText(requests.get(position).otherUserName);
        holder.trainingDate.setText(requests.get(position).trainingDate);
        holder.trainingTime.setText(requests.get(position).trainingTime);
        holder.trainingTitle.setText(requests.get(position).trainingTitle);
        holder.paymentMethod.setText(requests.get(position).paymentMethod);
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setMessage("Cancel "+ holder.trainingTitle.getText() + " training request with "+holder.trainerName.getText()+"?")
                        .setPositiveButton("Yes", null)
                        .setNegativeButton("No", null)
                        .show();

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View alertView) {
                        removeRequest(holder.getAdapterPosition());
                        holder.cancel.setVisibility(View.GONE);
                        holder.message.setText("The training is canceled");
                        holder.message.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });

            }
        });

    }

    private void removeRequest(int position){
        String requestID= requests.get(position).requestID;
        String trainerID= requests.get(position).otherUserID;
        String trainingID = requests.get(position).trainingID;

        //remove the request from the trainee requests list
        databaseReference.child("Users").
                child(userID).child("requests").child(requestID).removeValue();

        //remove the request from the trainer requests list
        databaseReference.child("Users").
                child(trainerID).child("requests").child(requestID).removeValue();

        //remove the request from the requests list
        databaseReference.child("Requests").
                child(requestID).removeValue();
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{

        ImageView trainerImage;
        TextView trainerName,paymentMethod,trainingTitle,trainingDate,trainingTime,message;
        ImageButton cancel;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            trainerImage = itemView.findViewById(R.id.request_trainer_image);
            trainerName =itemView.findViewById(R.id.request_trainer_name);
            trainerName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }


            });
            paymentMethod=itemView.findViewById(R.id.request_payment_method2);
            trainingTitle=itemView.findViewById(R.id.request_training_title2);
            trainingDate=itemView.findViewById(R.id.request_training_date2);
            trainingTime=itemView.findViewById(R.id.request_training_time2);
            cancel= itemView.findViewById(R.id.request_cancel_button);
            message = itemView.findViewById(R.id.message_request2);


        }
    }
}
