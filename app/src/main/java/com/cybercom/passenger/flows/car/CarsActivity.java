package com.cybercom.passenger.flows.car;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Car;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CarsActivity extends AppCompatActivity {

    private List<Car> mCarList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CarViewModel mCarViewModel;

    static final int CAR_DETAIL = 17;
    static final String CAR_NUMBER = "NUMBER";
    static final String CAR_MODEL = "MODEL";
    static final String CAR_YEAR = "YEAR";
    static final String CAR_COLOR = "COLOUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.FloatingActionButton_activitycars_add);
        fab.setOnClickListener(view -> openCarDetail());

        recyclerView = findViewById(R.id.RecyclerView_contentcars_carlist);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        recyclerView.addOnItemTouchListener(new CarTouchListener(getApplicationContext(),
                recyclerView, new CarTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Car car = mCarList.get(position);
                deleteConfirmDialog(car.getNumber());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        mCarViewModel = ViewModelProviders.of(this).get(CarViewModel.class);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        mCarViewModel.setUserId(extras.getString(getResources().getString(R.string.signup_userid)));

        mCarViewModel.getCarList().observe(this, (List<Car> carList) -> {
            mCarList = carList;
            CarsListAdapter carsListAdapter = new CarsListAdapter(mCarList);
            recyclerView.setAdapter(carsListAdapter);
        });


    }

    private void deleteConfirmDialog(String alertMsg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        String msg = getApplicationContext().getResources().getString(R.string.deleteConfirmation)
                        + " \n " + alertMsg ;
        alertDialogBuilder.setMessage(msg);
                alertDialogBuilder.setPositiveButton(getApplicationContext().getResources().getString(R.string.yes),
                        (arg0, arg1) -> {
                            mCarViewModel.deleteCar(alertMsg);
                            Timber.d("Deleting car details %s", alertMsg);
                        });

        alertDialogBuilder.setNegativeButton(getApplicationContext().getResources().getString(R.string.no),
                (dialog, which) -> {
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void openCarDetail() {
        Intent carDetailIntent = new Intent(this, CarDetailActivity.class);
        startActivityForResult(carDetailIntent, CAR_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAR_DETAIL) {
            Bundle extras = data.getExtras();
            if (extras == null) {
                Timber.e("No car values added");
                return;
            }
            mCarViewModel.addCar(extras.getString(CAR_NUMBER),
                    extras.getString(CAR_MODEL),
                    extras.getString(CAR_YEAR),
                    extras.getString(CAR_COLOR));
        }
    }
}
