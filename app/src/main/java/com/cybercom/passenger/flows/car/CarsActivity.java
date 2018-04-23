package com.cybercom.passenger.flows.car;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
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
    private CarsListAdapter mCarsListAdapter;
    CarViewModel mCarViewModel;

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCarDetail();
            }
        });

        recyclerView = findViewById(R.id.RecyclerView_contentcars_carlist);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        recyclerView.addOnItemTouchListener(new CarTouchListener(getApplicationContext(), recyclerView, new CarTouchListener.ClickListener() {
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
        mCarViewModel.mUserId = extras.getString(getResources().getString(R.string.signup_userid));

        mCarViewModel.getCarList().observe(this, (List<Car> carList) -> {
            mCarList = carList;
            CarsListAdapter carsListAdapter = new CarsListAdapter(mCarList);
            recyclerView.setAdapter(carsListAdapter);
        });


    }

    public void deleteConfirmDialog(String alertMsg){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        String msg = getApplicationContext().getResources().getString(R.string.deleteConfirmation)
                        + " \n " + alertMsg ;
        alertDialogBuilder.setMessage(msg);
                alertDialogBuilder.setPositiveButton(getApplicationContext().getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mCarViewModel.deleteCar(alertMsg);
                                Timber.d("Deleting car details %s", alertMsg);
                            }
                        });

        alertDialogBuilder.setNegativeButton(getApplicationContext().getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void openCarDetail()
    {
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
            }
            mCarViewModel.addCar(extras.getString(CAR_NUMBER),
                    extras.getString(CAR_MODEL),
                    extras.getString(CAR_YEAR),
                    extras.getString(CAR_COLOR));
        }
    }


   /* public void addIntentValues(Intent intent, String type)
    {
        if (mExtras != null) {
            // intent.putExtra("extraBundle",mExtras);
            intent.putExtra(BankCard,type);
            intent.putExtra("email",mExtras.getString("email"));
            intent.putExtra("password",mExtras.getString("password"));
            intent.putExtra("phone",mExtras.getString("phone"));
            intent.putExtra("personalnumber",mExtras.getString("personalnumber"));
            intent.putExtra("fullname",mExtras.getString("fullname"));
            intent.putExtra("gender", mExtras.getString("gender"));
        }
        else
        {
            Timber.e("Error getting values");
        }
    }*/
}
