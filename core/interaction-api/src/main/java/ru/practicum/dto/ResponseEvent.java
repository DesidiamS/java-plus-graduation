package ru.practicum.dto;

public interface ResponseEvent {
    void setConfirmedRequests(int confirmedRequests);

    int getConfirmedRequests();

    void setViews(long views);

    long getViews();
}
