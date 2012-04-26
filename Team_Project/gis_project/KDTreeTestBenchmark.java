package gis_project;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KDTreeTestBenchmark {

    // compares RUNNING TIME of tree search to brute force
    public static void main(String[] args) {
        
        int k = 3;
        
        int numDps[] = {100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
        for(int myi = 0; myi<7; myi++){
	System.out.println("Creating Dataset with " + numDps[myi] + " datapoints!");
	DataSet ds = new DataSet(numDps[myi], 4);
        for (int i = 0; i < numDps[myi]; i++) {
            int id = i;
            int t1 = 2009;
            int t2 = ((int) Math.floor(Math.random() * (12))) + 1;
            int t3 = ((int) Math.floor(Math.random() * (28))) + 1;
            int[] time = {t1, t2, t3};
            double x = Math.floor(Math.random() * (1001));
            double y = Math.floor(Math.random() * (1001));
            double measurement = Math.floor(Math.random() * (101));
            DataPoint dp = new DataPoint(id, time, x, y, measurement);
            ds.set(i, dp);
        }
        ds.setAllT();
        int nnn = 9;
        
        
       
        long startTime = System.currentTimeMillis();
        System.out.println("\nStart Serial tree build: " + (System.currentTimeMillis()-startTime) + " ms");
        KDTree testTree = new KDTree(ds);
        System.out.println("Finish Serial tree build: " + (System.currentTimeMillis()-startTime) + " ms\n");
         
        
        int numTestPoints = 10000;
	/*
	System.out.println("Test neighbor search using " + numTestPoints + "!");
        startTime = System.currentTimeMillis();
        System.out.println("Start neighbor search (partitioned): " + (System.currentTimeMillis()-startTime) + " ms");
        for (int i = 0; i < numTestPoints; i++) {
            int id = -1;
            int t1 = 2009;
            int t2 = ((int) Math.floor(Math.random() * (12))) + 1;
            int t3 = ((int) Math.floor(Math.random() * (28))) + 1;
            int[] time = {t1, t2, t3};
            double x = Math.floor(Math.random() * (1001));
            double y = Math.floor(Math.random() * (1001));
            double measurement = -1;
            DataPoint tp = new DataPoint(id, time, x, y, measurement);
            DataSet ts = new DataSet(1, 4);
            ts.set(0, tp);
            ts.setAllT();
            
            Neighbor[] nN = testTree.nearestNeighbors(tp, nnn);
            DataPoint[] nn = new DataPoint[nnn];
            for (int j = 0; j < nnn; j++) {
                nn[j] = (nN[j].getDp());
            }  
        }        
        System.out.println("Finish neighbor search (partitioned): " + (System.currentTimeMillis()-startTime) + " ms\n");
        
        
        startTime = System.currentTimeMillis();
        System.out.println("Start neighbor search (brute force): " + (System.currentTimeMillis()-startTime) + " ms");
        for (int i = 0; i < numTestPoints; i++) {
            int id = -1;
            int t1 = 2009;
            int t2 = ((int) Math.floor(Math.random() * (12))) + 1;
            int t3 = ((int) Math.floor(Math.random() * (28))) + 1;
            int[] time = {t1, t2, t3};
            double x = Math.floor(Math.random() * (1001));
            double y = Math.floor(Math.random() * (1001));
            double measurement = -1;
            DataPoint tp = new DataPoint(id, time, x, y, measurement);
            DataSet ts = new DataSet(1, 4);
            ts.set(0, tp);
            ts.setAllT();
            
            DataPoint[] nnBF = bruteForce(ds, tp, nnn);
        }
        System.out.println("Finish neighbor search (brute force): " + (System.currentTimeMillis()-startTime) + " ms\n");
    */    
        
        startTime = System.currentTimeMillis();
        System.out.println("Start neighbor search (brute force2): " + (System.currentTimeMillis()-startTime) + " ms");
        for (int i = 0; i < numTestPoints; i++) {
            int id = -1;
            int t1 = 2009;
            int t2 = ((int) Math.floor(Math.random() * (12))) + 1;
            int t3 = ((int) Math.floor(Math.random() * (28))) + 1;
            int[] time = {t1, t2, t3};
            double x = Math.floor(Math.random() * (1001));
            double y = Math.floor(Math.random() * (1001));
            double measurement = -1;
            DataPoint tp = new DataPoint(id, time, x, y, measurement);
            DataSet ts = new DataSet(1, 4);
            ts.set(0, tp);
            ts.setAllT();
            
            DataPoint[] nnBF = bruteForce2(ds, tp, nnn);
        }
        System.out.println("Finish neighbor search (brute force2): " + (System.currentTimeMillis()-startTime) + " ms\n");
	System.out.println("------------------------------------\n\n");
    }
    
}
    // for result comparison and performance benchmarking against tree search only
    public static DataPoint[] bruteForce(DataSet ds, DataPoint tp, int nnn) {
        double minDistance = 999999999;
        ArrayList<Neighbor> nearest = new ArrayList<Neighbor>();
        for (int i = 0; i < ds.getSize(); i++) {
            double dx = ds.getX(i) - tp.getX();
            double dy = ds.getY(i) - tp.getY();
            double dt = ds.getT(i) - tp.getT();
            double distSq = Math.sqrt(dx*dx + dy*dy + dt*dt);
            nearest.add(new Neighbor(ds.get(i), distSq));
        }
        Neighbor.sort(nearest, nnn);
        DataPoint[] dps = new DataPoint[nnn];
        for (int i = 0; i < nnn; i++) {
            dps[i] = (nearest.get(i)).getDp();
        }
        return dps;
    }
    
        // for result comparison and performance benchmarking against tree search only
    public static DataPoint[] bruteForce2(DataSet ds, DataPoint tp, int nnn) {
        double tpx = tp.getX();
        double tpy = tp.getY();
        double tpt = tp.getT();
        ArrayList<Neighbor> nearest = new ArrayList<Neighbor>();
        //final int cores = Runtime.getRuntime().availableProcessors();
        int cores = 6;
        int totalDP = ds.getData().length;
        int partitionSize = (int)Math.ceil(totalDP/cores);
        Thread[] threadPool = new Thread[cores];
        DataPoint[] dsData = ds.getData();
       // System.out.println("Max Data = " + totalDP);
        nearest.ensureCapacity(totalDP);
        int upperBound;
        for (int start=0, currentCore=0; currentCore<cores; start+=(partitionSize + 1), currentCore++){
            upperBound = (start+partitionSize > totalDP) ? totalDP : start+partitionSize;
             DataPoint[] threadDS = Arrays.copyOfRange(dsData, start, upperBound);
      //      System.out.println("Creating thread: " + currentCore);
             int len = start + partitionSize;
//            System.out.println("Data Range is: " + start + " - " + len);
  //          System.out.println("-------");
             threadPool[currentCore] = new Thread(new bruteForceWorker(threadDS, tpx, tpy, tpt, nearest));
             threadPool[currentCore].start();
        }
        
        for (int currentCore = 0; currentCore < cores; currentCore++){
            try {
//                System.out.println("Waiting on thread #" + currentCore);
                threadPool[currentCore].join();
            } catch (InterruptedException ex) {
                System.out.println("InterruptedException: " + ex.toString());
            }            
        }
        //System.out.println(nearest.size());
        nearest.trimToSize();
        nearest.removeAll(Collections.singleton(null));
	Neighbor.sort(nearest, nnn);
        DataPoint[] dps = new DataPoint[nnn];
        for (int i = 0; i < nnn; i++) {
            dps[i] = (nearest.get(i)).getDp();
        }
        //System.out.println("DPS Size = " + dps.length + "\n\n-----------------------------------------------\n\n");
        return dps;
    }
}

class bruteForceWorker implements Runnable {


    ArrayList<Neighbor> nearest;
    private DataPoint[] myDataSet;
    private double tpx;
    private double tpy;
    private double tpt;
    
    public bruteForceWorker(DataPoint[] myDataSet, double tpx, double tpy, double tpt, ArrayList<Neighbor> nearest){
        this.nearest = nearest;
        this.myDataSet = myDataSet;
        this.tpx = tpx;
        this.tpy = tpy;
        this.tpt = tpt;
    }
    
    @Override
    public void run() {
                int len = myDataSet.length;
                double dx;
                double dy;
                double dt;
                double distSq;
                DataPoint current;
		ArrayList<Neighbor> myNearest = new ArrayList<Neighbor>();                
                for (int i=0; i<len; i++){
                    current = myDataSet[i];
                    dx = current.getX() + tpx;
                    dy = current.getY() + tpy;
                    dt = current.getT() + tpt;
                    distSq = Math.sqrt((dx*dx) + (dy*dy) + (dt*dt));
                    myNearest.add(new Neighbor(current, distSq));
                }
		nearest.addAll(myNearest);
    }
    
}
