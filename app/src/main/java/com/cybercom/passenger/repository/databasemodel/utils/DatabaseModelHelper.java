package com.cybercom.passenger.repository.databasemodel.utils;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;

import java.util.ArrayList;

public class DatabaseModelHelper {

    private static final String DRIVE_TIME = "driveTime";
    private static final String DRIVE_START_LOCATION_LATITUDE = "driveStartLocationLatitude";
    private static final String DRIVE_START_LOCATION_LONGITUDE = "driveStartLocationLongitude";
    private static final String DRIVE_END_LOCATION_LATITUDE = "driveEndLocationLatitude";
    private static final String DRIVE_END_LOCATION_LONGITUDE = "driveEndLocationLongitude";
    private static final String DRIVE_AVAILABLE_SEATS = "driveAvailableSeats";
    private static final String DRIVE_REQUEST_TIME = "driveRequestTime";
    private static final String DRIVE_REQUEST_START_LOCATION_LATITUDE = "driveRequestStartLocationLatitude";
    private static final String DRIVE_REQUEST_START_LOCATION_LONGITUDE = "driveRequestStartLocationLongitude";
    private static final String DRIVE_REQUEST_END_LOCATION_LATITUDE = "driveRequestEndLocationLatitude";
    private static final String DRIVE_REQUEST_END_LOCATION_LONGITUDE = "driveRequestEndLocationLongitude";
    private static final String DRIVE_REQUEST_EXTRA_PASSENGERS = "driveRequestExtraPassengers";
    private static final String TYPE = "type";

    public static com.cybercom.passenger.repository.databasemodel.Drive convertToDataBaseDrive(Drive drive) {

        return new com.cybercom.passenger.repository.databasemodel.Drive(drive.getDriver().getUserId(), drive.getTime(),
                drive.getStartLocation(), drive.getEndLocation(), drive.getAvailableSeats(), drive.getCurrentPosition(), drive.getCurrentVelocity() );
    }

    public static com.cybercom.passenger.repository.databasemodel.DriveRequest convertDriveRequest(DriveRequest driveRequest) {

        return new com.cybercom.passenger.repository.databasemodel.DriveRequest(driveRequest.getPassenger().getUserId(),
                driveRequest.getTime(), driveRequest.getStartLocation(),
                driveRequest.getEndLocation(), driveRequest.getExtraPassengers(),driveRequest.getPrice(),driveRequest.getChargeId(),
                new ArrayList<>(), driveRequest.getDistance());
    }

    public static com.cybercom.passenger.repository.databasemodel.Notification convertNotification(
            Notification notification) {
        return new com.cybercom.passenger.repository.databasemodel.Notification(
                notification.getType(), notification.getDriveRequest().getId(),
                notification.getDrive().getId());
    }

}
