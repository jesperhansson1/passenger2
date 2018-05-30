package com.cybercom.passenger.route;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.cybercom.passenger.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FetchRoute extends AsyncTask<String, Void, List<Route>> {

    private static final String DIRECTIONS_URL =
            "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String ORIGIN = "origin=";
    private static final String DESTINATION = "destination=";
    private static final String WAYPOINTS = "waypoints=";

    // Used to optimize the route for multiple waypoints to find the best solution for the
    // "Travling sales man" problem.
    private static final String OPTIMIZE_TRUE = "optimize:true";
    private static final String DIVIDER = "|";
    private static final String COMMA = ",";
    private static final String ADD = "&";
    private static final String KEY = "key=";

    private ParserTask.OnRouteCompletion mCaller;

    /**
     *
     * @param origin
     * @param destination
     * @param wayPoints
     * @param caller
     */
    public FetchRoute(LatLng origin, LatLng destination, @Nullable List<RidePoints> wayPoints,
                      ParserTask.OnRouteCompletion caller) {
        Timber.d("re-route");
        mCaller = caller;
        execute(getRouteURL(origin, destination, wayPoints));
    }

    @Override
    protected List<Route> doInBackground(String... url) {
        // For storing data from web service
        List<Route> routes = new ArrayList<>();
        try {
            // Fetching the data from web service
            String data = downloadUrl(url[0]);
            routes = parseRoute(data);
            Timber.d(data);

        } catch (Exception e) {
            Timber.e(e.toString());
        }
        return routes;
    }


    @Override
    protected void onPostExecute(List<Route> result) {
        super.onPostExecute(result);
        mCaller.onRoutesFetched(result);
    }

    private List<Route> parseRoute(String... jsonData ) {
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

    private String downloadUrl(String urlString) throws IOException {

        HttpURLConnection httpURLConnection;

        URL url = new URL(urlString);

        httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setReadTimeout(10000 /* milliseconds */);
        httpURLConnection.setConnectTimeout(15000 /* milliseconds */);

        httpURLConnection.setDoOutput(true);

        httpURLConnection.connect();

        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(url.openStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        bufferedReader.close();

        String jsonString = stringBuilder.toString();

        Timber.d("JSON: %s", jsonString);
        httpURLConnection.disconnect();

        return jsonString;
    }

    @Nullable
    private String getWayPointsString(List<RidePoints> wayPoints) {
        if (wayPoints == null) {
            return null;
        }
        StringBuilder wayPointsString = new StringBuilder();
        wayPointsString.append(WAYPOINTS);
        wayPointsString.append(OPTIMIZE_TRUE);
        for (RidePoints wayPoint : wayPoints) {
            wayPointsString.append(DIVIDER);
            wayPointsString.append(wayPoint.getFromPoint().latitude);
            wayPointsString.append(COMMA);
            wayPointsString.append(wayPoint.getFromPoint().longitude);
            wayPointsString.append(DIVIDER);
            wayPointsString.append(wayPoint.getToPoint().latitude);
            wayPointsString.append(COMMA);
            wayPointsString.append(wayPoint.getToPoint().longitude);
        }
        return wayPointsString.toString();
    }

    private String getRouteURL(LatLng from, LatLng to, @Nullable List<RidePoints> wayPoints) {
        StringBuilder url = new StringBuilder();
        url.append(DIRECTIONS_URL);
        // Add origin drive point
        url.append(ORIGIN);
        url.append(from.latitude);
        url.append(COMMA);
        url.append(from.longitude);
        // Add destination drive point
        url.append(ADD);
        url.append(DESTINATION);
        url.append(to.latitude);
        url.append(COMMA);
        url.append(to.longitude);

        // Add any existing waypoints
        String wayPointsString = getWayPointsString(wayPoints);
        if (wayPointsString != null) {
            url.append(ADD);
            url.append(wayPointsString);
        }
        url.append(ADD);
        url.append(KEY);
        url.append(((Context)mCaller).getResources().getString(R.string.google_api_key));
        return url.toString();
    }
}
