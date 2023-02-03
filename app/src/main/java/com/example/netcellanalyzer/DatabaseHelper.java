package com.example.netcellanalyzer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper{
    // Database and table name
    private static final String DATABASE_NAME = "nca_db";
    private static final String TABLE_NAME = "logs_table";

    DatabaseHelper(Context context){
       super(context, DATABASE_NAME, null, 1 );
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creating SQLite table
        String createTable = "create table " + TABLE_NAME +
                "(id INTEGER PRIMARY KEY, " +
                "operator TEXT, " +
                "signalPower INTEGER, " +
                "sinr INTEGER, " +
                "networkType TEXT, " +
                "freqBand INTEGER, " +
                "cellid INTEGER, " +
                "timestamp TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //Drop if existing already
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }
    public boolean logData(String operator, int signalPower,int sinr, String networkType, int freqBand, int cellId, String timeStamp ){
        // writing to db table
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("operator",operator);
        contentValues.put("signalPower",signalPower);
        contentValues.put("sinr",sinr);
        contentValues.put("networkType",networkType);
        contentValues.put("freqBand",freqBand);
        contentValues.put("cellid",cellId);
        contentValues.put("timestamp",timeStamp);
        sqLiteDatabase.insert(TABLE_NAME, null, contentValues );
        return true;

    }

    public List<String> getAllLabels(){
        List<String> labels = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT  timestamp FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning lables
        return labels;
    }
    public float alfaPercent(String date1, String date2){
        String selectQuery1 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"operator ='alfa'";
        String selectQuery2 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);
        Cursor cursor2 = db.rawQuery(selectQuery2, null);
        float alfa= cursor.getCount();
        float total= cursor2.getCount();
        float alfapercentage = (alfa/total)*100;
        return (float) alfapercentage;
    }
    public float twogPercent(String date1, String date2){
        String selectQuery1 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='EDGE'";
        String selectQuery2 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);
        Cursor cursor2 = db.rawQuery(selectQuery2, null);
        float twog= cursor.getCount();
        float total= cursor2.getCount();
        float twogPercent = (twog/total)*100;
        return (float) twogPercent;
    }
    public float threegPercent(String date1, String date2){
        String selectQuery1 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='HSPA+'";
        String selectQuery3 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='HSUPA'";
        String selectQuery4 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='UMTS'";
        String selectQuery2 = "SELECT operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);
        Cursor cursor3 = db.rawQuery(selectQuery3,null);
        Cursor cursor4 = db.rawQuery(selectQuery4,null);
        Cursor cursor2 = db.rawQuery(selectQuery2, null);
        float threeg= cursor.getCount() + cursor3.getCount()+ cursor4.getCount();
        float total= cursor2.getCount();
        float threegPercent = (threeg/total)*100;
        return (float) threegPercent;
    }

    public float twogSigPower(String date1, String date2){
        String selectQuery1 = "SELECT avg(signalPower)operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='EDGE'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);
        if(cursor != null && cursor.getCount() > 0 ){
            cursor.moveToFirst();
            float twog= cursor.getFloat(0);
            return (float) twog;
        }else{
            return (float) 0;
        }
    }
    public float threegSigPower(String date1, String date2){
        String selectQuery1 = "SELECT avg(signalPower)operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='HSPA' OR networkType ='HSUPA' OR networkType ='HSPA+'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);
        if ( cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            float threeg = cursor.getFloat(0);
            return (float) threeg;
        }else {return (float) 0;}
    }
    public float fourgSigPower(String date1, String date2){
        String selectQuery1 = "SELECT avg(signalPower)operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='LTE'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);
        if( cursor != null && cursor.getCount() > 0 ){
            cursor.moveToFirst();
            float fourg= cursor.getFloat(0);
            return (float) fourg;
        } else {return (float) 0;}

    }
    public float averageSnr(String date1, String date2){
        String selectQuery1 = "SELECT avg(sinr)operator FROM " + TABLE_NAME + " WHERE timestamp BETWEEN " + "'"+ date1 + "'"+" AND "+ "'" + date2 + "'" + " AND " +"networkType ='LTE'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);
        if( cursor != null && cursor.getCount() > 0 ){
            cursor.moveToFirst();
            float avg= cursor.getFloat(0);
            return (float) avg;
        } else {return (float) 0;}

    }

}
