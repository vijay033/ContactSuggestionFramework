package com.example.vijay.contactsuggestionframework.DataCollection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class callentryDataBase extends SQLiteOpenHelper {

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();
    public static final String TAG = "callentryDataBase";
    public static final String DATABASE_NAME = "CallEntry";
    public static final String TABLE_NAME = "CallLogEntry";
    public static final String COL_0 = "ID";
    public static final String COL_1 = "DAY";
    public static final String COL_2 = "HOUR";
    public static final String COL_3 = "LATITUDE";
    public static final String COL_4 = "LONGITUDE";
    public static final String COL_5 = "CALL_TYPE";
    public static final String COL_6 = "CONTACT_NAME";
    public static final String COL_7 = "CONTACT_PHONE";
    public static final String COL_8 = "DURATION";
    public static final String COL_9 = "APP_PACKAGE";
    public static int version = 1;


    public callentryDataBase(Context context) {
        super(context, DATABASE_NAME, null, version);
//        context.openOrCreateDatabase(DATABASE_NAME, context.MODE_PRIVATE, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DAY INTEGER, HOUR INTEGER, LATITUDE REAL, LONGITUDE REAL,CALL_TYPE INTEGER,CONTACT_NAME TEXT,CONTACT_PHONE TEXT, DURATION INTEGER, APP_PACKAGE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public synchronized  boolean insertCallLogData(int day, int hour, Double lat, Double lon , int callType, String contactName, String phoneNumber, long duration, String packageName){

        rwlock.writeLock().lock();
        try{
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(COL_1,day);
                contentValues.put(COL_2,hour);
                contentValues.put(COL_3,lat);
                contentValues.put(COL_4,lon);
                contentValues.put(COL_5,callType);
                contentValues.put(COL_6,contactName);
                contentValues.put(COL_7,phoneNumber);
                contentValues.put(COL_8,duration);
                contentValues.put(COL_9,packageName);
                long result = db.insert(TABLE_NAME,null,contentValues);
                if(result==-1){
                    return  false;
                }
                db.close();
            }finally {
            rwlock.writeLock().unlock();
        }
        return  true;
    }

    public synchronized Cursor getAllData(){

        rwlock.readLock().lock();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor res = null;
            res = db.rawQuery("select * from " + TABLE_NAME, null);
            return res;
        }finally {
            rwlock.readLock().unlock();
        }
    }
}
