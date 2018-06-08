package com.cybercom.passenger.service;

public class Constants {
    public interface ACTION {
        public static String MAIN = "com.nkdroid.alertdialog.action.main";
        public static String STARTFOREGROUND = "com.nkdroid.alertdialog.action.startforeground";
        public static String STOPFOREGROUND = "com.nkdroid.alertdialog.action.stopforeground";
        public static String STARTFOREGROUND_DRIVER_CLIENT = "UPDATE_DRIVER_POSITION";
        public static String STARTFOREGROUND_PASSENGER_CLIENT = "UPDATE_PASSENGER_POSITION";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }


}
