package com.example.warmapp.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
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
import com.example.warmapp.classes.TrainerModel;
import com.example.warmapp.activitys.TrainerProfileActivity;

import java.util.ArrayList;

public class SearchTrainerProfileAdapter extends RecyclerView.Adapter<SearchTrainerProfileAdapter.MyViewHolder> {

    Context context;
    ArrayList<TrainerModel> trainers;

    public SearchTrainerProfileAdapter(Context context, ArrayList<TrainerModel> trainers) {
        this.context = context;
        this.trainers = trainers;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_search_trainer_row, parent, false);
        return new SearchTrainerProfileAdapter.MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchTrainerProfileAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);

        holder.trainerName.setText(trainers.get(position).getUserTrainer().getFirstName() + " " + trainers.get(position).getUserTrainer().getLastName());
        holder.trainerDescription.setText(trainers.get(position).getUserTrainer().getDescription());
        holder.trainerRatingNumber.setText(trainers.get(position).getUserTrainer().getRating() + "");
        holder.trainerImage.setImageBitmap(trainers.get(position).getTrainerImage());

        holder.itemView.setAnimation(animation);

        holder.trainerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(context, TrainerProfileActivity.class);
                Pair[] pairs = new Pair[4];
                pairs[0] = new Pair<View,String>(holder.trainerImage,"trainer_image");
                pairs[1] = new Pair<View,String>(holder.trainerName,"trainer_name");
                pairs[2] = new Pair<View,String>(holder.trainerRatingNumber,"trainer_rating");
                pairs[3] = new Pair<View,String>(holder.trainerDescription,"trainer_description");

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context,pairs);

                myIntent.putExtra("trainerId", trainers.get(position).getTrainerID());
                myIntent.putExtra("firstName",trainers.get(position).getUserTrainer().getFirstName());
                myIntent.putExtra("lastName", trainers.get(position).getUserTrainer().getLastName());
                myIntent.putExtra("description", trainers.get(position).getUserTrainer().getDescription());
                myIntent.putExtra("phone", trainers.get(position).getUserTrainer().getPhone());
                myIntent.putExtra("mail", trainers.get(position).getUserTrainer().getMail());
                context.startActivity(myIntent,activityOptions.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.trainers.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView trainerImage;
        TextView trainerName, trainerDescription, trainerRatingNumber;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            trainerImage = itemView.findViewById(R.id.trainer_image);
            trainerName = itemView.findViewById(R.id.trainer_name);
            trainerDescription = itemView.findViewById(R.id.trainer_review);
            trainerRatingNumber = itemView.findViewById(R.id.rating_av_number);
        }
    }
}
