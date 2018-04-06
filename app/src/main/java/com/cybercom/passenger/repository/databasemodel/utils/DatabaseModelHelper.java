package com.cybercom.passenger.repository.databasemodel.utils;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Notification;

public class DatabaseModelHelper {
    public static com.cybercom.passenger.repository.databasemodel.Drive convertDrive(Drive drive) {

        return new com.cybercom.passenger.repository.databasemodel.Drive(drive.getDriver().getUserIdId(), drive.getTime(),
                drive.getStartLocation(), drive.getEndLocation(), drive.getAvailableSeats());
    }

    public static com.cybercom.passenger.repository.databasemodel.DriveRequest convertDriveRequest(DriveRequest driveRequest) {

        return new com.cybercom.passenger.repository.databasemodel.DriveRequest(driveRequest.getPassenger().getUserIdId(), driveRequest.getTime(),
                driveRequest.getStartLocation(), driveRequest.getEndLocation(), driveRequest.getExtraPassengers());
    }

    public static com.cybercom.passenger.repository.databasemodel.Notification convertNotification(Notification notification) {
        com.cybercom.passenger.repository.databasemodel.Drive firebaseDrive = convertDrive(notification.getDrive());
        com.cybercom.passenger.repository.databasemodel.DriveRequest firebaseDriveRequest = convertDriveRequest(notification.getDriveRequest());

        return new com.cybercom.passenger.repository.databasemodel.Notification(notification.getType(), firebaseDriveRequest, firebaseDrive);
    }
}
