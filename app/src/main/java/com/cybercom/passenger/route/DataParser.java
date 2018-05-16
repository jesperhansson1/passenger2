package com.cybercom.passenger.route;

import android.support.constraint.solver.widgets.Rectangle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static com.cybercom.passenger.utils.GpsLocations.INTERVAL;

class DataParser {

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    List<List<HashMap<String,String>>> parse(JSONObject jsonObject){

        List<List<HashMap<String, String>>> listRoutes = new ArrayList<>() ;
        JSONArray jsonArrayRoutes;
        JSONArray jsonArrayLegs;
        JSONObject jsonObjectBounds;
        JSONArray jsonArraySteps;

        try {

            jsonArrayRoutes = jsonObject.getJSONArray("routes");

            // Traversing all routes
            for(int i=0;i<jsonArrayRoutes.length();i++){

                //to get bounds
                jsonObjectBounds = ( (JSONObject)jsonArrayRoutes.get(i)).getJSONObject("bounds");
                System.out.println("bounds " + jsonObjectBounds);
                JSONObject ne = jsonObjectBounds.getJSONObject("northeast");
                System.out.println("northeast " + jsonObjectBounds.getJSONObject("northeast"));
                System.out.println("northeast lat " + jsonObjectBounds.getJSONObject("northeast").get("lat"));
                System.out.println("northeast lng " + jsonObjectBounds.getJSONObject("northeast").get("lng"));
                System.out.println("southwest " + jsonObjectBounds.getJSONObject("southwest"));
                System.out.println("southwest lat " + jsonObjectBounds.getJSONObject("southwest").get("lat"));
                System.out.println("southwest lng " + jsonObjectBounds.getJSONObject("southwest").get("lng"));




                jsonArrayLegs = ( (JSONObject)jsonArrayRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> listPath = new ArrayList<>();

                // Traversing all legs
                for(int j=0;j<jsonArrayLegs.length();j++){
                    jsonArraySteps = ( (JSONObject)jsonArrayLegs.get(j)).getJSONArray("steps");

                    // Traversing all steps
                    for(int k=0;k<jsonArraySteps.length();k++){

                      /*------

                        String dist = (String)((JSONObject)((JSONObject)jsonArraySteps.get(k)).get("distance")).get("value");
                        if(Integer.parseInt(dist) > INTERVAL)
                        {

                        }
                      ------*/
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

    /*/To get bounds
    public static Rectangle getBoundingBox(Polygon polygon) {

        double boundsMinX = Double.MAX_VALUE; // bottom south latitude of the bounding box.
        double boundsMaxX = Double.MIN_VALUE; // top north latitude of bounding box.

        double boundsMinY = Double.MAX_VALUE; // left longitude of bounding box (western bound).
        double boundsMaxY = Double.MIN_VALUE; // right longitude of bounding box (eastern bound).

        for (int i = 0; i < polygon.getPoints().size(); i++) {
            double x = polygon.getPoints().get(i).latitude;
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);

            double y = polygon.getPoints().get(i).longitude;
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        //Rectangle(double left, double bottom, double right, double top)
        return new Rectangle(boundsMinY, boundsMinX, boundsMaxY, boundsMaxX);
    }*/

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
