package com.cybercom.passenger.repository.databasemodel.utils;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.utils.LocationHelper;

import java.util.Map;

public class DatabaseModelHelper {
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

    private static Drive convertViewModelDrive(Map<String, String> payload, User driver) {
        return new Drive(
                driver,
                Long.valueOf(payload.get("driveTime")),
                LocationHelper.getPositionFromString(
                        payload.get("driveStartLocationLatitude") +
                                "," + payload.get("driveStartLocationLongitude")),
                LocationHelper.getPositionFromString(
                        payload.get("driveEndLocationLatitude") +
                                "," + payload.get("driveEndLocationLongitude")),
                Integer.valueOf(payload.get("driveAvailableSeats"))
        );
    }

    private static DriveRequest convertViewModelDriveRequest(Map<String, String> payload, User passenger) {
        return new DriveRequest(
                passenger,
                Long.valueOf(payload.get("driveRequestTime")),
                LocationHelper.getPositionFromString(
                        payload.get("driveRequestStartLocationLatitude") +
                                "," + payload.get("driveRequestStartLocationLongitude")),
                LocationHelper.getPositionFromString(
                        payload.get("driveRequestEndLocationLatitude") +
                                "," + payload.get("driveRequestEndLocationLongitude")),
                Integer.valueOf(payload.get("driveRequestExtraPassengers")));
    }

    public static Notification convertPayloadToNotification(Map<String, String> payload, User driver, User passenger) {
        return new Notification(
                Integer.valueOf(payload.get("type")),
                convertViewModelDriveRequest(payload, passenger),
                convertViewModelDrive(payload, driver)
        );
    }
}
