package com.example.warmapp.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.warmapp.R;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.example.warmapp.classes.TypesTrainings;
import com.example.warmapp.classes.UserTrainer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

public class TypesTrainingsAdapter extends RecyclerView.Adapter<TypesTrainingsAdapter.TypesTrainingsHolder> {

    private Context context;
    private ArrayList<TypesTrainings> types;
    private int selectedType = -1;
    private UpdateRVTrainingsByType updateRVTrainingsByType;
    private boolean check = true;
    private boolean select = true;
    private String[] titles;
    private ArrayList<TrainingModel> typeTrainings;
    private ProgressDialog progressDialog;
    private MaterialCardView lastPosition;

    public TypesTrainingsAdapter(Context context, ArrayList<TypesTrainings> types, UpdateRVTrainingsByType updateRVTrainingsByType){
        this.context = context;
        this.types = types;
        this.updateRVTrainingsByType = updateRVTrainingsByType;
        this.lastPosition=null;

    }

    @NonNull
    @Override
    public TypesTrainingsAdapter.TypesTrainingsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_types_trainings, parent, false);
        return new TypesTrainingsAdapter.TypesTrainingsHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull TypesTrainingsAdapter.TypesTrainingsHolder holder, @SuppressLint("RecyclerView") int position) {
        typeTrainings = new ArrayList<>();
        titles = context.getResources().getStringArray(R.array.Titles);
        TypesTrainings currentType = types.get(position);

        holder.imageViewType.setImageResource(currentType.getImage());
        holder.textViewType.setText(currentType.getText());

        holder.materialCardView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(lastPosition!=null){
                    lastPosition.setStrokeWidth(0);

                }
                holder.materialCardView.setStrokeWidth(5);
                holder.materialCardView.setStrokeColor(R.color.teal_200);//not same color
                selectedType = holder.getAdapterPosition();
                notifyDataSetChanged();

                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Loading");
                progressDialog.show();

                typeTrainings.clear();
                getTypeTrainings(selectedType);

            }
        });

//        if (select){
//            if (position == 0){
//                holder.materialCardView.setBackgroundColor(R.color.white);
//                select = false;
//            }
//        }else {
//            if (selectedType == position){
//                holder.materialCardView.setBackgroundColor(R.color.white);
//            }else {
//                holder.materialCardView.setBackgroundColor(R.color.teal_200);
//            }
//        }

    }

    private void getTypeTrainings(int position) {
        typeTrainings.clear();
        FirebaseDatabase.getInstance().getReference("Trainings").orderByChild("title").equalTo(titles[position])
                .addListenerForSingleValueEvent(new ValueEventListener() {
            int countTrainings = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null){
                    progressDialog.dismiss();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    countTrainings++;
                    Training training = dataSnapshot.getValue(Training.class);
                    getTrainerName(Objects.requireNonNull(training),countTrainings,snapshot.getChildrenCount(),position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTrainerName(Training training, int countTrainings, long childrenCount, int position) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(training.getTrainerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserTrainer userTrainer = snapshot.getValue(UserTrainer.class);
                        setTrainingModel(training,userTrainer,countTrainings,childrenCount,position);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setTrainingModel(Training training, UserTrainer userTrainer, int countTrainings, long childrenCount, int position) {
        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference().child(training.getTrainerId() + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(byte[] bytes) {
                        String trainerName = userTrainer.getFirstName() + " " + userTrainer.getLastName();
                        Bitmap trainerImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        TrainingModel trainingModel = new TrainingModel(training,training.getTrainingID(),trainerName,trainerImage,"");
                        typeTrainings.add(trainingModel);
                        if (countTrainings == childrenCount){
                            progressDialog.dismiss();
                            updateRVTrainingsByType.callback(position,typeTrainings);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    public class TypesTrainingsHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewType;
        private TextView textViewType;
        private MaterialCardView materialCardView;

        public TypesTrainingsHolder(@NonNull View itemView) {
            super(itemView);

            imageViewType = itemView.findViewById(R.id.types_trainings_image_card_type);
            textViewType = itemView.findViewById(R.id.types_trainings_text_card_type);
            materialCardView = itemView.findViewById(R.id.types_trainings_material_card_view);
        }
    }
}
