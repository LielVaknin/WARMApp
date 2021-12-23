package com.example.warmapp.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class SearchTrainerProfileAdapter extends RecyclerView.Adapter<SearchTrainerProfileAdapter.MyViewHolder> {

    Context context;
    ArrayList<UserTrainer> trainers;
    ArrayList<String> trainersId;

    public SearchTrainerProfileAdapter(Context context, ArrayList<UserTrainer> trainers, ArrayList<String> trainersId) {
        this.context = context;
        this.trainers = trainers;
        this.trainersId = trainersId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trainer_row, parent, false);
        return new SearchTrainerProfileAdapter.MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchTrainerProfileAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tvTrainerName.setText(trainers.get(position).getFirstName() + " " + trainers.get(position).getLastName());
        holder.tvDescription.setText(trainers.get(position).getDescription());
        holder.ratingNumber.setText(trainers.get(position).getRating() + "");
        holder.ratingBarTrainer.setRating(trainers.get(position).getRating());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference = storageReference.child(trainersId.get(position) + ".jpg");
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

        holder.trainerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(context, TrainerProfileActivity.class);
                myIntent.putExtra("trainerId", trainersId.get(position));
                myIntent.putExtra("firstName",trainers.get(position).getFirstName());
                myIntent.putExtra("lastName", trainers.get(position).getLastName());
                myIntent.putExtra("description", trainers.get(position).getDescription());
                myIntent.putExtra("phone", trainers.get(position).getPhone());
                myIntent.putExtra("mail", trainers.get(position).getMail());
                context.startActivity(myIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.trainers.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView trainerImage;
        TextView tvTrainerName, tvDescription, ratingNumber;
        RatingBar ratingBarTrainer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            trainerImage = itemView.findViewById(R.id.trainer_image);
            tvTrainerName = itemView.findViewById(R.id.trainer_name);
            tvDescription = itemView.findViewById(R.id.trainee_review);
            ratingNumber = itemView.findViewById(R.id.rating_av_number);
            ratingBarTrainer = itemView.findViewById(R.id.rating_trainer);
        }
    }
}
