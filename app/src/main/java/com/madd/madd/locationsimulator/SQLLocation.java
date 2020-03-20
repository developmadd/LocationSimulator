package com.madd.madd.locationsimulator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class SQLLocation extends SQLiteOpenHelper {


    static String DB_NAME = "LocationDB";
    private static String TABLE_NAME = "location";
    private static String ROUTE_ID = "route_id";
    private static String SEQUENCE = "sequence";
    private static String DATE = "date";
    private static String X = "x";
    private static String Y = "y";


    //private final String DELETE_TABLE_LOCATION_SENTENCE =
    //        "DROP TABLE IF EXISTS " + TABLE_NAME;

    private final String CREATE_TABLE_LOCATION_SENTENCE =
            "CREATE TABLE " + TABLE_NAME +
            " ("+ ROUTE_ID +" TEXT, " +
                 SEQUENCE + " INTEGER, " +
                 DATE + " STRING , "+
                 X +" REAL, "+ Y +" REAL)";




    SQLLocation(@Nullable Context context,
                @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory,
                int version) {
        super(context, name, factory, version);
    }





    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOCATION_SENTENCE);
    }

    void updateLocation(String routeId, Location location){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROUTE_ID,routeId);
        values.put(SEQUENCE,location.sequence);
        values.put(X,location.latLng.latitude);
        values.put(Y,location.latLng.longitude);
        values.put(DATE,location.date);

        database.insert(TABLE_NAME, SEQUENCE,values);
        database.close();
    }

    List<Location> getLocationList(String routeId) {

        SQLiteDatabase database = getReadableDatabase();
        String[] columns = {
                ROUTE_ID,
                SEQUENCE,
                X, Y,
                DATE};
        String[] consultCriteria = {routeId};
        Cursor cursor = database.query(TABLE_NAME, columns, SQLLocation.ROUTE_ID + "=?", consultCriteria, null, null, SQLLocation.SEQUENCE);

        List<Location> sqlLocationList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int sequence = cursor.getInt(cursor.getColumnIndex(columns[1]));
                double x = cursor.getDouble(cursor.getColumnIndex(columns[2]));
                double y = cursor.getDouble(cursor.getColumnIndex(columns[3]));
                String date = cursor.getString(cursor.getColumnIndex(columns[4]));
                Location location = new Location();
                location.sequence = sequence;
                location.date = date;
                location.latLng = new LatLng(x, y);
                sqlLocationList.add(location);
            }
            cursor.close();
        }
        return sqlLocationList;
    }

    void clearLocationList(String routeId){
        SQLiteDatabase database = getWritableDatabase();
        String[] deleteCriteria = {routeId};
        database.delete(TABLE_NAME,ROUTE_ID + " =? ", deleteCriteria);
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL(DELETE_TABLE_LOCATION_SENTENCE);
    }
}
