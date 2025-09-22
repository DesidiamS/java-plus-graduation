package ru.practicum.dto;

public interface ResponseEvent {
    void setConfirmedRequests(int confirmedRequests);

    int getConfirmedRequests();

    void setRating(double rating);

    double getRating();
}
