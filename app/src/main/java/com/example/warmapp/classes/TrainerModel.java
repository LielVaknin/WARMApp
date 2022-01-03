package com.example.warmapp.classes;

import android.graphics.Bitmap;

public class TrainerModel {
    UserTrainer userTrainer;
    String trainerID;
    Bitmap trainerImage;

    public TrainerModel(UserTrainer userTrainer, String trainerID) {
        this.userTrainer = userTrainer;
        this.trainerID = trainerID;
    }

    public UserTrainer getUserTrainer() {
        return userTrainer;
    }

    public String getTrainerID() {
        return trainerID;
    }

    public Bitmap getTrainerImage() {
        return trainerImage;
    }

    public void setTrainerImage(Bitmap trainerImage) {
        this.trainerImage = trainerImage;
    }
}
