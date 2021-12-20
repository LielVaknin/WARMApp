package com.example.warmapp.classes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MySearchAdapter extends RecyclerView.Adapter<MySearchAdapter.MyViewHolder> {

    Context context;
    ArrayList<TrainingModel> trainings;
    FirebaseAuth auth;
    String userType;
    String userID;

    public MySearchAdapter(Context context, ArrayList<TrainingModel> trainings, String userType) {
        this.context = context;
        this.trainings = trainings;
        auth= FirebaseAuth.getInstance();
        userID=auth.getCurrentUser().getUid();
        this.userType=userType;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.training_row, parent, false);
        return new MySearchAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Training training = trainings.get(position).training;
        String trainerID = training.getTrainerId();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child(trainerID + ".jpg");

        String trainerName = trainings.get(position).trainerName;
        holder.tvTitle.setText(trainings.get(position).training.getTitle());
        holder.tvCity.setText(trainings.get(position).training.getCity());
        holder.tvTrainerName.setText(trainerName);

        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.trainerImage.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context.getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
            }
        });

        if(userType.equals("trainer")){
            holder.requestTraining.setVisibility(View.INVISIBLE);
        }


        if(trainings.get(position).trainingStatus.equals("request")){
            holder.requestTraining.setImageResource(R.drawable.ic_clock_1);
            holder.requestTraining.setClickable(false);
            holder.requestTraining.setEnabled(false);
        } else if(trainings.get(position).trainingStatus.equals("apply")){
            holder.requestTraining.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
            holder.requestTraining.setClickable(false);
            holder.requestTraining.setEnabled(false);
        }
        else{
            holder.requestTraining.setImageResource(R.drawable.ic_plus_circle);
            holder.requestTraining.setClickable(true);
            holder.requestTraining.setEnabled(true);
        }

        holder.moreDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                View dailogView =
                        LayoutInflater.from(context).inflate(R.layout.more_details_dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setView(dailogView);
                builder.setNegativeButton("Cancel", null);
                AlertDialog alert = builder.create();

                TextInputEditText text1 = dailogView.findViewById(R.id.title_dialog);
                text1.setText(training.getTitle());
                text1.setInputType(InputType.TYPE_NULL);
                text1.setKeyListener(null);

                TextInputEditText text2 = dailogView.findViewById(R.id.features_dialog);
                StringBuilder s = new StringBuilder();
                int sizeFeatures = training.getFeatures().size() - 1;
                for (String feature : training.getFeatures().keySet()) {
                    s.append(training.getFeatures().get(feature)).append(", ");
                }

                text2.setText(s.toString());
                text2.setInputType(InputType.TYPE_NULL);
                text2.setKeyListener(null);

                TextInputEditText text3 = dailogView.findViewById(R.id.address_dialog);
                text3.setText(training.getAddress());
                text3.setInputType(InputType.TYPE_NULL);
                text3.setKeyListener(null);

                TextInputEditText text4 = dailogView.findViewById(R.id.date_dialog);
                text4.setText(training.getDate());
                text4.setInputType(InputType.TYPE_NULL);
                text4.setKeyListener(null);

                TextInputEditText text5 = dailogView.findViewById(R.id.starting_time_dialog);
                text5.setText(training.getStartTraining());
                text5.setInputType(InputType.TYPE_NULL);
                text5.setKeyListener(null);

                TextInputEditText text6 = dailogView.findViewById(R.id.ending_time_dialog);
                text6.setText(training.getEndTraining());
                text6.setInputType(InputType.TYPE_NULL);
                text6.setKeyListener(null);

                TextInputEditText text7 = dailogView.findViewById(R.id.price_dialog);
                text7.setText(training.getPrice() + "₪");
                text7.setInputType(InputType.TYPE_NULL);
                text7.setKeyListener(null);

                TextInputEditText text8 = dailogView.findViewById(R.id.description_dialog);
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

        holder.requestTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trainings.get(position).trainingStatus.equals("block")) {
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
                text2.setText(trainerName);

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
                        holder.requestTraining.setImageResource(R.drawable.ic_clock_1);
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

    @Override
    public int getItemCount() {
        return trainings.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView trainerImage, requestTraining;
        TextView tvTitle, tvCity, tvTrainerName;
        Button moreDetailsBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            trainerImage = itemView.findViewById(R.id.trainer_image);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvTrainerName = itemView.findViewById(R.id.tv_trainer_name);
            moreDetailsBtn = itemView.findViewById(R.id.more_details_button);
            requestTraining = itemView.findViewById(R.id.request_training);
        }
    }
}
