package com.madd.madd.locationsimulator;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class LocationModel {

    private Context context;
    private List<Location> locationList = new ArrayList<>();
    private SQLLocation sqlLocation;


    LocationModel(Context context) {
        this.sqlLocation = new SQLLocation(context, SQLLocation.DB_NAME, null, 1);
        this.context = context;
    }




    // ------------- SENDER SERVICES ----------------
    void updateLocation(LatLng latLng, String routeId){

        Location lastLocation = createLocation(latLng);
        locationList.add(lastLocation);

        persistLocation(lastLocation,routeId);

    }

    private Location createLocation(LatLng latLng){
        Location lastLocation = new Location();
        lastLocation.date = String.valueOf(Calendar.getInstance().getTime()).substring(10,19);
        lastLocation.latLng = latLng;
        lastLocation.sequence = locationList.size() + 1;
        return lastLocation;
    }



    private BroadcastConnectivity broadcastConnectivity;
    private void persistLocation(final Location lastLocation, final String routeId){
        APILocation.getInstance(context).registerLocation(routeId, lastLocation, new APILocation.OnRegisterLocation() {

            @Override
            public void onSuccessRegister(Location location) {

            }

            @Override
            public void onFailedRegister(String error) {
                sqlLocation.updateLocation(routeId,lastLocation);
                if( broadcastConnectivity == null ) {
                    broadcastConnectivity = new BroadcastConnectivity(routeId,
                        new BroadcastConnectivity.OnUpdateLocationList() {
                            @Override
                            public void onUpdate() {
                                context.unregisterReceiver(broadcastConnectivity);
                            }
                        });
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                        context.registerReceiver(broadcastConnectivity, intentFilter);
                }
            }

        });
    }
















    // ------------- RECEIVER SERVICES ----------------
    List<Location> getNetworkLocationList(String routeId) {
        return APILocation.getInstance(context).getLocationList(routeId);
    }

    Location getCurrentLocation(String routeId){
        return APILocation.getInstance(context).getLastLocation(routeId);
    }














}
