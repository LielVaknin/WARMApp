package com.example.warmapp.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MyTrainingsAdapter extends RecyclerView.Adapter<MyTrainingsAdapter.MyViewHolder> {

    Context context;
    ArrayList<Training> trainings;

    public MyTrainingsAdapter(Context context, ArrayList<Training> trainings){
        this.context = context;
        this.trainings = trainings;
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
        setTypeIcon(holder,type);

        String trainingTime = trainings.get(position).getStartTraining() + " - " + trainings.get(position).getEndTraining();
        holder.textViewTime.setText(trainingTime);
        holder.textViewAddress.setText(trainings.get(position).getAddress());
        holder.textViewCity.setText(trainings.get(position).getCity());

        holder.cancelTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelTrainingDialog();
            }
        });
    }



    @Override
    public int getItemCount() {
        return trainings.size();
    }

    private void setTypeIcon(MyViewHolder holder, String type) {
        holder.imageViewType.setImageResource(R.drawable.apply_icon);
    }

    private void showMoreDetailsDialog(Training training) {

    }

    private void showCancelTrainingDialog() {

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageViewType,moreDetails;
        TextView textViewType,textViewTime,textViewAddress,textViewCity;
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
