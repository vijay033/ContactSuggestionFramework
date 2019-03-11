package com.example.vijay.contactsuggestionframework;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.vijay.contactsuggestionframework.DataCollection.CallReceiver;
import com.example.vijay.contactsuggestionframework.DataCollection.callentryDataBase;
import com.example.vijay.contactsuggestionframework.DataCollection.convertDB2ARFF;
import com.example.vijay.contactsuggestionframework.DataTraining.modelTraining;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "contactSuggestion";
    private static final int PERMISSIONS_REQUEST=100;
    Context context;
    CallReceiver callReceiver;
    convertDB2ARFF db2arff;
    modelTraining trainingReciver;
    private static boolean isTrain = false;


    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG,"Permission Granted");
                    Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG,"Permission Denied");
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.PACKAGE_USAGE_STATS,
                        Manifest.permission.GET_TASKS,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST);


        if(!checkForPermission(this)){
            Log.i(TAG,"Permission Access");

        }
        rwlock.writeLock().lock();
        callReceiver = new CallReceiver(context);
        rwlock.writeLock().unlock();

        rwlock.writeLock().lock();
        db2arff = new convertDB2ARFF(context);
        rwlock.writeLock().unlock();

        rwlock.writeLock().lock();
        trainingReciver = new modelTraining();
        rwlock.writeLock().unlock();



    }
    public void sendTrainingIntent(){
        registerReceiver(trainingReciver,new IntentFilter("TrainingIntent"));
        broadcastIntent();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(isTrain==false){
            sendTrainingIntent();
            isTrain= true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(trainingReciver);
        isTrain =false;
    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }


    public void broadcastIntent(){
        Intent intent = new Intent();
        intent.setAction("TrainingIntent");
        sendBroadcast(intent);
    }

}
