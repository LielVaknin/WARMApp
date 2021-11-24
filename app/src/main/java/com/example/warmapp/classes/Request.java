package com.example.warmapp.classes;

import com.example.warmapp.classes.Training;

enum paymentMethod{
    card,
    other
}

public class Request {
    String traineeID;
    Training training;
    boolean isConfirm = false;
    paymentMethod paymentMethod;

    public Request(String traineeID, Training training) {
        this.traineeID = traineeID;
        this.training = training;
    }

    public String getTraineeID() {
        return traineeID;
    }

    public Training getTraining() {
        return training;
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public void setConfirm(boolean confirm) {
        isConfirm = confirm;
    }

    public paymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(paymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}