package com.g15.smarthelper;

public class Constants {

    public static final String PACKAGE_NAME = "com.g15.smarthelper";
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // Activity Recognition
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 5 * 1000; // 5 seconds
    public static final int CONFIDENCE = 70;

    // Location
    public static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    public static final String LOCATION_ADDRESS_KEY = "location-address";
    public static final long UPDATE_INTERVAL = 30000; // 30 sec
    public static final long FASTEST_UPDATE_INTERVAL = 10000; // 10 sec
    public static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 4; // 2 min
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

}