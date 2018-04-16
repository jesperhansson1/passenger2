package com.cybercom.passenger.flows.car;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Car;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarsActivity extends AppCompatActivity {

    private List<Car> carList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CarsListAdapter mCarsListAdapter;
    CarViewModel mCarViewModel;

    final int CAR_DETAIL = 17;
    final String CAR_NUMBER = "NUMBER";
    final String CAR_MODEL = "MODEL";
    final String CAR_YEAR = "YEAR";
    final String CAR_COLOR = "COLOUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FloatingActionButton_activitycars_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCarDetail();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.RecyclerView_contentcars_carlist);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        recyclerView.addOnItemTouchListener(new CarTouchListener(getApplicationContext(), recyclerView, new CarTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Car car = carList.get(position);
                System.out.println(car.getNumber());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        mCarViewModel = ViewModelProviders.of(this).get(CarViewModel.class);

        mCarViewModel.getCarList().observe(this, new List<Car>()(List<Car> fruitlist) -> {
            // update UI
            CarsListAdapter carsListAdapter = new CarsListAdapter(fruitlist);
            // Assign adapter to ListView
            System.out.println(carsListAdapter.getItemCount());
            //ArrayAdapter adapter = new ArrayAdapter()
            recyclerView.setAdapter(ap);
        });
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
                return;
            }
            mCarViewModel.addCar(extras.getString("CAR_NUMBER"),
                    extras.getString("CAR_MODEL"),
                    extras.getString("CAR_YEAR"),
                    extras.getString("CAR_COLOR"));
        }
    }
}
