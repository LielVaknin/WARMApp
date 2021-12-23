package com.example.warmapp.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.warmapp.R;


import java.util.ArrayList;

public class TrainerProfileAdapter extends RecyclerView.Adapter<TrainerProfileAdapter.MyViewHolder> {

    Context context;
    ArrayList<Review> reviews;

    public TrainerProfileAdapter(Context context, ArrayList<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.review_row, parent, false);
        return new TrainerProfileAdapter.MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TrainerProfileAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tvTraineeName.setText(reviews.get(position).getTraineeName());
        holder.tvReview.setText(reviews.get(position).getReview());
        holder.tvRating.setText(reviews.get(position).getRating() + "");
    }

    @Override
    public int getItemCount() {
        return this.reviews.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTraineeName, tvReview, tvRating;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTraineeName = itemView.findViewById(R.id.trainee_name);
            tvRating = itemView.findViewById(R.id.rating);
            tvReview = itemView.findViewById(R.id.trainee_review);
        }
    }
}
