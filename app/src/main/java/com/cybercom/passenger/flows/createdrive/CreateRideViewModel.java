package com.cybercom.passenger.flows.createdrive;

import android.arch.lifecycle.ViewModel;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;

public class CreateRideViewModel extends ViewModel {

    public void createRide(int type, Position startLocation, Position endLocation){
        long currentTimeMillis = System.currentTimeMillis();
        int seats = 1;
        PassengerRepository repo = (PassengerRepository.getInstance());

        User driver = new User("user-id", "notification-token", User.TYPE_DRIVER, "040-88 88 88", "personalnumber" , "fullname", "image-link", "gender");

        Drive drive = new Drive(driver, currentTimeMillis, startLocation, endLocation, seats);

        if (type == CreateRideDialogFragment.TYPE_RIDE) {
            repo.addDrive(drive);
         }
        if(type == CreateRideDialogFragment.TYPE_REQUEST){
            DriveRequest driveRequest = new DriveRequest(PassengerRepository.gPassenger, currentTimeMillis, startLocation, endLocation, seats);
            repo.addDriveRequest(driveRequest);
        }
    }
}
