package com.cybercom.passenger.flows.car;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.cybercom.passenger.model.Car;
import com.cybercom.passenger.repository.PassengerRepository;
import java.util.List;

import static com.cybercom.passenger.utils.CarNumberHelper.getKeyFromNumber;

class CarViewModel extends AndroidViewModel {
    private String mUserId;
    private MutableLiveData<List<Car>> mCarList;
    private final PassengerRepository repository = PassengerRepository.getInstance();


    public CarViewModel(@NonNull Application application) {
        super(application);
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

    public void setUserId(String userId) {
        mUserId = userId;
    }

    LiveData<List<Car>> getCarList() {
        mCarList = repository.getUpdatedCarList();
        return mCarList;
    }


}