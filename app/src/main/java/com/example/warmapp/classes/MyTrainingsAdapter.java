package com.example.warmapp.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;
import com.example.warmapp.trainerActivities.CalendarActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

public class MyTrainingsAdapter extends RecyclerView.Adapter<MyTrainingsAdapter.MyViewHolder> {

    Context context;
    ArrayList<Training> trainings;
    String userID;
    String userType;

    public MyTrainingsAdapter(Context context, ArrayList<Training> trainings, String userType, String userID) {
        this.context = context;
        this.trainings = trainings;
        this.userID = userID;
        this.userType = userType;
    }

    @NonNull
    @Override
    public MyTrainingsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_recycle_view_my_trainings, parent, false);
        return new MyTrainingsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyTrainingsAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.moreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreDetailsDialog(trainings.get(position));
            }
        });

        String type = trainings.get(position).getTitle();
        holder.textViewType.setText(type);
        setTypeIcon(holder, type);

        String trainingTime = trainings.get(position).getStartTraining() + " - " + trainings.get(position).getEndTraining();
        holder.textViewTime.setText(trainingTime);
        holder.textViewAddress.setText(trainings.get(position).getAddress());
        holder.textViewCity.setText(trainings.get(position).getCity());

        holder.cancelTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelTrainingDialog(trainings.get(position));
            }
        });
    }


    @Override
    public int getItemCount() {
        return trainings.size();
    }

    private void setTypeIcon(MyViewHolder holder, String type) {
        holder.imageViewType.setImageResource(R.drawable.ic_user);
    }

    private void showMoreDetailsDialog(Training training) {

    }

    private void showCancelTrainingDialog(Training training) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_rounded);
        builder.setTitle("Cancel Training");
        builder.setMessage("Are you sure you want to cancel the training?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelTraining(training);
                Intent intent = new Intent(context, CalendarActivity.class);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void cancelTraining(Training training) {
        if (userType.equals("trainer")) {
            //get list from training of participants
            ArrayList<String> trainingUsers = new ArrayList<>();
            if(training.getParticipants() != null){
                for (Map.Entry<String, Boolean> entry : training.getParticipants().entrySet()) {
                    trainingUsers.add(entry.getKey());
                }
            }
            //remove all participants from the training
            for (int i = 0; i < trainingUsers.size(); i++) {
                FirebaseDatabase.getInstance().getReference("Users").child(trainingUsers.get(i)).child("trainings")
                        .child(training.getTrainingID()).removeValue();
            }
            //remove the training
            FirebaseDatabase.getInstance().getReference("Trainings").child(training.getTrainingID()).removeValue();
        } else {
            //remove user from participants list in the training
            FirebaseDatabase.getInstance().getReference("Trainings").child(training.getTrainingID()).child("participants")
                    .child(userID).removeValue();
        }
        //remove the training from user/trainer trainings list
        FirebaseDatabase.getInstance().getReference("Users").child(userID).child("trainings")
                .child(training.getTrainingID()).removeValue();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewType, moreDetails;
        TextView textViewType, textViewTime, textViewAddress, textViewCity;
        MaterialButton cancelTraining;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewType = itemView.findViewById(R.id.image_card_type);
            textViewType = itemView.findViewById(R.id.text_card_type);
            textViewTime = itemView.findViewById(R.id.text_card_time);
            textViewAddress = itemView.findViewById(R.id.text_card_address);
            textViewCity = itemView.findViewById(R.id.text_card_city);
            moreDetails = itemView.findViewById(R.id.image_card_details);
            cancelTraining = itemView.findViewById(R.id.button_card_cancel);
        }
    }
}
