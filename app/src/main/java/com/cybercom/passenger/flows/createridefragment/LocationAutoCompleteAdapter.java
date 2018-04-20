package com.cybercom.passenger.flows.createridefragment;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LocationAutoCompleteAdapter extends ArrayAdapter<AutocompletePrediction> implements Filterable {
    private static final String TAG = "PlaceAutocompleteAdapter";
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private ArrayList<AutocompletePrediction> mResultList;
    private GeoDataClient mGeoDataClient;

    private LatLngBounds mBounds;

    private AutocompleteFilter mPlaceFilter;

    public LocationAutoCompleteAdapter(Context context, GeoDataClient geoDataClient,
                                    LatLngBounds bounds, AutocompleteFilter filter) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
        mGeoDataClient = geoDataClient;
        mBounds = bounds;
        mPlaceFilter = filter;
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }


    @Override
    public AutocompletePrediction getItem(int position) {
        return mResultList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        AutocompletePrediction item = getItem(position);

        TextView textView1 = (TextView) row.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) row.findViewById(android.R.id.text2);
        textView1.setText(item.getPrimaryText(STYLE_BOLD));
        textView2.setText(item.getSecondaryText(STYLE_BOLD));

        return row;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                if (constraint != null) {

                    filterData = getAutocomplete(constraint);
                }

                results.values = filterData;
                if (filterData != null) {
                    results.count = filterData.size();
                } else {
                    results.count = 0;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (results != null && results.count > 0) {
                    mResultList = (ArrayList<AutocompletePrediction>) results.values;
                    notifyDataSetChanged();
                } else {

                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {

        Task<AutocompletePredictionBufferResponse> results =
                mGeoDataClient.getAutocompletePredictions(constraint.toString(), mBounds,
                        mPlaceFilter);

        try {
            Tasks.await(results, 60, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        try {
            AutocompletePredictionBufferResponse autocompletePredictions = results.getResult();

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        } catch (RuntimeExecutionException e) {
            Toast.makeText(getContext(), "Error contacting API: " + e.toString(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error getting autocomplete prediction API call", e);
            return null;
        }
    }
}
