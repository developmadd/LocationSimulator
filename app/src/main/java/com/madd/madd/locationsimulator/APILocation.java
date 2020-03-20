package com.madd.madd.locationsimulator;

import android.content.Context;
import android.net.ConnectivityManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class APILocation  {

    private List<Location> networkLocationList = new ArrayList<>();
    private Context context;



    private APILocation(Context context) {
        this.context = context;
    }



    private static APILocation apiLocation;
    static APILocation getInstance(Context context){
        if( apiLocation == null ){
            apiLocation = new APILocation(context);
        }
        return apiLocation;
    }



    List<Location> getLocationList(String routeId) {
        return networkLocationList;
    }



    // ------------- SENDER SERVICES ------------------
    void registerLocation(String routeId, Location location, OnRegisterLocation onRegisterLocation) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if( cm != null &&  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            networkLocationList.add(location);
            Collections.sort(networkLocationList, new Comparator<Location>() {
                @Override
                public int compare(Location location, Location t1) {
                    return location.sequence - t1.sequence;
                }
            });
            onRegisterLocation.onSuccessRegister(location);
        } else {
            onRegisterLocation.onFailedRegister("NETWORK");
        }
    }

    void registerSetLocation(String routeId, List<Location> locationList, OnRegisterLocation onRegisterLocation) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if( cm != null &&  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            networkLocationList.addAll(locationList);
            Collections.sort(networkLocationList, new Comparator<Location>() {
                @Override
                public int compare(Location location, Location t1) {
                    return location.sequence - t1.sequence;
                }
            });
            onRegisterLocation.onSuccessRegister(networkLocationList.get(networkLocationList.size() - 1));
        } else {
            onRegisterLocation.onFailedRegister("NETWORK");
        }
    }


















    // ------------- RECEIVER SERVICES ------------------
    Location getLastLocation(String routeId){
        return networkLocationList.isEmpty() ? null : networkLocationList.get(networkLocationList.size() - 1);
    }















    interface OnRegisterLocation{
        void onSuccessRegister(Location location);
        void onFailedRegister(String error);
    }


}
