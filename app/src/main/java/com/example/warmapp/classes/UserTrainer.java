package com.example.warmapp.classes;

import java.io.File;
import java.util.ArrayList;

public class UserTrainer extends User{
    ArrayList<File> diplomas;

    public UserTrainer(String uid, String firstName, String lastName, String mail, String password, String phone) {
        super(uid, firstName, lastName, mail, password, phone);
    }

    public void removeTraining(Training training){}
    public void confirmRequest(Request request){}
    public void rejectRequest(Request request){}
    public void addDiploma(File file){}
    public void removeDiploma(File file){}
    public void sendNotification(String message,Request request){}
}
