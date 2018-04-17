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

    DatabaseReference mCarRef;
    String mUserId;
    List<Car> mAllCar;
    private MutableLiveData<List<Car>> mCarList;


    public CarViewModel(@NonNull Application application) {
        super(application);
        mCarRef = (PassengerRepository.getInstance()).getCarsReference();
        mUserId = "CAFpHVaBPSed9RiwVPYPlamYkrb2";//FirebaseAuth.getInstance().getCurrentUser().getUid();
        mCarList = new MutableLiveData<>();
        mCarRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                taskDeletion(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void addCar(String number, String model, String year, String colour)
    {
        Car car = new Car(number,model,year,colour);
        mCarRef.child(mUserId).child(getKeyFromNumber(number)).setValue(car);
    }

    public void deleteCar(String number){
        mCarRef.child(mUserId).child(getKeyFromNumber(number)).removeValue();
    }

    public List<Car> getCars() {
        return this.mAllCar;
    }

    private void getAllTask(DataSnapshot dataSnapshot){
        mAllCar = new ArrayList<Car>();
        Map<String, Object> objectMap = (HashMap<String, Object>)
                dataSnapshot.getValue();
        if(objectMap.values()!= null)
        for (Object obj : objectMap.values()) {
            if (obj instanceof Map) {
                Map<String, Object> mapObj = (Map<String, Object>) obj;
                try
                {
                    Car match = new Car(mapObj.get("number").toString(),
                            mapObj.get("model").toString(),
                            mapObj.get("year").toString(),
                            mapObj.get("color").toString());
                    mAllCar.add(match);
                }
                catch(Exception e)
                {
                    Timber.e(e.getLocalizedMessage());
                }
            }
        }
         mCarList.setValue(mAllCar);
    }

    public void taskDeletion(DataSnapshot dataSnapshot){
        getAllTask(dataSnapshot);
    }

    LiveData<List<Car>> getCarList() {
        return mCarList;
    }

}