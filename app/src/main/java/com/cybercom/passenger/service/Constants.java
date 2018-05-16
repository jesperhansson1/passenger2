package com.cybercom.passenger.service;

public class Constants {
    public interface ACTION {
        public static String MAIN = "com.nkdroid.alertdialog.action.main";
        public static String STARTFOREGROUND = "com.nkdroid.alertdialog.action.startforeground";
        public static String STOPFOREGROUND = "com.nkdroid.alertdialog.action.stopforeground";
        public static String STARTFOREGROUND_UPDATE_DRIVER_POSITION = "UPDATE_DRIVER_POSITION";
        public static String STARTFOREGROUND_UPDATE_PASSENGER_POSITION = "UPDATE_DRIVER_POSITION";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
