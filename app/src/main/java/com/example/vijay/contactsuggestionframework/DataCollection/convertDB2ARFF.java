package com.example.vijay.contactsuggestionframework.DataCollection;


import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class convertDB2ARFF {

    public static String TAG = "convertDB2ARFF";
    private static String MODEL_PATH = Environment.getExternalStorageDirectory().toString()+"/CONTACT_DATA/";
    public static final String TRAININGFILE = "training.arff";
    Context context;
    callentryDataBase callDB;
    /*Database Attributes*/
    int day;
    int hrs;
    String contactName;
    String contactNumber;
    long Duration;
    int CallType;
    String topPackage;
    Location location;
    double lati;
    double longi;
    File modelsavedfile;
    private static final int MAXAPP = 20;
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

    public static int NUMBEROFATTRIBUTES = 3; // 2 (Feature) + 1 (Class)
    public static  int NUMBEROFINSTANCES = 1;
    public static Instances trainingset;



    public convertDB2ARFF(Context ctx){
        context = ctx;
        callDB = new callentryDataBase(context);
        readDataBase();
    }

    public synchronized void readDataBase()  {

        /*Create WEKA ARFF Format*/
        Attribute attribute1 = new Attribute("DAY");
        Attribute attribute2 = new Attribute("HOUR");
        Attribute attribute3 = new Attribute("C_NAME");

        ArrayList<Attribute> attributeArrayList = new ArrayList<>(NUMBEROFATTRIBUTES);
        attributeArrayList.add(attribute1);
        attributeArrayList.add(attribute2);
        attributeArrayList.add(attribute3);

        Instance instance = new DenseInstance(NUMBEROFATTRIBUTES);

        /*Read Database Entry One by one*/
        Cursor cursor = callDB.getAllData();

        /*Number of training data set*/
        NUMBEROFINSTANCES = cursor.getCount();

        if (cursor.getCount() == 0) {
            Log.i(TAG, "No data");
        } else {
            int db_day;
            int db_hour;
            double db_lat;
            double db_lon;
            int db_CallType;
            String db_contactName;
            String db_phoneNumber;
            long db_duration;
            String db_packageName;

            trainingset = new Instances("training",attributeArrayList,NUMBEROFINSTANCES);
            trainingset.setClassIndex(trainingset.numAttributes()-1);

            while (cursor.moveToNext()) {
                rwlock.readLock().lock();
                db_day = cursor.getInt(1);
                db_hour = cursor.getInt(2);
                db_lat = cursor.getDouble(3);
                db_lon = cursor.getDouble(4);
                db_CallType = cursor.getInt(5);
                db_contactName  = cursor.getString(6);
                db_phoneNumber  = cursor.getString(7);
                db_duration = cursor.getLong(8);
                db_packageName = cursor.getString(9);

                /*Dump to ARFF dataframe format*/
                instance.setValue(attribute1,db_day);
                instance.setValue(attribute2,db_hour);
                instance.setValue(attribute3,db_contactName.hashCode());
                /*Put ContactName and HashCode in SharedPreferences*/
                trainingset.add(instance);
                rwlock.readLock().unlock();
            }

            /*Save data frame to model.arff*/
            rwlock.writeLock().lock();

            File datapathFiles = new File(MODEL_PATH);
            if(!datapathFiles.exists()){
                datapathFiles.mkdir();
            }

            modelsavedfile = new File(MODEL_PATH+File.separator+TRAININGFILE);
            if(modelsavedfile.exists()){
                modelsavedfile.delete();
            }else{
                try {
                    modelsavedfile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /*Dump the training dataset @ internal storage*/
            ArffSaver saver = new ArffSaver();
            saver.setInstances(trainingset);
            try {
                saver.setFile(new File(MODEL_PATH+File.separator+TRAININGFILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                saver.writeBatch();
            } catch (IOException e) {
                e.printStackTrace();
            }
            rwlock.writeLock().unlock();

//            Below code works fine too
//          BufferedWriter writer = null;
//            try {
//                writer = new BufferedWriter(new FileWriter(modelsavedfile));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                writer.write(trainingset.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                writer.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
    }/*readDataBase*/
}


/*Testing */
//            Cursor cursor = callentryDataBaseContext.getAllData();
//            if(cursor.getCount() == 0){
//                Log.i(TAG,"No data");
//            }else{
//
//                int db_day ;
//                int db_hour ;
//                double db_lat ;
//                double db_lon ;
//                int db_CallType ;
//                String db_contactName ;
//                String db_phoneNumber ;
//                long db_duration ;
//                String db_packageName;
//                while(cursor.moveToNext()){
//                    db_day = cursor.getInt(1);
//                    db_hour = cursor.getInt(2);
//                    db_lat = cursor.getDouble(3);
//                    db_lon = cursor.getDouble(4);
//                    db_CallType = cursor.getInt(5);
//                    db_contactName = cursor.getString(6);
//                    db_phoneNumber = cursor.getString(7);
//                    db_duration = cursor.getLong(8);
//                    db_packageName = cursor.getString(9);
//                    Log.i(TAG,db_packageName);
//                }
//            }