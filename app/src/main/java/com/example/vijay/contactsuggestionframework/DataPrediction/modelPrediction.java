package com.example.vijay.contactsuggestionframework.DataPrediction;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ProtectedProperties;

public class modelPrediction {
    private static String TAG = "modelPrediction";
    private static String MODEL_PATH = Environment.getExternalStorageDirectory().toString()+"/CONTACT_DATA/";
    private static String MODELFILE = "model.txt";

    public void predictModelFile(){

        Instances trainingset;
        int NUMBEROFINSTANCES = 1;
        int NUMBEROFATTR = 3;
        int ATTR = 2; //Contact Name or Number
        SimpleKMeans simpleKMeans = null;
        try {
            simpleKMeans = (SimpleKMeans) weka.core.SerializationHelper.read(MODEL_PATH+File.separator+MODELFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(simpleKMeans);

        int day = 6;
        int hour = 12;
        String caller = "+919873049369";

        Attribute attribute1 = new Attribute("DAY");
        Attribute attribute2 = new Attribute("HOUR");
        Attribute attribute3 = new Attribute("C_NAME");

        ArrayList<Attribute> attributeArrayList = new ArrayList<>(NUMBEROFATTR);
        attributeArrayList.add(attribute1);
        attributeArrayList.add(attribute2);
        attributeArrayList.add(attribute3);

        trainingset = new Instances("testing",attributeArrayList,NUMBEROFINSTANCES);

        Instance instance = new DenseInstance(NUMBEROFATTR);
        instance.setValue(attribute1,day);
        instance.setValue(attribute2,hour);
        instance.setValue(attribute3,caller.hashCode());

        trainingset.add(instance);

        try {
            clusterEvaluation.evaluateClusterer(trainingset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int []clusterAssigned;
        Instances centindex = null;
        int id;

        int totalCluster = clusterEvaluation.getNumClusters();
        double[] cluster = clusterEvaluation.getClusterAssignments();
        clusterAssigned = new int[cluster.length];
        for(int i = 0 ; i < cluster.length ; i++) {
            clusterAssigned[i] = (int) cluster[i];
        }
        centindex = simpleKMeans.getClusterCentroids();

        int attributes = centindex.numAttributes();
        int numinst = centindex.numInstances();
        for(int i =0 ; i < clusterAssigned.length ; i++) {
            id = (int) centindex.instance(clusterAssigned[i]).value(2);
        }

        SimpleKMeans Dist = (SimpleKMeans)simpleKMeans.getDistanceFunction();
        double value;
        for(int i = 0 ; i < centindex.numInstances(); i++){
            // for each cluster center
            Instance inst = centindex.instance( i );
            value = inst.value( ATTR );
            java.lang.System.out.println( "Value for centroid " + i + ": " + value );
        }


    }

    public int[] getCurrentPrediction(int day, int hour, String contact){

        Instances trainingset;
        int NUMBEROFINSTANCES = 1;
        int NUMBEROFATTR = 3;
        int []result;
        int ATTR = 2; //Contact Name or Number

        SimpleKMeans simpleKMeans = null;
        try {
            simpleKMeans = (SimpleKMeans) weka.core.SerializationHelper.read(MODEL_PATH+File.separator+MODELFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(simpleKMeans);

        Attribute attribute1 = new Attribute("DAY");
        Attribute attribute2 = new Attribute("HOUR");
        Attribute attribute3 = new Attribute("C_NAME");

        ArrayList<Attribute> attributeArrayList = new ArrayList<>(NUMBEROFATTR);
        attributeArrayList.add(attribute1);
        attributeArrayList.add(attribute2);
        attributeArrayList.add(attribute3);

        trainingset = new Instances("testing",attributeArrayList,NUMBEROFINSTANCES);

        Instance instance = new DenseInstance(NUMBEROFATTR);
        instance.setValue(attribute1,day);
        instance.setValue(attribute2,hour);
        instance.setValue(attribute3,contact.hashCode());

        trainingset.add(instance);

        try {
            clusterEvaluation.evaluateClusterer(trainingset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances centindex = null;
        centindex = simpleKMeans.getClusterCentroids();
        result = new int[centindex.numInstances()];

        double value;

        for(int i = 0 ; i < centindex.numInstances(); i++){
            // for each cluster center
            Instance inst = centindex.instance( i );
            value = inst.value( ATTR );
            result[i] = (int)value;
            java.lang.System.out.println( "Value for centroid " + i + ": " + value );
        }
        return result;
    }


}
