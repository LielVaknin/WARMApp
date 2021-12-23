package com.example.warmapp.classes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class UserTrainer extends User {
    ArrayList<File> diplomas;
    float rating;
    String description;
    HashMap<String,Boolean> reviewsMap;

    public UserTrainer(String firstName, String lastName, String mail, String password, String phone, String userType) {
        super(firstName, lastName, mail, password, phone, userType);
    }

    public UserTrainer() {
    }

    public void removeTraining(Training training) {
    }

    public void confirmRequest(Request request) {
    }

    public void rejectRequest(Request request) {
    }

    public void addDiploma(File file) {
    }

    public void removeDiploma(File file) {
    }

    public void sendNotification(String message, Request request) {
    }

    public float getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}