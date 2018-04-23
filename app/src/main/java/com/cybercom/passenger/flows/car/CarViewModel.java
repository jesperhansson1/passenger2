package com.cybercom.passenger.flows.car;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.cybercom.passenger.model.Car;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.cybercom.passenger.utils.CarNumberHelper.getKeyFromNumber;

public class CarViewModel extends AndroidViewModel {
    String mUserId;
    List<Car> mAllCar;
    private MutableLiveData<List<Car>> mCarList;
    private PassengerRepository repository = PassengerRepository.getInstance();


    public CarViewModel(@NonNull Application application) {
        super(application);
       // mUserId = "CAFpHVaBPSed9RiwVPYPlamYkrb2";
        mCarList = new MutableLiveData<>();
        repository.onChangeCarDetails();
    }

    public void addCar(String number, String model, String year, String colour)
    {
        Car car = new Car(number,model,year,colour);
        repository.createCar(getKeyFromNumber(number),mUserId, car);
    }

    public void deleteCar(String number)
    {
        repository.removeCar(getKeyFromNumber(number),mUserId);
    }

    public List<Car> getCars() {
        return this.mAllCar;
    }

    LiveData<List<Car>> getCarList() {
        mCarList = repository.getUpdatedCarList();
        return mCarList;
    }
}