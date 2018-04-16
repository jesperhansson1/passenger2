package com.cybercom.passenger.flows.car;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Car;

import java.util.List;

public class CarsListAdapter extends RecyclerView.Adapter<CarsListAdapter.MyViewHolder> {

    private List<Car> mCarList;
    Context mContext;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView carNumber;

        public MyViewHolder(View view) {
            super(view);
            carNumber = (TextView) view.findViewById(R.id.textView_carlistrow_number);
        }
    }

    public CarsListAdapter(List<Car> carsList) {
        mCarList = carsList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Car car = mCarList.get(position);
        holder.carNumber.setText(car.getNumber());
    }

    @Override
    public int getItemCount() {
        return mCarList.size();
    }
}