package com.cybercom.passenger.route;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

class DataParser {

    private static final String JSON_ROUTES = "routes";
    /**
     * Parse the routes object
     * @param jsonObject A JSONObject holding the routes to parseRoutes
     * @return A List with all the routes.
     */
    static List<Route> parseRoutes(JSONObject jsonObject) {
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
