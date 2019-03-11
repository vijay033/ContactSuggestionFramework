package com.example.vijay.contactsuggestionframework.DataCollection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CallReceiver extends PhonecallReceiver {

    private static String TAG = "CallReceiver";
    int incomingCall = 1;
    int outgoingCall = 2;
    int missedCall = 3;
    int day;
    int hrs;
    String contactName;
    String contactNumber;
    long Duration; /* (millisecond/1000)/60 - min    (millisecond /1000)%60 - sec*/
    long dur_min;
    long dur_sec;
    long DurInMin;
    int CallType;
    List<String> topPackage;
    Location location;
    double lati;
    double longi;
    public static DataInterface dataInterface = null;
    public static callentryDataBase callentryDataBaseContext = null;
    convertDB2ARFF DB2ARFF;
    private static final int UPDATE_DB = 100;
    private static final int MAXAPP = 20;
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

    Context context;

    public CallReceiver(){
        if(dataInterface == null) {
            dataInterface = new DataInterface();
        }
    }

    public CallReceiver(Context context) {
        if(callentryDataBaseContext == null) {
            callentryDataBaseContext = new callentryDataBase(context);
            callentryDataBaseContext.getReadableDatabase();
        }
        this.context = context;
    }

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            rwlock.writeLock().lock();
            DB2ARFF = new convertDB2ARFF(context); /*Update Training File*/
            rwlock.writeLock().unlock();
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
           /*Update Database*/
            rwlock.writeLock().lock();
            int day = dataInterface.getDay();
            int hour = dataInterface.getHour();
            double lat = dataInterface.getLat();
            double lon = dataInterface.getLon();
            int CallType = dataInterface.getGetCallType();
            String contactName = dataInterface.getContactName();
            String phoneNumber = dataInterface.getPhoneNumber();
            long duration = dataInterface.getDuration();
            List<String> packageName= dataInterface.getPackageName();
            for(int i = 0 ; i < packageName.size(); i++) {
                if(callentryDataBaseContext != null) {
                    callentryDataBaseContext.insertCallLogData(day, hour, lat, lon, CallType, contactName, phoneNumber, duration, packageName.get(i));
                }
            }
            rwlock.writeLock().unlock();
            new Thread(runnable1).start();

        }
    };



    public final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_DB){
                new Thread(runnable).start();
            }
        }
    };



    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        rwlock.writeLock().lock();
        hrs = start.getHours();
        day = start.getDay();
        CallType = incomingCall;
        contactNumber = number;
        contactName = getContactDisplayNameByNumber(contactNumber,ctx);
        Duration = start.getTime();
        Toast.makeText(ctx, "onIncomingCallStarted"
                + "Hour:: "+hrs + "Day:: "+day + "Name:: "+contactName + "Num:: " + contactNumber, Toast.LENGTH_LONG).show();

        dataInterface.setDay(day);
        dataInterface.setHour(hrs);
        dataInterface.setContactName(contactName);
        dataInterface.setCallType(CallType);
        dataInterface.setPhoneNumber(contactNumber);
        dataInterface.setDuration(Duration);
        this.context = ctx;
        rwlock.writeLock().unlock();

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        rwlock.writeLock().lock();
        hrs = start.getHours();
        day = start.getDay();
        CallType = outgoingCall;
        contactNumber = number;
        contactName = getContactDisplayNameByNumber(contactNumber,ctx);
        Duration = start.getTime();
        Toast.makeText(ctx, "onOutgoingCallStarted"
                + "Hour:: "+hrs + "Day:: "+day + "Name:: "+contactName + "Num:: " + contactNumber, Toast.LENGTH_LONG).show();

        dataInterface.setDay(day);
        dataInterface.setHour(hrs);
        dataInterface.setContactName(contactName);
        dataInterface.setCallType(CallType);
        dataInterface.setPhoneNumber(contactNumber);
        dataInterface.setDuration(Duration);
        this.context = ctx;
        rwlock.writeLock().unlock();;
    }


    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        rwlock.writeLock().lock();
        Duration =  end.getTime() - start.getTime();

        topPackage = getTopAppName(ctx);
        location = new Location(ctx);
        if(location != null && 1 == location.locationUpdate()){
            lati =  location.getLat();
            longi = location.getLon();
        }
        Toast.makeText(ctx, "onIncomingCallEnded" + "Duration::" + Duration + "Top Package:: "+ topPackage, Toast.LENGTH_SHORT).show();
        dataInterface.setDuration(Duration);
        dataInterface.setLat(lati);
        dataInterface.setLon(longi);
        dataInterface.setPackage(topPackage);
        this.context = ctx;
        rwlock.writeLock().unlock();

        if(!dataInterface.getContactName().equals("Unknown number")){ /*Don't Update Training data having no contact DB entry*/
            Message message = new Message();
            message.what = UPDATE_DB;
            mHandler.sendMessage(message);
        }else{
            Toast.makeText(ctx, "onIncomingCallEnded" +"Name::->"+ dataInterface.getContactName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        rwlock.writeLock().lock();
        Duration =  end.getTime() - start.getTime();
        topPackage = getTopAppName(ctx);
        location = new Location(ctx);
        if(location != null && 1 == location.locationUpdate()){
            lati =  location.getLat();
            longi = location.getLon();
        }
        Toast.makeText(ctx, "onOutgoingCallEnded"+ "Duration::" + Duration + "Top Package:: "+ topPackage , Toast.LENGTH_LONG).show();
        dataInterface.setDuration(Duration);
        dataInterface.setLat(lati);
        dataInterface.setLon(longi);
        dataInterface.setPackage(topPackage);
        this.context = ctx;
        rwlock.writeLock().unlock();

        if(!dataInterface.getContactName().equals("Unknown number")){ /*Don't Update Training data having no contact DB entry*/
            Message message = new Message();
            message.what = UPDATE_DB;
            mHandler.sendMessage(message);
        }else{
            Toast.makeText(ctx, "onOutgoingCallEnded" +"Name::->"+ dataInterface.getContactName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        rwlock.writeLock().lock();
        hrs = start.getHours();
        day = start.getDay();
        CallType = missedCall;
        contactNumber = number;
        contactName = getContactDisplayNameByNumber(contactNumber,ctx);
        Duration = start.getTime();
        dataInterface.setDay(day);
        dataInterface.setHour(hrs);
        dataInterface.setContactName(contactName);
        dataInterface.setCallType(CallType);
        dataInterface.setPhoneNumber(contactNumber);
        dataInterface.setDuration(Duration);
        this.context = ctx;
        rwlock.writeLock().unlock();
        Toast.makeText(ctx, "onMissedCall" , Toast.LENGTH_SHORT).show();
        if(!dataInterface.getContactName().equals("Unknown number")){ /*Don't Update DB having no contact DB entry*/
            Message message = new Message();
            message.what = UPDATE_DB;
            mHandler.sendMessage(message);
        }else{
            Toast.makeText(ctx, "onMissedCall" +"Name::->"+ dataInterface.getContactName(), Toast.LENGTH_SHORT).show();
        }

    }

    public String getContactDisplayNameByNumber(String number,Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        contactName = "Incoming call from";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, null, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                contactName = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }else{
                contactName = "Unknown number";
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return contactName;
    }

    public static List<String> getTopAppName(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String strName = null ;
        List<String> packages = null;
        final List<ActivityManager.RecentTaskInfo> runningTaskInfos;
        try {
              runningTaskInfos = mActivityManager.getRecentTasks(MAXAPP,0);
              packages = new LinkedList<>();
              for(int i = 0; i < runningTaskInfos.size() ; i++){
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                      strName = runningTaskInfos.get(i).topActivity.getPackageName();
                  }
                  packages.add(strName);
              }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        List<String> packages = null;
//        ActivityManager mgr = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.AppTask>  tasks = mgr.getAppTasks();
//        String packagename;
//        String label = null;
//        for (ActivityManager.AppTask task: tasks){
//            packagename = task.getTaskInfo().baseIntent.getComponent().getPackageName();
//            packages.add(packagename);
//            try {
//                label = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(packagename, PackageManager.GET_META_DATA)).toString();
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            Log.v(TAG,packagename + ":" + label);
//        }

        return packages;
    }

    /*
    public String[] getForegroundPackageNameClassNameByUsageStats(Context ctx,long start, long end) {
        String packageNameByUsageStats = null;
        String classByUsageStats = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) ctx.getSystemService("usagestats");
            final UsageEvents usageEvents = mUsageStatsManager.queryEvents(start, end);
            while (usageEvents.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    packageNameByUsageStats = event.getPackageName();
                    classByUsageStats = event.getClassName();
                    Log.d(TAG, "packageNameByUsageStats is" + packageNameByUsageStats + ", classByUsageStats is " + classByUsageStats);
                }
            }
        }
        return new String[]{packageNameByUsageStats,classByUsageStats};
    }

    public static String getForegroundPackage(UsageStatsManager usageStatsManager) {
        String packageName = null;

        final long INTERVAL = 1000 * 60;
        final long end = System.currentTimeMillis();
        final long begin = end - INTERVAL;
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            switch (event.getEventType()) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    packageName = event.getPackageName();
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    if (event.getPackageName().equals(packageName)) {
                        packageName = null;
                    }
            }
        }

        return packageName;
    }

    public static String getTopAppName(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String strName = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                strName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();

            } else {
                strName = getLollipopFGAppPackageName(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strName;
    }


    private static String getLollipopFGAppPackageName(Context ctx) {

        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) ctx.getSystemService("usagestats");
            long milliSecs = 60 * 1000;
            Date date = new Date();
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, date.getTime() - milliSecs, date.getTime());
            if (queryUsageStats.size() > 0) {
                Log.i("LPU", "queryUsageStats size: " + queryUsageStats.size());
            }
            long recentTime = 0;
            String recentPkg = "";
            for (int i = 0; i < queryUsageStats.size(); i++) {
                UsageStats stats = queryUsageStats.get(i);
                if (i == 0 && !"org.pervacio.pvadiag".equals(stats.getPackageName())) {
                    Log.i("LPU", "PackageName: " + stats.getPackageName() + " " + stats.getLastTimeStamp());
                }
                if (stats.getLastTimeStamp() > recentTime) {
                    recentTime = stats.getLastTimeStamp();
                    recentPkg = stats.getPackageName();
                }
            }
            return recentPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    */


}