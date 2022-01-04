package com.example.warmapp.adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.example.warmapp.activities.CalendarActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Map;

public class MyTrainingsAdapter extends RecyclerView.Adapter<MyTrainingsAdapter.MyViewHolder> {

    Context context;
    ArrayList<TrainingModel> trainings;
    String userID;
    String userType;

    //dialog
    Dialog dialogMoreDetails;
    ImageView imageViewExitIcon;
    CircularImageView imageViewTrainer;
    TextView textViewTrainerName, textViewTrainingType, textViewTrainingDate, textViewTrainingCity, textViewTrainingAddress,
            textViewTrainingTime, textViewTrainingPrice, textViewTrainingParticipants, textViewTrainingFeatures, textViewTrainingDetails;

    public MyTrainingsAdapter(Context context, ArrayList<TrainingModel> trainings, String userType, String userID) {
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
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_down_to_up);
        holder.moreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreDetailsDialog(trainings.get(position));
            }
        });

        String type = trainings.get(position).getTraining().getTitle();
        holder.textViewType.setText(type);
        setTypeIcon(holder, type);

        holder.circularImageTrainer.setImageBitmap(trainings.get(position).getTrainerImage());
        holder.textViewTrainerName.setText(trainings.get(position).getTrainerName());
        String trainingTime = trainings.get(position).getTraining().getStartTraining() + " - " + trainings.get(position).getTraining().getEndTraining();
        holder.textViewTime.setText(trainingTime);
        holder.textViewAddress.setText(trainings.get(position).getTraining().getAddress());
        holder.textViewCity.setText(trainings.get(position).getTraining().getCity());

        holder.itemView.setAnimation(animation);

        holder.cancelTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelTrainingDialog(trainings.get(position).getTraining());
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
    private void showMoreDetailsDialog(TrainingModel trainingModel) {
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
        if (trainingModel.getTraining().getFeatures() != null) {
            for (String feature : trainingModel.getTraining().getFeatures().keySet()) {
                count++;
                features.append(feature);
                if (count != trainingModel.getTraining().getFeatures().keySet().size()) {
                    features.append(", ");
                }
            }
        }
        if (trainingModel.getTraining().getParticipants() == null) {
            textViewTrainingParticipants.setText("0");
        } else {
            textViewTrainingParticipants.setText(trainingModel.getTraining().getParticipants().size() + "");
        }
        textViewTrainerName.setText(trainingModel.getTrainerName());
        imageViewTrainer.setImageBitmap(trainingModel.getTrainerImage());
        textViewTrainingFeatures.setText(features.toString());
        textViewTrainingType.setText(trainingModel.getTraining().getTitle());
        textViewTrainingDate.setText(trainingModel.getTraining().getDate());
        textViewTrainingCity.setText(trainingModel.getTraining().getCity());
        textViewTrainingAddress.setText(trainingModel.getTraining().getAddress());
        textViewTrainingTime.setText(trainingModel.getTraining().getStartTraining() + " - " + trainingModel.getTraining().getEndTraining());
        textViewTrainingPrice.setText(trainingModel.getTraining().getPrice() + "");
        textViewTrainingDetails.setText(trainingModel.getTraining().getDetails());
    }

    private void initViewsDialog() {
        imageViewExitIcon = dialogMoreDetails.findViewById(R.id.more_details_dialog_exit);
        imageViewTrainer = dialogMoreDetails.findViewById(R.id.more_details_dialog_trainer_image);
        textViewTrainerName = dialogMoreDetails.findViewById(R.id.more_details_dialog_trainer_name);
        textViewTrainingType = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_type);
        textViewTrainingDate = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_date);
        textViewTrainingCity = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_city);
        textViewTrainingAddress = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_address);
        textViewTrainingTime = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_time);
        textViewTrainingPrice = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_price);
        textViewTrainingParticipants = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_participants);
        textViewTrainingFeatures = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_features);
        textViewTrainingDetails = dialogMoreDetails.findViewById(R.id.more_details_dialog_text_details);
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
            if (training.getParticipants() != null) {
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

        CircularImageView circularImageTrainer;
        ImageView imageViewType, moreDetails;
        TextView textViewType, textViewTime, textViewAddress, textViewCity, textViewTrainerName;
        FloatingActionButton cancelTraining;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            circularImageTrainer = itemView.findViewById(R.id.layout_my_trainings_trainer_image);
            textViewTrainerName = itemView.findViewById(R.id.layout_my_trainings_trainer_name);
            imageViewType = itemView.findViewById(R.id.layout_my_trainings_image_type);
            textViewType = itemView.findViewById(R.id.layout_my_trainings_text_type);
            textViewTime = itemView.findViewById(R.id.layout_my_trainings_text_time);
            textViewAddress = itemView.findViewById(R.id.layout_my_trainings_text_address);
            textViewCity = itemView.findViewById(R.id.layout_my_trainings_text_city);
            moreDetails = itemView.findViewById(R.id.layout_my_trainings_image_details);
            cancelTraining = itemView.findViewById(R.id.layout_my_trainings_button_cancel);
        }
    }
}
