package com.cybercom.passenger.flows.car;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.cybercom.passenger.model.Car;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CarViewModel extends AndroidViewModel {

    DatabaseReference mCarRef;
    String mUserId;

    public CarViewModel(@NonNull Application application) {
        super(application);
        mCarRef = (PassengerRepository.getInstance()).getCarsReference();
        mUserId = "CAFpHVaBPSed9RiwVPYPlamYkrb2";//FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    public void addCar(String number, String model, int year, String colour)
    {
        String carId = mCarRef.push().getKey();

        Car car = new Car(number,model,year,colour);
        mCarRef.child(mUserId).child(carId).setValue(car);
    }

    public LiveData<Car> getCars() {

        final MutableLiveData<Car> car = new MutableLiveData<>();
        if (mCarRef != null) {
            mCarRef.child(mUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   // user.setValue(dataSnapshot.getValue(Car.class));
                    System.out.println(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        } else {
            // TODO: Not logged in...
            return null;
        }
        return car;
    }

}
