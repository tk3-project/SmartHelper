package com.g15.smarthelper;

public class Constants {

    // Activity Recognition
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 5 * 1000; // 5 seconds
    public static final int CONFIDENCE = 70;

    // Location
    public static final String BROADCAST_DETECTED_LOCATION = "location_intent";
    public static final long UPDATE_INTERVAL = 20000; // 20 sec
    public static final long FASTEST_UPDATE_INTERVAL = 10000; // 10 sec
    public static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 4; // 80 sec
}