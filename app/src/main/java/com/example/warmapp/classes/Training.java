package com.example.warmapp.classes;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

enum type{

};
enum feature{

};

public class Training {
    type title;
    String city;
    String address;
    ArrayList<feature> features;
    String trainerId;
    Time startTraining;
    Time endTraining;
    Date date;
    int price;
    String details;

    public Training(type title, String city, String address, String trainerId, Time startTraining, Time endTraining, Date date, int price) {
        this.title = title;
        this.city = city;
        this.address = address;
        this.trainerId = trainerId;
        this.startTraining = startTraining;
        this.endTraining = endTraining;
        this.date = date;
        this.price = price;
    }

    public type getTitle() {
        return title;
    }

    public void setTitle(type title) {
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

    public ArrayList<feature> getFeatures() {
        return features;
    }

    public void addFeature(feature feature){
        this.features.add(feature);
    }

    public void removeFeature(feature feature) {
        this.features.remove(feature);
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public Time getStartTraining() {
        return startTraining;
    }

    public void setStartTraining(Time startTraining) {
        this.startTraining = startTraining;
    }

    public Time getEndTraining() {
        return endTraining;
    }

    public void setEndTraining(Time endTraining) {
        this.endTraining = endTraining;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

}
