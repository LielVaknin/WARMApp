package com.example.warmapp.classes;

import android.annotation.SuppressLint;
import android.app.Dialog;
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

    //dialog
    Dialog dialogMoreDetails;
    ImageView imageViewExitIcon;
    TextView textViewParticipants, textViewFeatures, textViewPrice, textViewDetails;

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

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void showMoreDetailsDialog(Training training) {
        dialogMoreDetails = new Dialog(context);
        dialogMoreDetails.setContentView(R.layout.layout_more_details_dialog);
        dialogMoreDetails.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.edit_text_bg));
        dialogMoreDetails.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogMoreDetails.setCancelable(false);
        dialogMoreDetails.getWindow().getAttributes().windowAnimations = R.style.animation;
        dialogMoreDetails.show();

        initViewsDialog();

        imageViewExitIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogMoreDetails.dismiss();
            }
        });

        StringBuilder features = new StringBuilder();
        int count = 0;
        for (String feature : training.getFeatures().keySet()) {
            count++;
            features.append(feature);
            if (count != training.getFeatures().keySet().size()){
                features.append(", ");
            }
        }
        if (training.getParticipants() == null){
            textViewParticipants.setText("0");
        }else {
            textViewParticipants.setText(training.getParticipants().size() + "");
        }
        textViewFeatures.setText(features.toString());
        textViewPrice.setText(training.getPrice() + "");
        textViewDetails.setText(training.getDetails());
    }

    private void initViewsDialog() {
        imageViewExitIcon = dialogMoreDetails.findViewById(R.id.more_details_calendar_exit_dialog);
        textViewParticipants = dialogMoreDetails.findViewById(R.id.more_details_calendar_text_participants);
        textViewFeatures = dialogMoreDetails.findViewById(R.id.more_details_calendar_text_features);
        textViewPrice = dialogMoreDetails.findViewById(R.id.more_details_calendar_text_price);
        textViewDetails = dialogMoreDetails.findViewById(R.id.more_details_calendar_text_details);
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
            textViewType = itemView.findViewById(R.id.type_trainings_selected_text_card_type);
            textViewTime = itemView.findViewById(R.id.type_trainings_selected_text_card_time);
            textViewAddress = itemView.findViewById(R.id.type_trainings_selected_text_card_address);
            textViewCity = itemView.findViewById(R.id.type_trainings_selected_text_card_city);
            moreDetails = itemView.findViewById(R.id.type_trainings_selected_image_card_details);
            cancelTraining = itemView.findViewById(R.id.type_trainings_selected_button_card_cancel);
        }
    }
}
