package com.cybercom.passenger.route;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

class DataParser {

    private static final String TAG = DataParser.class.getSimpleName();

    private List<Route> getRouteListFromRootObject(JSONObject rootJsonObject) throws JSONException {
        List<Route> routeList = new ArrayList<>();
        JSONArray jsonArrayRoutes;
        jsonArrayRoutes = rootJsonObject.getJSONArray("routes");

        // Traversing all routes
        for (int i = 0; i < jsonArrayRoutes.length(); i++) {
            Route route = new Route();
            JSONObject jsonObjectRoute = ((JSONObject)jsonArrayRoutes.get(i));

            route.setBounds(getBoundsFromRouteObject(jsonObjectRoute));
            route.setLegs(getLegsFromRouteObject(jsonObjectRoute));
            routeList.add(route);
        }
        return routeList;
    }

    private Bounds getBoundsFromRouteObject(JSONObject jsonObjectRoute) throws JSONException {
        JSONObject jsonObjectBounds = jsonObjectRoute.getJSONObject("bounds");
        JSONObject northeast = jsonObjectBounds.getJSONObject("northeast");
        JSONObject southwest = jsonObjectBounds.getJSONObject("southwest");
        double maxLatitude = northeast.getDouble("lat");
        double maxLongitude = northeast.getDouble("lng");
        double minLatitude = southwest.getDouble("lat");
        double minLongitude = southwest.getDouble("lng");
        return new Bounds(maxLongitude, minLongitude, maxLatitude, minLatitude);
    }

    private Legs getLegsFromRouteObject(JSONObject jsonObjectRoute) throws JSONException {
        JSONArray jsonArrayLegs = jsonObjectRoute.getJSONArray("legs");
        Legs legs = new Legs();
        List<Steps> stepsList = new ArrayList<>();
        for(int j = 0; j < jsonArrayLegs.length(); j++) {
            JSONObject legObject = (JSONObject)jsonArrayLegs.get(j);
            Steps steps = getStepsFromLegObject(legObject);
            stepsList.add(steps);
        }
        legs.setStepsList(stepsList);
        return legs;
    }

    private Steps getStepsFromLegObject(JSONObject jsonObjectLeg) throws JSONException {
        Steps steps = new Steps();
        JSONArray jsonArraySteps = jsonObjectLeg.getJSONArray("steps");
        List<HashMap<String, String>> listPath = new ArrayList<>();
        for (int k = 0; k<jsonArraySteps.length(); k++) {
            // Traversing all steps

            /*------

            String dist = (String)((JSONObject)((JSONObject)jsonArraySteps.get(k)).get("distance"))
            .get("value");
            if(Integer.parseInt(dist) > INTERVAL)
            {

            }
            ------*/
            String polyline;
            polyline = (String)((JSONObject)((JSONObject)jsonArraySteps.get(k)).get(
                    "polyline")).get("points");
            List<LatLng> latlngList = decodePoly(polyline);

            // Traversing all points
            for(int l=0;l<latlngList.size();l++){
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("lat", Double.toString((latlngList.get(l)).latitude) );
                hashMap.put("lng", Double.toString((latlngList.get(l)).longitude) );
                listPath.add(hashMap);
            }
        }
        steps.setPointsList(listPath);
        return steps;
    }


    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<Route> parse(JSONObject jsonObject){
        try {
            return getRouteListFromRootObject(jsonObject);
        } catch (JSONException e) {
            Timber.e("Could not parse routes: ", e);
        }
        return null;
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
