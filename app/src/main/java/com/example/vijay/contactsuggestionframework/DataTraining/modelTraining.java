package com.example.vijay.contactsuggestionframework.DataTraining;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.example.vijay.contactsuggestionframework.DataPrediction.modelPrediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class modelTraining extends BroadcastReceiver {

    private static String TAG = "modelTraining";
    private static String MODEL_PATH = Environment.getExternalStorageDirectory().toString()+"/CONTACT_DATA/";
    private static final String TRAININGFILE = "training.arff";
    private static String MODELFILE = "model.txt";
    File modelsavedfile;
    modelPrediction modelpred ;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        startTraining();
//        startTrainingCluster();
    }


    public synchronized void startTraining(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MODEL_PATH+File.separator+TRAININGFILE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Instances inst = null;

        try {
            inst = new Instances(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleKMeans simpleKMeans = new SimpleKMeans();
        try {
            simpleKMeans.setNumClusters(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            simpleKMeans.buildClusterer(inst);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Save data frame to model.arff*/
        File datapathFiles = new File(MODEL_PATH);
        if(!datapathFiles.exists()){
            datapathFiles.mkdir();
        }
        modelsavedfile = new File(MODEL_PATH+File.separator+MODELFILE);
        if(modelsavedfile.exists()){
            modelsavedfile.delete();
        }else{
            try {
                modelsavedfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // serialize model to the model path
        try {
            FileOutputStream fout=new FileOutputStream(modelsavedfile);
            weka.core.SerializationHelper.write(fout, simpleKMeans);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Test Prediction*/
        int day = 6;
        int hour = 12;
        String caller = ""; //could be null
        int []prediction;
        String[]contact;
        modelpred = new modelPrediction();
        prediction = modelpred.getCurrentPrediction(day,hour,caller);
        contact = new String[prediction.length];

    }

//    public void startTrainingCluster(){
//
//        BufferedReader reader = null;
//        Instances dataClusterer = null;
//        try {
//            reader = new BufferedReader(new FileReader(MODEL_PATH+File.separator+TRAININGFILE));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        // generate data for clusterer (w/o class)
//        // load data
//        Instances data = null;
//        try {
//            data = new Instances(reader);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Remove filter = new Remove();
//        filter.setAttributeIndices("" + (data.classIndex() + 1));
//        try {
//            filter.setInputFormat(data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            dataClusterer = Filter.useFilter(data, filter);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        SimpleKMeans simpleKMeans = new SimpleKMeans();
//        try {
//            simpleKMeans.setNumClusters(5);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            simpleKMeans.buildClusterer(dataClusterer);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        /*Save data frame to model.arff*/
//        File datapathFiles = new File(MODEL_PATH);
//        if(!datapathFiles.exists()){
//            datapathFiles.mkdir();
//        }
//        modelsavedfile = new File(MODEL_PATH+File.separator+MODELFILE);
//        if(modelsavedfile.exists()){
//            modelsavedfile.delete();
//        }else{
//            try {
//                modelsavedfile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        // serialize model
//        try {
//            FileOutputStream fout=new FileOutputStream(modelsavedfile);
//            weka.core.SerializationHelper.write(fout, simpleKMeans);
//            fout.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
////        readModelFile();
//    }
}
