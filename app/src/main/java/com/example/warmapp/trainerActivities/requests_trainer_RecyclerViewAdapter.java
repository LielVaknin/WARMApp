package com.example.warmapp.trainerActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.warmapp.R;
import com.example.warmapp.classes.RequestModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;



    public class requests_trainer_RecyclerViewAdapter extends RecyclerView.Adapter<requests_trainer_RecyclerViewAdapter.myViewHolder> {
        Context context;
        ArrayList<RequestModel> requests;
        DatabaseReference databaseReference;


        public requests_trainer_RecyclerViewAdapter(Context context, ArrayList<RequestModel> requests){
            this.context=context;
            this.requests=requests;
            databaseReference= FirebaseDatabase.getInstance().getReference();
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
            holder.trainingDate.setText(requests.get(position).trainingDate);
            holder.trainingTime.setText(requests.get(position).trainingTime);
            holder.trainingTitle.setText(requests.get(position).trainingTitle);
            holder.paymentMethod.setText(requests.get(position).paymentMethod);
            holder.apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String traineeID = requests.get(holder.getAdapterPosition()).otherUserID;
                    String trainingID = requests.get(holder.getAdapterPosition()).trainingID;
                    databaseReference.child("Users").
                            child("Trainee").child(traineeID).child("trainings").child(trainingID).setValue(true);
                    //add the trainee to the training participants list
                    databaseReference.child("Trainings").
                            child(trainingID).child("participants").child(traineeID).setValue(true);
                    removeRequest(holder.getAdapterPosition());
                    holder.reject.setVisibility(View.GONE);
                    holder.apply.setVisibility(View.GONE);

                    holder.message.setText("The training is approved");
                    holder.message.setVisibility(View.VISIBLE);
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
                        }
                    });

                }
            });

        }

        private void removeRequest(int position){
            String requestID= requests.get(position).requestID;
            String traineeID= requests.get(position).otherUserID;
            String userID= "Z1QT2iiO0ZVOMg7E8vph4TQQLT32";
            String trainingID = requests.get(position).trainingID;

            //remove the request from the trainer requests list
            databaseReference.child("Users").
                    child("Trainer").child(userID).child("requests").child(requestID).removeValue();

            //remove the request from the trainee requests list
            databaseReference.child("Users").
                    child("Trainee").child(traineeID).child("requests").child(requestID).removeValue();

            //remove the request from the requests list
            databaseReference.child("Requests").
                    child(requestID).removeValue();
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        public static class myViewHolder extends RecyclerView.ViewHolder{

            ImageView traineeImage;
            TextView traineeName,paymentMethod,trainingTitle,trainingDate,trainingTime,message;
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
                trainingTitle=itemView.findViewById(R.id.request_training_title1);
                trainingDate=itemView.findViewById(R.id.request_training_date1);
                trainingTime=itemView.findViewById(R.id.request_training_time1);
                apply = itemView.findViewById(R.id.request_apply_button);
                reject= itemView.findViewById(R.id.request_reject_button);
                message = itemView.findViewById(R.id.message_request);


            }
        }
}
