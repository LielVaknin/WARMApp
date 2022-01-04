package com.example.warmapp.adapters;

import com.example.warmapp.classes.TrainerModel;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;

import java.util.ArrayList;

public interface UpdateRVTrainingsByType {
    public void callback(int position, ArrayList<TrainingModel> trainings);
}
