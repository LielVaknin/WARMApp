package com.example.warmapp.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;

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
    public void onBindViewHolder(@NonNull MyTrainingsAdapter.MyViewHolder holder, int position) {
        holder.textViewType.setText(trainings.get(position).getTitle());
        String trainingTime = trainings.get(position).getStartTraining() + " - " + trainings.get(position).getEndTraining();
        holder.textViewTime.setText(trainingTime);
        holder.textViewAddress.setText(trainings.get(position).getAddress());
        holder.textViewCity.setText(trainings.get(position).getCity());
    }

    @Override
    public int getItemCount() {
        return trainings.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageViewType;
        TextView textViewType,textViewTime,textViewAddress,textViewCity;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewType = itemView.findViewById(R.id.text_card_type);
            textViewTime = itemView.findViewById(R.id.text_card_time);
            textViewAddress = itemView.findViewById(R.id.text_card_address);
            textViewCity = itemView.findViewById(R.id.text_card_city);
        }
    }
}
