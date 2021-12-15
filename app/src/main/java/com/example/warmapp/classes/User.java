package com.example.warmapp.classes;

import java.util.HashMap;

public class User {
    private String firstName;
    private String lastName;
    private String mail;
    private String password;
    private String phone;
    private String userType;
    private HashMap<String,Boolean> requests;
    private HashMap<String,Boolean> trainings;


    public User(String firstName, String lastName, String mail, String password, String phone,String userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.password = password;
        this.phone = phone;
        this.userType = userType;
        requests= new HashMap<>();
        trainings=new HashMap<>();
    }
    public User(){}

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

    public String userType() {
        return userType;
    }

    public void setPhone(String phone) { this.phone = phone; }

    public HashMap<String, Boolean> getRequests() {
        return requests;
    }

    public void addRequest(String requestID){ this.requests.put(requestID,true); }

    public void removeRequest(Request request){ this.requests.remove(request); }

    public HashMap<String,Boolean> getTrainings() { return trainings; }

    public void addTraining(String trainingID) { this.trainings.put(trainingID,true); }
}