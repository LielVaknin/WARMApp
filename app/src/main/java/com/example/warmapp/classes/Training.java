package com.example.warmapp.classes;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Training implements Serializable {


    String trainingID;
    String title;
    String city;
    String address;
    HashMap<String,String> features;
    String trainerId;
    String startTraining;
    String endTraining;
    String date;
    int price;
    String details;
    int maxParticipants;
    HashMap<String,Boolean> participants;


    public Training(){}
    public Training(String trainingID, String title, String city, String address, String trainerId, String startTraining, String endTraining, String date, int price) {
        this.trainingID=trainingID;
        this.title = title;
        this.city = city;
        this.address = address;
        this.trainerId = trainerId;
        this.startTraining = startTraining;
        this.endTraining = endTraining;
        this.date = date;
        this.price = price;
        this.participants = new HashMap<>();
    }
    public String getTrainingID() {
        return trainingID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public HashMap<String, String> getFeatures() {
        return features;
    }

    public void addFeature(String feature){
        this.features.put(feature,feature);
    }

    public void removeFeature(String feature) {
        this.features.remove(feature);
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public String getStartTraining() {
        return startTraining;
    }

    public void setStartTraining(String startTraining) {
        this.startTraining = startTraining;
    }

    public String getEndTraining() {
        return endTraining;
    }

    public void setEndTraining(String endTraining) {
        this.endTraining = endTraining;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }


    @Override
    public String toString() {
        return "Training{" +
                "title='" + title + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", features=" + features +
                ", trainerId='" + trainerId + '\'' +
                ", startTraining='" + startTraining + '\'' +
                ", endTraining='" + endTraining + '\'' +
                ", date='" + date + '\'' +
                ", price=" + price +
                ", details='" + details + '\'' +
                '}';
    }
}