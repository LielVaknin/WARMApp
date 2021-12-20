package com.example.warmapp.trainerActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Notification;
import androidx.recyclerview.widget.RecyclerView;
import com.example.warmapp.R;
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.RequestModel;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.util.ArrayList;



    public class requests_trainer_RecyclerViewAdapter extends RecyclerView.Adapter<requests_trainer_RecyclerViewAdapter.myViewHolder> {
        Context context;
        ArrayList<RequestModel> requests;
        DatabaseReference databaseReference;
        FirebaseAuth auth;
        String userID;
        private NotificationManagerCompat notificationManager;

        public requests_trainer_RecyclerViewAdapter(Context context, ArrayList<RequestModel> requests){
            this.context=context;
            this.requests=requests;
            databaseReference= FirebaseDatabase.getInstance().getReference();
            auth= FirebaseAuth.getInstance();

            userID=auth.getCurrentUser().getUid();
            notificationManager = NotificationManagerCompat.from(context);
        }

        @NonNull
        @Override
        public requests_trainer_RecyclerViewAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.trainer_request_row,parent,false);
            return new requests_trainer_RecyclerViewAdapter.myViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

            holder.traineeName.setText(requests.get(position).otherUserName);
            holder.trainingDate.setText(requests.get(position).training.getDate());
            holder.trainingTime.setText(requests.get(position).training.getStartTraining()+"-"+requests.get(position).training.getEndTraining());
            holder.trainingTitle.setText(requests.get(position).training.getTitle());
            holder.paymentMethod.setText(requests.get(position).paymentMethod);
            holder.trainerPhone.setText(requests.get(position).otherUserPhone);
            holder.trainerPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" +holder.trainerPhone.getText()));
                    context.startActivity(intent);
                }
            });
            holder.apply.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    try {
                        removeRequest(holder.getAdapterPosition());
                        String traineeID = requests.get(holder.getAdapterPosition()).otherUserID;
                        String trainingID = requests.get(holder.getAdapterPosition()).training.getTrainingID();
                        databaseReference.child("Users").child(traineeID).child("trainings").child(trainingID).setValue(true);
                        //add the trainee to the training participants list
                        databaseReference.child("Trainings").
                                child(trainingID).child("participants").child(traineeID).setValue(true);

                        holder.reject.setVisibility(View.GONE);
                        holder.apply.setVisibility(View.GONE);

                        holder.message.setText("The training is approved");
                        holder.message.setVisibility(View.VISIBLE);

                        int currentParticipants= requests.get(holder.getAdapterPosition()).training.getParticipants().size();
                        int maxParticipants= requests.get(holder.getAdapterPosition()).training.getMaxParticipants();
                        if (currentParticipants==maxParticipants){
                            databaseReference.child("Users").child(userID).child("requests").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   for (DataSnapshot reqID : snapshot.getChildren()){
                                       Request r = reqID.getValue(Request.class);
                                       if(r.getTrainingID().equals(trainingID)){
                                           removeRequest(reqID.getKey(),traineeID,userID);
                                       }
                                   }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        int currentPosition= holder.getAdapterPosition();
                        String date= requests.get(currentPosition).training.getDate();
                        String start =requests.get(currentPosition).training.getStartTraining();
                        String end =requests.get(currentPosition).training.getEndTraining();
                        databaseReference.child("Users").child(traineeID).child("requests").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String requestID = dataSnapshot.getKey();
                                    if(requestID!=requests.get(currentPosition).requestID) {
                                        databaseReference.child("Requests").child(requestID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Request request = snapshot.getValue(Request.class);
                                                String trainingID = request.getTrainingID();
                                                databaseReference.child("Trainings").child(trainingID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Training training = snapshot.getValue(Training.class);
                                                        String otherDate= training.getDate();
                                                        if(otherDate.equals(holder.trainingDate.getText().toString())){
                                                            String otherStart = training.getStartTraining();
                                                            String otherEnd = training.getEndTraining();
                                                            if(isOverlapping(start,end,otherStart,otherEnd)){
                                                                removeRequest(requestID,traineeID,training.getTrainerId());
                                                               /* databaseReference.child("Users").child(training.getTrainerId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        User trainer=snapshot.getValue(User.class);
                                                                        String trainerName= trainer.getFirstName() + " "+trainer.getLastName();
                                                                        String title = "Request Status";
                                                                        String message = "Request for "+training.getTitle() + " with "+trainerName +" is canceled (overlapping)";
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });*/

                                                            }

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }

                                        });
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }

                        });

                    }catch (NullPointerException e){
                        Toast.makeText(context,"Something went wrong, please refresh the page",Toast.LENGTH_LONG);
                    }

                }
            });
            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                            .setMessage("Reject "+ holder.traineeName.getText()+" "+holder.trainingTitle.getText() + " training request?")
                            .setPositiveButton("Yes", null)
                            .setNegativeButton("No", null)
                            .show();

                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View alertView) {
                            removeRequest(holder.getAdapterPosition());
                            holder.reject.setVisibility(View.GONE);
                            holder.apply.setVisibility(View.GONE);
                            holder.message.setText("The training is rejected");
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



        @RequiresApi(api = Build.VERSION_CODES.O)
        public static boolean isOverlapping(String start1, String end1, String start2, String end2) {
            LocalTime startTraining1 = LocalTime.parse(start1);
            LocalTime endTraining1 = LocalTime.parse(end1);
            LocalTime startTraining2 = LocalTime.parse(start2);
            LocalTime endTraining2 = LocalTime.parse(end2);
            return (startTraining1.isBefore(endTraining2) && startTraining2.isBefore(endTraining1));

        }

        private void removeRequest(int position){
            String requestID= requests.get(position).requestID;
            String traineeID= requests.get(position).otherUserID;

            String trainingID = requests.get(position).training.getTrainingID();
            try{
                //remove the request from the trainer requests list
                databaseReference.child("Users").
                        child(userID).child("requests").child(requestID).removeValue();

                //remove the request from the trainee requests list
                databaseReference.child("Users").
                        child(traineeID).child("requests").child(requestID).removeValue();

                //remove the request from the requests list
                databaseReference.child("Requests").
                        child(requestID).removeValue();
            }catch (Exception e){
                Toast.makeText(context,"Something went wrong, please refresh the page",Toast.LENGTH_LONG);
            }

        }

        private void removeRequest(String requestID,String traineeID,String trainerID){

            try{
                //remove the request from the trainer requests list
                databaseReference.child("Users").
                        child(trainerID).child("requests").child(requestID).removeValue();

                //remove the request from the trainee requests list
                databaseReference.child("Users").
                        child(traineeID).child("requests").child(requestID).removeValue();

                //remove the request from the requests list
                databaseReference.child("Requests").
                        child(requestID).removeValue();
            }catch (Exception e){
                Toast.makeText(context,"Something went wrong, please refresh the page",Toast.LENGTH_LONG);
            }

        }

        public void sendEmail(String title,String message) {

        }

            @Override
        public int getItemCount() {
            return requests.size();
        }

        public static class myViewHolder extends RecyclerView.ViewHolder{



            ImageView traineeImage;
            TextView traineeName,paymentMethod,trainingTitle,trainingDate,trainingTime,trainerPhone,message;
            ImageButton reject,apply;

            public myViewHolder(@NonNull View itemView) {
                super(itemView);
                traineeImage= itemView.findViewById(R.id.request_trainee_image);
                traineeName=itemView.findViewById(R.id.request_trainee_name);
                traineeName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }


                });
                paymentMethod=itemView.findViewById(R.id.request_payment_method1);
                trainingTitle=itemView.findViewById(R.id.training_title);
                trainingDate=itemView.findViewById(R.id.request_training_date1);
                trainingTime=itemView.findViewById(R.id.request_training_time1);
                trainerPhone=itemView.findViewById(R.id.request_trainee_phone);
                apply = itemView.findViewById(R.id.request_apply_button);
                reject= itemView.findViewById(R.id.request_reject_button);
                message = itemView.findViewById(R.id.message_request);


            }
        }
}
