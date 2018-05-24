package com.cybercom.passenger.route;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

public class FetchRouteUrl extends AsyncTask<String, Void, String> {

    private GoogleMap mGoogleMap;
    private ParserTask.OnRouteCompletion mCaller;
    public FetchRouteUrl(GoogleMap googleMap, LatLng origin, LatLng destination, ParserTask.OnRouteCompletion caller)
    {
        mGoogleMap = googleMap;
        mCaller = caller;
        execute(getURL(origin,destination));
    }


    private String getURL(LatLng from, LatLng to){

        String origin = "origin=" + from.latitude + "," + from.longitude;
        String dest = "destination=" + to.latitude + "," + to.longitude;
        String sensor = "sensor=false";
        String params = origin +"&"+ dest + "&" + sensor;
        return "https://maps.googleapis.com/maps/api/directions/json?"+params;
    }

    public FetchRouteUrl(GoogleMap googleMap, LatLng origin, LatLng destination, LatLng viaStart, LatLng viaEnd, ParserTask.OnRouteCompletion caller)
    {
       Timber.d("reroute");
        mGoogleMap = googleMap;
        mCaller = caller;
        execute(getURL(origin,destination, viaStart,viaEnd));
    }

    private String getURL(LatLng from, LatLng to, LatLng viaStart, LatLng viaEnd)
    {

        String origin = "origin=" + from.latitude + "," + from.longitude;
        String dest = "destination=" + to.latitude + "," + to.longitude;
        String sensor = "sensor=false";
        //String waypoints = "&waypoints=via:" + viaStart.latitude + "%2C1"+ viaStart.longitude + "%7Cvia:" + viaEnd.latitude + "%2C1"+ viaEnd.longitude;
        String waypoints = "&waypoints=" + viaStart.latitude + ","+ viaStart.longitude + "|" + viaEnd.latitude + ","+ viaEnd.longitude;
        String params = origin +"&"+ dest + waypoints + "&" + sensor ;
        return "https://maps.googleapis.com/maps/api/directions/json?"+params;
    }



    @Override
    protected String doInBackground(String... url) {
        // For storing data from web service
        String data = "";
        try {
            // Fetching the data from web service
            data = downloadUrl(url[0]);
            Timber.d(data);
        } catch (Exception e) {
            Timber.e(e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        ParserTask parserTask = new ParserTask(mGoogleMap, mCaller);
        // Invokes the thread for parsing the JSON data
        parserTask.execute(result);

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

        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(url.openStream()));

        String jsonString;

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        bufferedReader.close();

        jsonString = stringBuilder.toString();

        Timber.d("JSON: %s", jsonString);
        httpURLConnection.disconnect();

        return jsonString;
    }
}
