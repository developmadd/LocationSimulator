package com.madd.madd.locationsimulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import java.util.List;

public class BroadcastConnectivity extends BroadcastReceiver {




    String routeId;
    OnUpdateLocationList onUpdateLocationList;


    public BroadcastConnectivity(String routeId, OnUpdateLocationList onUpdateLocationList) {
        this.routeId = routeId;
        this.onUpdateLocationList = onUpdateLocationList;
    }





    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connectivityStatus = !intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if(connectivityStatus){

            final SQLLocation sqlLocation = new SQLLocation(context, SQLLocation.DB_NAME, null, 1);


            List<Location> lostLocationList = sqlLocation.getLocationList(routeId);
            APILocation.getInstance(context).registerSetLocation(routeId, lostLocationList, new APILocation.OnRegisterLocation() {
                @Override
                public void onSuccessRegister(Location location) {
                    if( location != null ) {
                        sqlLocation.clearLocationList(routeId);
                        onUpdateLocationList.onUpdate();
                    }
                }

                @Override
                public void onFailedRegister(String error) {

                }
            });


        }
    }


    interface OnUpdateLocationList{
        void onUpdate();
    }
}
