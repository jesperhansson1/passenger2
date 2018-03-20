package com.cybercom.passenger.flows.createdrive;

import android.arch.lifecycle.ViewModel;

import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;
import com.cybercom.passenger.model.Position;
import com.cybercom.passenger.repository.PassengerRepository;

public class CreateRideViewModel extends ViewModel {

    public Position getPosition(String locationValue)
    {
        double lat,lang;
        String[] locArr = locationValue.split(",");
        lat = Double.valueOf(locArr[0]);
        lang = Double.valueOf(locArr[1]);
        return new Position(lat,lang);
    }

    public void createRide(String ride, Position startLocation, Position endLocation){
        Long currentTimeMillis = System.currentTimeMillis();
        int seats = 1;

        if (ride.matches("Create Drive")){
            Drive drive = new Drive(currentTimeMillis,startLocation,endLocation,String.valueOf(currentTimeMillis),seats);
            (PassengerRepository.getInstance()).addDrive(drive);
         }
        if(ride.matches("Request")){
            DriveRequest driveRequest = new DriveRequest(currentTimeMillis,startLocation,endLocation,String.valueOf(currentTimeMillis),seats);
            (PassengerRepository.getInstance()).addDriveRequest(driveRequest);
        }
    }

}
