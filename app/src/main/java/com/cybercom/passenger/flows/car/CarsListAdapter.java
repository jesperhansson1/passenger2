package com.cybercom.passenger.flows.car;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Car;
import java.util.List;

import timber.log.Timber;

public class CarsListAdapter extends RecyclerView.Adapter<CarsListAdapter.MyViewHolder> {

    private final List<Car> mCarList;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_list_row, parent, false);
        return new MyViewHolder(itemView);
    }


    public CarsListAdapter(List<Car> carsList) {
        mCarList = carsList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Car car = mCarList.get(position);
        holder.carNumber.setText(car.getNumber());
        holder.carModel.setText(car.getModel());
        holder.carYear.setText(car.getYear());
        holder.carColor.setText(car.getColor());

    }

    @Override
    public int getItemCount() {
        Timber.i("No. of cars %s", mCarList.size());
        return mCarList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView carNumber;
        private final TextView carModel;
        private final TextView carYear;
        private final TextView carColor;

        MyViewHolder(View view) {
            super(view);
            carNumber = view.findViewById(R.id.textView_carlistrow_number);
            carModel = view.findViewById(R.id.textView_carlistrow_model);
            carYear = view.findViewById(R.id.textView_carlistrow_year);
            carColor = view.findViewById(R.id.textView_carlistrow_color);
        }
    }

}