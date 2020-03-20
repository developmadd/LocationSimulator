package com.madd.madd.locationsimulator;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMapSender;
    private GoogleMap mMapReceiver;
    private TextView textView;
    private Button buttonReceiver;
    private Button buttonSender;
    private CheckBox checkBoxBattery;
    private CheckBox checkBoxRoute;

    LocationModel locationModel = new LocationModel(this);
    Marker markerReceiver, markerSender;

    Polyline line;
    Thread receiverThread, senderThread;
    String routeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        routeId = UUID.randomUUID().toString();

        buttonReceiver = findViewById(R.id.buttonFollow);
        buttonSender = findViewById(R.id.buttonShare);
        textView = findViewById(R.id.textView);
        checkBoxBattery = findViewById(R.id.checkBoxBattery);
        checkBoxRoute = findViewById(R.id.checkBoxRoute);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFriend);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMapReceiver = googleMap;
            }
        });

        SupportMapFragment mapFragment2 = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapUser);
        mapFragment2.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMapSender = googleMap;
            }
        });

        events();
    }


    void events(){

        buttonReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( buttonReceiver.getText().equals("START LISTENING") ) {
                    startFollow();
                    buttonReceiver.setText("STOP LISTENING");
                } else {
                    stopFollow();
                    buttonReceiver.setText("START LISTENING");
                }
            }
        });

        buttonSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( buttonSender.getText().equals("START SHARING") ) {
                    startSharing();
                    buttonSender.setText("STOP SHARING");
                } else {
                    stopSharing();
                    buttonSender.setText("START SHARING");
                }
            }
        });


        checkBoxBattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean lowBattery) {
                shareLimit = lowBattery ? 3 : 1;
                shareStep = 0;
            }
        });

        checkBoxRoute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean showRoute) {
                if( !showRoute && line != null ){
                    line.remove();
                }
            }
        });

    }






    int timeInterval = 1000;
    int shareStep = 0, shareLimit = 1;
    public void startSharing() {

        final CommunicationHandler handler = new CommunicationHandler(new CommunicationHandler.OnReceiveMessage() {
            @Override
            public void handleMessage(Message msg) {
                double x = msg.getData().getDouble("x");
                double y = msg.getData().getDouble("y");
                LatLng latLng = new LatLng(x,y);

                if(markerSender != null){
                    markerSender.remove();
                }
                markerSender = mMapSender.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.dog)));
                mMapSender.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
            }
        });



        senderThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while ( senderThread != null ){

                    try                            {    Thread.sleep(timeInterval); }
                    catch (InterruptedException e) {    e.printStackTrace(); }

                    simulateNewLocation();
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putDouble("x",latLng.latitude);
                    bundle.putDouble("y",latLng.longitude);
                    message.setData(bundle);
                    handler.sendMessage(message);

                    if( ++shareStep == shareLimit ) {
                        shareStep = 0;
                        locationModel.updateLocation(latLng, routeId);
                    }

                }
            }
        });
        senderThread.start();

    }
    public void stopSharing() {
        if( senderThread != null ) {
            senderThread = null;
        }
    }



















    public void startFollow() {

        final CommunicationHandler handler = new CommunicationHandler(new CommunicationHandler.OnReceiveMessage() {
            @Override
            public void handleMessage(Message msg) {
                double x = msg.getData().getDouble("x");
                double y = msg.getData().getDouble("y");
                String dateSend = msg.getData().getString("date");
                String dateUpdated = msg.getData().getString("dateUpdated");
                LatLng latLng = new LatLng(x,y);
                textView.setText("last received : " + dateSend + "\n" + "last updated : " + dateUpdated);

                if(markerReceiver != null){
                    markerReceiver.remove();
                }
                if(line != null){
                    line.remove();
                }
                if( checkBoxRoute.isChecked() ) {
                    List<Location> list = locationModel.getNetworkLocationList(routeId);
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(5)
                            .color(Color.RED);
                    for (Location locationUpdate : list) {
                        polylineOptions.add(locationUpdate.latLng);
                    }
                    line = mMapReceiver.addPolyline(polylineOptions);
                }

                markerReceiver = mMapReceiver.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.dog)));
                mMapReceiver.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
            }
        });

        receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {

            while ( receiverThread != null ){
                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Location location = locationModel.getCurrentLocation(routeId);
                if( location != null ) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("date", location.date);
                    bundle.putString("dateUpdated", String.valueOf(Calendar.getInstance().getTime()).substring(10, 19));
                    bundle.putDouble("x", location.latLng.latitude);
                    bundle.putDouble("y", location.latLng.longitude);

                    message.setData(bundle);
                    handler.sendMessage(message);
                }

            }

            }
        });
        receiverThread.start();

    }

    public void stopFollow() {
        if( receiverThread != null ){
            receiverThread = null;
        }
    }














    @Override
    protected void onStart() {
        super.onStart();
        if( buttonReceiver.getText().equals("STOP LISTENING") ) {
            startFollow();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopFollow();
    }








    // Location simulator
    private LatLng latLng = new LatLng(20.684349, -101.370192);
    private void simulateNewLocation(){
        double x = latLng.latitude + newCoordinate();
        double y = latLng.longitude + newCoordinate();
        latLng = new LatLng(x,y);
    }

    private Random random = new Random();
    private double newCoordinate() {
        return (random.nextInt(5) == 1 ? 1 : -1) *
                ((random.nextInt(2) + 1 ) / 10000.0); // 0.0001//10 mts
    }

}
