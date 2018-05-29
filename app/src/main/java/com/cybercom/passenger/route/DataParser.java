package com.cybercom.passenger.route;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.cybercom.passenger.model.Bounds;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

class DataParser {


    JSONObject mJsonObject;
    private ParserTask.OnRouteCompletion mDelegate;
    DataParser(ParserTask.OnRouteCompletion delegate)
    {
        mDelegate = delegate;
    }
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    List<List<HashMap<String,String>>> parse(JSONObject jsonObject){
        mJsonObject = jsonObject;
        List<List<HashMap<String, String>>> listRoutes = new ArrayList<>() ;
        JSONArray jsonArrayRoutes;
        JSONArray jsonArrayLegs;
        JSONArray jsonArraySteps;
        JSONObject jsonObjectBounds;
        String distance = "0"; //in meter
        String duration = "0"; //in seconds

        try {

            jsonArrayRoutes = jsonObject.getJSONArray("routes");
            Timber.d("routes " + jsonArrayRoutes.toString());

            // Traversing all routes
            for(int i=0;i<jsonArrayRoutes.length();i++){

                //To get bounds - need to be changed to okhttp for speed/performance tuning...

                jsonObjectBounds = ( (JSONObject)jsonArrayRoutes.get(i)).getJSONObject("bounds");
                JSONObject ne = jsonObjectBounds.getJSONObject("northeast");



                jsonArrayLegs = ( (JSONObject)jsonArrayRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> listPath = new ArrayList<>();

                //To get distance and time

                for(int j=0;j<jsonArrayLegs.length();j++) {
                    System.out.println(((JSONObject) jsonArrayLegs.get(j)).getJSONObject("distance"));
                    System.out.println(((JSONObject) jsonArrayLegs.get(j)).getJSONObject("duration"));
                    distance = (((JSONObject)jsonArrayLegs.get(j)).getJSONObject("distance").get("value")).toString();
                    duration = (((JSONObject)jsonArrayLegs.get(j)).getJSONObject("duration").get("value")).toString();
                }

                Bounds bounds = new Bounds(Double.parseDouble(jsonObjectBounds.getJSONObject("northeast").get("lat").toString()),
                        Double.parseDouble(jsonObjectBounds.getJSONObject("northeast").get("lng").toString()),
                        Double.parseDouble(jsonObjectBounds.getJSONObject("southwest").get("lat").toString()),
                        Double.parseDouble(jsonObjectBounds.getJSONObject("southwest").get("lng").toString()), Long.parseLong(distance),
                        Long.parseLong(duration));

                mDelegate.onBoundsParse(bounds);


                // Traversing all legs
                for(int j=0;j<jsonArrayLegs.length();j++){
                    jsonArraySteps = ( (JSONObject)jsonArrayLegs.get(j)).getJSONArray("steps");

                    // Traversing all steps
                    for(int k=0;k<jsonArraySteps.length();k++){
                        String polyline;
                        polyline = (String)((JSONObject)((JSONObject)jsonArraySteps.get(k)).get("polyline")).get("points");
                        List<LatLng> latlngList = decodePoly(polyline);

                        // Traversing all points
                        for(int l=0;l<latlngList.size();l++){
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("lat", Double.toString((latlngList.get(l)).latitude) );
                            hashMap.put("lng", Double.toString((latlngList.get(l)).longitude) );
                            listPath.add(hashMap);
                        }
                    }
                    listRoutes.add(listPath);
                }
            }
        } catch (Exception exception){
            Timber.e(exception.getLocalizedMessage());
        }
        return listRoutes;
    }

    private List<LatLng> decodePoly(String encoded) {


        List<LatLng> latlngList = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            latlngList.add(p);
        }

        return latlngList;
    }

}
