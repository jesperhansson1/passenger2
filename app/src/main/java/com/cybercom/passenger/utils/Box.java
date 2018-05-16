package com.cybercom.passenger.utils;

import com.google.android.gms.maps.model.LatLng;

public class Box {
        private double swLatitude  = 0.0;
        private double swLongitude = 0.0;
        private double neLatitude  = 0.0;
        private double neLongitude = 0.0;

        /*************************************************************************
         Constructor.
         @param bboxSpecification A comma-separated string containing the
         southwest latitude, soutwest longitude, northest latitude, and
         northest longitude.
         *************************************************************************/
        public Box(String bboxSpecification) {
            String tokens[] = bboxSpecification.split("(?:,\\s*)+");

            if (tokens.length != 4) {
                throw new IllegalArgumentException(
                        String.format("Expected 4 values in bbox string but found %d: %s\n",
                                tokens.length, bboxSpecification));
            }
            swLatitude =  Double.parseDouble(tokens[0]);
            swLongitude = Double.parseDouble(tokens[1]);
            neLatitude =  Double.parseDouble(tokens[2]);
            neLongitude = Double.parseDouble(tokens[3]);
        }

        @Override
        public String toString()
        {
            return String.format("swLatitude=%f, swLongitude=%f, neLatitude=%f, neLongitude=%f",
                    swLatitude, swLongitude, neLatitude, neLongitude);
        }

        /*************************************************************************
         Checks if the bounding box contains the latitude and longitude. Note that
         the function works if the box contains the prime merdian but does not
         work if it contains one of the poles.
         *************************************************************************/
        public boolean contains(double latitude, double longitude) {
            boolean longitudeContained = false;
            boolean latitudeContained = false;

            // Check if the bbox contains the prime meridian (longitude 0.0).
            if (swLongitude < neLongitude) {
                if (swLongitude < longitude && longitude < neLongitude) {
                    longitudeContained = true;
                }

            } else if ((0 < longitude && longitude < neLongitude) ||
                        (swLongitude < longitude && longitude < 0)) {
                // Contains prime meridian.
                    longitudeContained = true;
            }

            if (swLatitude < neLatitude && (swLatitude < latitude && latitude < neLatitude)) {
                    latitudeContained = true;
            }
            return (longitudeContained && latitudeContained);
        }

        public Box(double minLatitude, double minLangitude, double maxLatitude,
                   double maxLangitude) {

           // bbox = new Box("37.43, -122.38, 37.89, -121.98");
            //Box bbox1 = new Box("55.3733065, 12.6928275, 56.0467692, 13.1574056");

            //Box bbox = new Box("55.3733065, 12.6928275, 56.0467692, 13.1574056");
            //double latitude = 59.329323;//55.612024;
            //double longitude = 18.068581;//13.009798;

            swLatitude =  minLatitude;
            swLongitude = minLangitude;
            neLatitude =  maxLatitude;
            neLongitude = maxLangitude;
            //System.out.printf("bbox (%s) contains %f, %f: %s\n",
            //        bbox, latitude, longitude, bbox.contains(latitude, longitude));

            //bbox = new Box("50.99, -2.0, 54, 1.0");
            //latitude = 51.0;
            //longitude = 0.1;

          /*  System.out.printf("bbox (%s) contains %f, %f: %s\n",
                    bbox, latitude, longitude, bbox.contains(latitude, longitude));*/
        }

}
