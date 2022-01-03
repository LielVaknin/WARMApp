package com.example.warmapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.warmapp.R;
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Objects;

public class SearchTrainingsAdapter extends RecyclerView.Adapter<SearchTrainingsAdapter.MyViewHolder> {

    Context context;
    ArrayList<TrainingModel> trainings;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
    String userType;
    Dialog dialogMoreDetails;
    ImageView imageViewExitIcon;
    CircularImageView imageViewTrainer;
    TextView textViewTrainerName, textViewTrainingType, textViewTrainingDate, textViewTrainingCity, textViewTrainingAddress,
            textViewTrainingTime, textViewTrainingPrice, textViewTrainingParticipants, textViewTrainingFeatures, textViewTrainingDetails;

    public SearchTrainingsAdapter(Context context, ArrayList<TrainingModel> trainings,String userType) {
        this.context = context;
        this.trainings = trainings;
        this.userType=userType;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_search_training_row, parent, false);
        return new SearchTrainingsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);

        holder.tvTitle.setText(trainings.get(position).getTraining().getTitle());
        holder.tvCity.setText(trainings.get(position).getTraining().getCity());
        holder.tvTrainerName.setText(trainings.get(position).getTrainerName());
        holder.trainerImage.setImageBitmap(trainings.get(position).getTrainerImage());
        holder.itemView.setAnimation(animation);

        if(userType.equals("trainer")){
            holder.requestTraining.setVisibility(View.INVISIBLE);
        }


        if(trainings.get(position).getTrainingStatus().equals("request")){
            holder.requestTraining.setImageResource(R.drawable.ic_clock);
            holder.requestTraining.setClickable(false);
            holder.requestTraining.setEnabled(false);
        } else if(trainings.get(position).getTrainingStatus().equals("registered")){
            holder.requestTraining.setImageResource(R.drawable.ic_check);
            holder.requestTraining.setClickable(false);
            holder.requestTraining.setEnabled(false);
        }
        else{
            holder.requestTraining.setImageResource(R.drawable.ic_plus);
            holder.requestTraining.setClickable(true);
            holder.requestTraining.setEnabled(true);
        }

        holder.moreDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreDetailsDialog(trainings.get(position));
            }
        });


        holder.requestTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Training training=trainings.get(position).getTraining();
                if (trainings.get(position).getTrainingStatus().equals("overlapping")) {
                    Toast t = Toast.makeText(context, "The training overlaps with existing training", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }
                View dailogView =
                        LayoutInflater.from(context).inflate(R.layout.request_training_dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dailogView);

                TextView text1 = dailogView.findViewById(R.id.training_title);
                text1.setText(training.getTitle());

                TextView text2 = dailogView.findViewById(R.id.trainer_name);
                text2.setText(trainings.get(position).getTrainerName());

                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Send request", null);
                AlertDialog alert = builder.create();
                alert.show();
                Button positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                RadioGroup rg = dailogView.findViewById(R.id.radio_group);
                RadioButton cardBtn = dailogView.findViewById(R.id.card);
                RadioButton otherBtn = dailogView.findViewById(R.id.other);
                CardView cardViewPayment = dailogView.findViewById(R.id.card_view_payment);
                cardBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardViewPayment.setVisibility(View.VISIBLE);
                    }
                });
                otherBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardViewPayment.setVisibility(View.GONE);
                    }
                });
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Request request = new Request(userID, training.getTrainingID());

                        int buttonID = rg.getCheckedRadioButtonId();
                        RadioButton rb = dailogView.findViewById(buttonID);

                        request.setPaymentMethod(rb.getText().toString());
                        String requestID = FirebaseDatabase.getInstance().getReference().child("Requests").push().getKey();
                        FirebaseDatabase.getInstance().getReference().child("Requests").child(requestID).setValue(request);
                        FirebaseDatabase.getInstance().getReference().child("Users").child(training.getTrainerId()).child("requests").child(requestID).setValue(true);
                        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("requests").child(requestID).setValue(true);
                        holder.requestTraining.setImageResource(R.drawable.ic_clock);
                        holder.requestTraining.setClickable(false);
                        holder.requestTraining.setEnabled(false);
                        alert.dismiss();
                        Toast t = Toast.makeText(context, "The request has been sent", Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        FloatingActionButton requestTraining;
        ImageView trainerImage,moreDetailsBtn;
        TextView tvTitle, tvCity, tvTrainerName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            trainerImage = itemView.findViewById(R.id.trainer_image);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvTrainerName = itemView.findViewById(R.id.tv_trainer_name);
            moreDetailsBtn = itemView.findViewById(R.id.more_details_button);
            requestTraining = itemView.findViewById(R.id.search_activity_request_training_button);
        }
    }
}
