package com.example.warmapp.traineeActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
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
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.RequestModel;
import com.example.warmapp.classes.Training;
import com.google.android.material.textfield.TextInputEditText;
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
        holder.trainingDate.setText(requests.get(position).training.getDate());
        holder.trainingTime.setText(requests.get(position).training.getStartTraining()+"-"+requests.get(position).training.getEndTraining());
        holder.trainingTitle.setText(requests.get(position).training.getTitle());
        holder.paymentMethod.setText(requests.get(position).paymentMethod);
        holder.trainerImage.setImageBitmap(requests.get(position).otherUserPhoto);
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

        holder.trainingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Training training = requests.get(holder.getAdapterPosition()).training;
                View dialogView =
                        LayoutInflater.from(context).inflate(R.layout.more_details_dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dialogView);
                builder.setNegativeButton("Cancel", null);
                AlertDialog alert = builder.create();

                TextInputEditText text1 = dialogView.findViewById(R.id.title_dialog);
                text1.setText(training.getTitle());
                text1.setInputType(InputType.TYPE_NULL);
                text1.setKeyListener(null);

                TextInputEditText text2 = dialogView.findViewById(R.id.features_dialog);
                StringBuilder s = new StringBuilder();
                int sizeFeatures = training.getFeatures().size() - 1;
                for (String feature : training.getFeatures().keySet()) {
                    s.append(training.getFeatures().get(feature)).append(", ");
                }

                text2.setText(s.toString());
                text2.setInputType(InputType.TYPE_NULL);
                text2.setKeyListener(null);

                TextInputEditText text3 = dialogView.findViewById(R.id.address_dialog);
                text3.setText(training.getAddress());
                text3.setInputType(InputType.TYPE_NULL);
                text3.setKeyListener(null);

                TextInputEditText text4 = dialogView.findViewById(R.id.date_dialog);
                text4.setText(training.getDate());
                text4.setInputType(InputType.TYPE_NULL);
                text4.setKeyListener(null);

                TextInputEditText text5 = dialogView.findViewById(R.id.starting_time_dialog);
                text5.setText(training.getStartTraining());
                text5.setInputType(InputType.TYPE_NULL);
                text5.setKeyListener(null);

                TextInputEditText text6 = dialogView.findViewById(R.id.ending_time_dialog);
                text6.setText(training.getEndTraining());
                text6.setInputType(InputType.TYPE_NULL);
                text6.setKeyListener(null);

                TextInputEditText text7 = dialogView.findViewById(R.id.price_dialog);
                text7.setText(training.getPrice() + "₪");
                text7.setInputType(InputType.TYPE_NULL);
                text7.setKeyListener(null);

                TextInputEditText text8 = dialogView.findViewById(R.id.description_dialog);
                text8.setText(training.getDetails());
                text8.setInputType(InputType.TYPE_NULL);
                text8.setKeyListener(null);

                alert.show();
                Button negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
            }
            });
        }



    private void removeRequest(int position){
        String requestID= requests.get(position).requestID;
        String trainerID= requests.get(position).training.getTrainerId();
        String trainingID = requests.get(position).training.getTrainingID();

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
