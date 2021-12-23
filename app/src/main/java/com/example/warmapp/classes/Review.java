package com.example.warmapp.classes;

public class Review {

    float rating;
    String review;
    String traineeId;
    String traineeName;

    public Review(){}
    public Review(float rating, String review, String traineeId, String traineeName) {
        this.rating = rating;
        this.review = review;
        this.traineeId = traineeId;
        this.traineeName = traineeName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(String traineeId) {
        this.traineeId = traineeId;
    }

    public String getTraineeName() {
        return traineeName;
    }

    public void setTraineeName(String traineeName) {
        this.traineeName = traineeName;
    }
}
