package com.example.warmapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.warmapp.R;
import com.example.warmapp.classes.Review;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.util.ArrayList;
import java.util.HashMap;

public class TrainerProfileAdapter extends RecyclerView.Adapter<TrainerProfileAdapter.MyViewHolder> {

    Context context;
    ArrayList<Review> reviews;
    HashMap<String, Bitmap> profileImages;

    public TrainerProfileAdapter(Context context, ArrayList<Review> reviews, HashMap<String, Bitmap> profileImages) {
        this.context = context;
        this.reviews = reviews;
        this.profileImages=profileImages;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_review_row, parent, false);
        return new TrainerProfileAdapter.MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TrainerProfileAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tvTraineeName.setText(reviews.get(position).getTraineeName());
        holder.tvReview.setText(reviews.get(position).getReview());
        holder.tvRating.setText(reviews.get(position).getRating() + "");
        String traineeId= reviews.get(position).getTraineeId();
        holder.tvTraineeImage.setImageBitmap(profileImages.get(traineeId));

    }

    @Override
    public int getItemCount() {
        return this.reviews.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTraineeName, tvReview, tvRating;
        CircularImageView tvTraineeImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTraineeName = itemView.findViewById(R.id.trainee_name);
            tvRating = itemView.findViewById(R.id.rating);
            tvReview = itemView.findViewById(R.id.trainee_review);
            tvTraineeImage = itemView.findViewById(R.id.trainee_image_review);
        }
    }
}
