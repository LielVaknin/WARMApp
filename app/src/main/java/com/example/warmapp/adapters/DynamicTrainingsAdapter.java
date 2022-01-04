package com.example.warmapp.adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;
import com.example.warmapp.classes.TrainerModel;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class DynamicTrainingsAdapter extends RecyclerView.Adapter<DynamicTrainingsAdapter.DynamicTrainingsHolder> {

    private Context context;
    private ArrayList<TrainingModel> trainings;

    //dialog
    Dialog dialogMoreDetails;
    ImageView imageViewExitIcon;
    CircularImageView imageViewTrainer;
    TextView textViewTrainerName, textViewTrainingType, textViewTrainingDate, textViewTrainingCity, textViewTrainingAddress,
            textViewTrainingTime, textViewTrainingPrice, textViewTrainingParticipants, textViewTrainingFeatures, textViewTrainingDetails;

    public DynamicTrainingsAdapter(Context context, ArrayList<TrainingModel> trainings) {
        this.context = context;
        this.trainings = trainings;
    }

    @NonNull
    @Override
    public DynamicTrainingsAdapter.DynamicTrainingsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_type_trainings_selected, parent, false);
        return new DynamicTrainingsAdapter.DynamicTrainingsHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DynamicTrainingsAdapter.DynamicTrainingsHolder holder, @SuppressLint("RecyclerView") int position) {
        Training training = trainings.get(position).getTraining();

        holder.circularImageTrainer.setImageBitmap(trainings.get(position).getTrainerImage());
        holder.textViewTrainerName.setText(trainings.get(position).getTrainerName());
        holder.textViewTime.setText(training.getStartTraining() + " - " + training.getEndTraining());
        holder.textViewCity.setText(training.getCity());
        holder.textViewAddress.setText(training.getAddress());

        holder.moreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreDetailsDialog(trainings.get(position));
            }
        });

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
        for (String feature : trainingModel.getTraining().getFeatures().keySet()) {
            count++;
            features.append(feature);
            if (count != trainingModel.getTraining().getFeatures().keySet().size()){
                features.append(", ");
            }
        }
        if (trainingModel.getTraining().getParticipants() == null){
            textViewTrainingParticipants.setText("0");
        }else {
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

    @Override
    public int getItemCount() {
        return trainings.size();
    }

    public static class DynamicTrainingsHolder extends RecyclerView.ViewHolder {

        CircularImageView circularImageTrainer;
        ImageView moreDetails;
        TextView textViewTime, textViewAddress, textViewCity, textViewTrainerName;
        FloatingActionButton addTraining;

        public DynamicTrainingsHolder(@NonNull View itemView) {
            super(itemView);

            circularImageTrainer = itemView.findViewById(R.id.type_trainings_selected_trainer_image);
            textViewTrainerName = itemView.findViewById(R.id.type_trainings_selected_trainer_name);
            textViewTime = itemView.findViewById(R.id.type_trainings_selected_text_time);
            textViewAddress = itemView.findViewById(R.id.type_trainings_selected_text_address);
            textViewCity = itemView.findViewById(R.id.type_trainings_selected_text_city);
            moreDetails = itemView.findViewById(R.id.type_trainings_selected_image_details);
            addTraining = itemView.findViewById(R.id.type_trainings_selected_button_add_training);
        }
    }
}
