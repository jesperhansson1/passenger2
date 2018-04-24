package com.cybercom.passenger.route;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    public static final int ROUTE_WIDTH = 10;
    public static final int ROUTE_COLOR = Color.rgb(6, 182, 239);

    public interface OnRouteCompletion{
        void onRouteDrawn(Polyline route);
    }

    public OnRouteCompletion delegate = null;

    private GoogleMap mGoogleMap;
    ParserTask(GoogleMap googleMap, OnRouteCompletion caller)
    {
        mGoogleMap = googleMap;
        delegate = caller;
    }
    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jsonObject;
        List<List<HashMap<String, String>>> listRoutes = null;

        try {
            jsonObject = new JSONObject(jsonData[0]);
            Timber.d(jsonData[0]);
            DataParser parser = new DataParser();
            Timber.d(parser.toString());

            // Starts parsing data
            listRoutes = parser.parse(jsonObject);
            Timber.d("Executing routes");
            Timber.d(listRoutes.toString());

        } catch (Exception exception) {
            Timber.e(exception.getLocalizedMessage());
        }
        return listRoutes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> arraylistPoints;
        PolylineOptions polylineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            arraylistPoints = new ArrayList<>();
            polylineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> listPath = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < listPath.size(); j++) {
                HashMap<String, String> hashmapPoint = listPath.get(j);

                double latitude = Double.parseDouble(hashmapPoint.get("lat"));
                double langitude = Double.parseDouble(hashmapPoint.get("lng"));
                LatLng latLngPosition = new LatLng(latitude, langitude);

                arraylistPoints.add(latLngPosition);
            }

            // Adding all the points in the route to LineOptions
            polylineOptions.addAll(arraylistPoints);
            polylineOptions.width(ROUTE_WIDTH);
            polylineOptions.color(ROUTE_COLOR);

            Timber.d("onPostExecute lineoptions decoded");
        }

        // Drawing polyline in the Google Map for the i-th route
        if(polylineOptions != null) {
            Timber.d(String.valueOf(polylineOptions));
            if(mGoogleMap!=null) {
                Polyline route = mGoogleMap.addPolyline(polylineOptions);
                delegate.onRouteDrawn(route);
            }
        }
        else {
            Timber.d("without Polylines drawn");
        }
    }
}
