package com.cybercom.passenger.route;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cybercom.passenger.model.Route;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
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

    public interface OnRouteCompletion{
        void onRoutesFetched(List<Route> routes);
    }

    private final OnRouteCompletion mOnRouteCompletion;

    /**
     *
     * @param origin
     * @param destination
     * @param latLngs
     * @param caller
     */
    public FetchRoute(LatLng origin, LatLng destination, @Nullable List<LatLng> latLngs,
                      @NonNull OnRouteCompletion caller, String googleApiKey) {
        Timber.d("re-route");
        mOnRouteCompletion = caller;
        execute(getRouteURL(origin, destination, latLngs, googleApiKey));
    }

    @Override
    protected List<Route> doInBackground(String... url) {
        // For storing data from web service
        List<Route> routes = new ArrayList<>();
        try {
            // Fetching the data from web service
            String data = downloadUrl(url[0]);
            JSONObject jsonObject = new JSONObject(data);
            routes = parseRoutes(jsonObject);
            Timber.d(data);

        } catch (Exception e) {
            Timber.e(e.toString());
        }
        return routes;
    }


    @Override
    protected void onPostExecute(List<Route> result) {
        mOnRouteCompletion.onRoutesFetched(result);
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
    private String getWayPointsString(@Nullable List<LatLng> latLngs) {
        if (latLngs == null || latLngs.size() == 0) {
            return null;
        }
        StringBuilder wayPointsString = new StringBuilder();
        wayPointsString.append(WAYPOINTS);
        wayPointsString.append(OPTIMIZE_TRUE);
        for (LatLng wayPoint : latLngs) {
            wayPointsString.append(DIVIDER);
            wayPointsString.append(wayPoint.latitude);
            wayPointsString.append(COMMA);
            wayPointsString.append(wayPoint.longitude);
        }
        return wayPointsString.toString();
    }

    private String getRouteURL(LatLng from, LatLng to, @Nullable List<LatLng> latLngs,
                               String googleApiKey) {
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

        String wayPointsString = getWayPointsString(latLngs);
        if (wayPointsString != null) {
            url.append(ADD);
            url.append(wayPointsString);
        }
        url.append(ADD);
        url.append(KEY);
        url.append(googleApiKey);
        return url.toString();
    }

    private static final String JSON_ROUTES = "routes";
    /**
     * Parse the routes object
     * @param jsonObject A JSONObject holding the routes to parseRoutes
     * @return A List with all the routes.
     */
    private List<Route> parseRoutes(JSONObject jsonObject) {
        List<Route> routes = new ArrayList<>();
        try {
            JSONArray jsonArrayRoutes = jsonObject.getJSONArray(JSON_ROUTES);
            for(int i=0; i<jsonArrayRoutes.length(); i++) {
                routes.add(Route.createInstanceFromJsonObject((JSONObject)jsonArrayRoutes.get(i)));
            }
        } catch (Exception exception){
            Timber.e(exception.getLocalizedMessage());
        }
        return routes;
    }
}
