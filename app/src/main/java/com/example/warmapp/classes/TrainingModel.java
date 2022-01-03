package com.example.warmapp.classes;

import android.graphics.Bitmap;

public class TrainingModel {
    private final Training training;
    private final String trainingID;
    private final String trainerName;
    private final Bitmap trainerImage;
    private final String trainingStatus;

    public TrainingModel(Training training, String trainingID, String trainerName, Bitmap trainerImage, String trainingStatus) {
        this.training = training;
        this.trainingID = trainingID;
        this.trainerName = trainerName;
        this.trainerImage = trainerImage;
        this.trainingStatus = trainingStatus;
    }

    public Training getTraining() {
        return training;
    }

    public String getTrainingID() {
        return trainingID;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public Bitmap getTrainerImage() {
        return trainerImage;
    }

    public String getTrainingStatus() {
        return trainingStatus;
    }
}