package me.groupinfinity.chatucf;

public class Chatroom {
    String name;
    double latitude;
    double longitude;
    double distanceTo;

    public Chatroom(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}