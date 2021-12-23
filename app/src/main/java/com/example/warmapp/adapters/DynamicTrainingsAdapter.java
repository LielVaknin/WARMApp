package com.example.warmapp.adapters;

import android.annotation.SuppressLint;
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
import com.example.warmapp.classes.Training;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class DynamicTrainingsAdapter extends RecyclerView.Adapter<DynamicTrainingsAdapter.DynamicTrainingsHolder> {

    private Context context;
    private ArrayList<Training> trainings;

    public DynamicTrainingsAdapter(Context context, ArrayList<Training> trainings) {
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
    public void onBindViewHolder(@NonNull DynamicTrainingsAdapter.DynamicTrainingsHolder holder, int position) {
        Training training = trainings.get(position);

        holder.textViewType.setText(training.getTitle());
        holder.textViewTime.setText(training.getStartTraining() + " - " + training.getEndTraining());
        holder.textViewCity.setText(training.getCity());
        holder.textViewAddress.setText(training.getAddress());

    }

    @Override
    public int getItemCount() {
        return trainings.size();
    }

    public static class DynamicTrainingsHolder extends RecyclerView.ViewHolder {

        ImageView imageViewType, moreDetails;
        TextView textViewType, textViewTime, textViewAddress, textViewCity;
        MaterialButton cancelTraining;
        ProgressBar progressBar;

        public DynamicTrainingsHolder(@NonNull View itemView) {
            super(itemView);

            imageViewType = itemView.findViewById(R.id.image_card_type);
            textViewType = itemView.findViewById(R.id.type_trainings_selected_text_card_type);
            textViewTime = itemView.findViewById(R.id.type_trainings_selected_text_card_time);
            textViewAddress = itemView.findViewById(R.id.type_trainings_selected_text_card_address);
            textViewCity = itemView.findViewById(R.id.type_trainings_selected_text_card_city);
            moreDetails = itemView.findViewById(R.id.type_trainings_selected_image_card_details);
            cancelTraining = itemView.findViewById(R.id.type_trainings_selected_button_card_cancel);
            progressBar = itemView.findViewById(R.id.type_trainings_selected_progress_bar);
        }
    }
}
