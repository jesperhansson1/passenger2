package com.cybercom.passenger.route;

import android.graphics.Color;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ParserTask extends AsyncTask<String, Integer, List<Route>> {

    public interface OnRouteCompletion{
        void onRoutesFetched(List<Route> routes);
    }

    private OnRouteCompletion mDelegate;

    ParserTask(OnRouteCompletion caller) {
        mDelegate = caller;
    }



    // Parsing the data in non-ui thread
    @Override
    protected List<Route> doInBackground(String... jsonData) {
        List<Route> listRoutes = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData[0]);
            // Starts parsing data
            listRoutes = DataParser.parseRoutes(jsonObject);
            Timber.d("Executing routes: " +listRoutes.toString());

        } catch (Exception exception) {
            Timber.e(exception.getLocalizedMessage());
        }
        return listRoutes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<Route> result) {
        mDelegate.onRoutesFetched(result);
        /*
        ArrayList<LatLng> points;
        PolylineOptions polylineOptions = null;

        // Traversing through all the routes
        for (Route route : result) {
            points = new ArrayList<>();
            polylineOptions = new PolylineOptions();

            route.getPolyline();
            // Fetching all the points in i-th route

            points.add(latLngPosition);

            // Adding all the points in the route to LineOptions
            polylineOptions.addAll(points);
            polylineOptions.width(ROUTE_WIDTH);
            polylineOptions.color(ROUTE_COLOR);

            Timber.d("onPostExecute lineoptions decoded");
        }

        if (polylineOptions != null) {
            Timber.d(String.valueOf(polylineOptions));
            mDelegate.onRouteDrawn(polylineOptions);
        } else {
            Timber.d("Polyline was null");
        }
        */
    }
}
