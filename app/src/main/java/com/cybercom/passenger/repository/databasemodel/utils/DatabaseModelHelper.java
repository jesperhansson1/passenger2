package com.cybercom.passenger.repository.databasemodel.utils;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.utils.LocationHelper;

import java.util.Map;

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

    public static com.cybercom.passenger.repository.databasemodel.Drive convertDrive(Drive drive) {

        return new com.cybercom.passenger.repository.databasemodel.Drive(drive.getDriver().getUserId(), drive.getTime(),
                drive.getStartLocation(), drive.getEndLocation(), drive.getAvailableSeats());
    }

    public static com.cybercom.passenger.repository.databasemodel.DriveRequest convertDriveRequest(DriveRequest driveRequest) {

        return new com.cybercom.passenger.repository.databasemodel.DriveRequest(driveRequest.getPassenger().getUserId(), driveRequest.getTime(),
                driveRequest.getStartLocation(), driveRequest.getEndLocation(), driveRequest.getExtraPassengers());
    }

    public static com.cybercom.passenger.repository.databasemodel.Notification convertNotification(Notification notification) {
        com.cybercom.passenger.repository.databasemodel.Drive firebaseDrive = convertDrive(notification.getDrive());
        com.cybercom.passenger.repository.databasemodel.DriveRequest firebaseDriveRequest = convertDriveRequest(notification.getDriveRequest());

        return new com.cybercom.passenger.repository.databasemodel.Notification(notification.getType(), firebaseDriveRequest, firebaseDrive);
    }

    private static Drive convertToViewModelDrive(Map<String, String> payload, User driver) {
        return new Drive(
                driver,
                Long.valueOf(payload.get(DRIVE_TIME)),
                LocationHelper.getPositionFromString(
                        payload.get(DRIVE_START_LOCATION_LATITUDE) +
                                "," + payload.get(DRIVE_START_LOCATION_LONGITUDE)),
                LocationHelper.getPositionFromString(
                        payload.get(DRIVE_END_LOCATION_LATITUDE) +
                                "," + payload.get(DRIVE_END_LOCATION_LONGITUDE)),
                Integer.valueOf(payload.get(DRIVE_AVAILABLE_SEATS))
        );
    }

    private static DriveRequest convertToViewModelDriveRequest(Map<String, String> payload, User passenger) {
        return new DriveRequest(
                passenger,
                Long.valueOf(payload.get(DRIVE_REQUEST_TIME)),
                LocationHelper.getPositionFromString(
                        payload.get(DRIVE_REQUEST_START_LOCATION_LATITUDE) +
                                "," + payload.get(DRIVE_REQUEST_START_LOCATION_LONGITUDE)),
                LocationHelper.getPositionFromString(
                        payload.get(DRIVE_REQUEST_END_LOCATION_LATITUDE) +
                                "," + payload.get(DRIVE_REQUEST_END_LOCATION_LONGITUDE)),
                Integer.valueOf(payload.get(DRIVE_REQUEST_EXTRA_PASSENGERS)));
    }

    public static Notification convertPayloadToNotification(Map<String, String> payload, User driver, User passenger) {
        return new Notification(
                Integer.valueOf(payload.get(TYPE)),
                convertToViewModelDriveRequest(payload, passenger),
                convertToViewModelDrive(payload, driver)
        );
    }
}
