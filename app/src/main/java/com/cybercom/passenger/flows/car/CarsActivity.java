package com.cybercom.passenger.flows.car;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

public class CarsActivity extends AppCompatActivity {

    private List<Car> carList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CarsListAdapter mCarsListAdapter;


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

        mCarsListAdapter = new CarsListAdapter(carList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mCarsListAdapter);

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

        prepareMovieData();
    }

    private void prepareMovieData() {
        String colour = "#" + Integer.toHexString(ContextCompat.getColor
                (getApplicationContext(), R.color.colorBlue) & 0x00ffffff);

         Car car = new Car("ABC 124", "Volvo v60", 2018,
                colour);
        carList.add(car);
    }

    public void openCarDetail()
    {
        Intent carDetailIntent = new Intent(this, CarDetailActivity.class);
        startActivity(carDetailIntent);
    }


}
