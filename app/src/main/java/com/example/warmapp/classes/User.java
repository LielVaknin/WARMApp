package com.example.warmapp.classes;

import java.util.ArrayList;

public class User {
    private String uniqueID;
    private String firstName;
    private String lastName;
    private String mail;
    private String password;
    private String phone;
    private ArrayList<Request> requests;
    private ArrayList<Training> trainings;

    public User(String uniqueid, String firstName, String lastName, String mail, String password, String phone) {
        this.uniqueID = uniqueid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.password = password;
        this.phone = phone;
    }
    public String getUniqueID() { return uniqueID; }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) { this.phone = phone; }

    public void addRequest(Request request){ this.requests.add(request); }

    public void removeRequest(Request request){ this.requests.remove(request); }

    public ArrayList<Training> getTrainings() { return trainings; }

    public void addTraining(Training training) { this.trainings.add(training); }
}
