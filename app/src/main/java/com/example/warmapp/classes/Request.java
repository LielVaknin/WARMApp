package com.example.warmapp.classes;

import com.example.warmapp.classes.Training;



public class Request{

    String traineeID;
    String trainingID;
    boolean isConfirm = false;
    String paymentMethod;

    public Request(String traineeID, String trainingID) {
        this.traineeID = traineeID;
        this.trainingID = trainingID;
    }

    public Request(){}
    public String getTraineeID() {
        return traineeID;
    }

    public String getTrainingID() {
        return trainingID;
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public void setConfirm(boolean confirm) {
        isConfirm = confirm;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}