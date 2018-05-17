package com.cybercom.passenger.route;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class ParserTask extends AsyncTask<String, Integer, List<Route>> {

    private static final int ROUTE_WIDTH = 10;
    private static final int ROUTE_COLOR = Color.rgb(6, 182, 239);

    public interface OnRouteCompletion{
        void onRouteDrawn(Polyline route);
    }

    private OnRouteCompletion mDelegate;

    private GoogleMap mGoogleMap;

    ParserTask(GoogleMap googleMap, OnRouteCompletion caller) {
        mGoogleMap = googleMap;
        mDelegate = caller;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<Route> doInBackground(String... jsonData) {

        JSONObject jsonObject;
        List<Route> listRoutes = null;

        try {
            jsonObject = new JSONObject(jsonData[0]);
            Timber.d(jsonData[0]);
            DataParser parser = new DataParser();
            Timber.d(parser.toString());

            // Starts parsing data
            listRoutes = parser.parse(jsonObject);
            Timber.d("Executing routes");

        } catch (Exception exception) {
            Timber.e(exception.getLocalizedMessage());
        }
        return listRoutes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<Route> routeList) {
        ArrayList<LatLng> points = new ArrayList<>();

        // Traversing through all the routes
        for (Route route : routeList) {
            Legs legs = route.getLegs();
            List<Steps> stepsList = legs.getStepsList();

            for (Steps steps : stepsList) {
                List<HashMap<String,String>> pointsList = steps.getPointsList();
                for(HashMap<String, String> pointMap : pointsList) {
                    double latitude = Double.parseDouble(pointMap.get("lat"));
                    double longitude = Double.parseDouble(pointMap.get("lng"));
                    LatLng latLngPosition = new LatLng(latitude, longitude);
                    points.add(latLngPosition);
                }
            }
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(ROUTE_WIDTH);
        polylineOptions.color(ROUTE_COLOR);

        polylineOptions.addAll(points);

        // TODO This should not be done here..
        Timber.d(String.valueOf(polylineOptions));
        if (!polylineOptions.getPoints().isEmpty() && mGoogleMap != null) {
            Polyline route = mGoogleMap.addPolyline(polylineOptions);
            mDelegate.onRouteDrawn(route);
        }
        else {
            Timber.d("without Polylines drawn");
        }
    }
}
