package gis_project;

import java.util.*;

public class KDTreeTestCompare {

    // compares RESULTS of tree search to brute force
    public static void main(String[] args) {
        
        System.out.println("\nAny difference between tree search and brute force listed below:");
        System.out.println("(these appear to only be equidistant points chosen in different order)");
        int numTestSetups = 1000;
        for (int n = 0; n < numTestSetups; n++) {

            int k = 3;
            
            int numDps = 1000;
            DataSet ds = new DataSet(numDps, 4);
            for (int i = 0; i < numDps; i++) {
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
            
            KDTreeParallel testTree = new KDTreeParallel(ds);
            int nnn = 9;
            
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
            for (int i = 0; i < nnn; i++) {
                nn[i] = (nN[i].getDp());
            }            
            DataPoint[] nnBF = bruteForce(ds, tp, nnn);
            
            if (nn[0] != nnBF[0] ||
                nn[1] != nnBF[1] ||
                nn[2] != nnBF[2] ||
                nn[3] != nnBF[3] ||
                nn[4] != nnBF[4] ||
                nn[5] != nnBF[5] ||
                nn[6] != nnBF[6] ||
                nn[7] != nnBF[7] ||
                nn[8] != nnBF[8]) {
                System.out.println("");
                System.out.println("Test Point:         X = " + tp.getX() + ", Y = " + tp.getY() + ", T = " + tp.getT());
                System.out.println("");
                for (int i = 0; i < nnn; i++) {
                    double dx1 = tp.getX() - nn[i].getX();
                    double dy1 = tp.getY() - nn[i].getY();
                    double dt1 = tp.getT() - nn[i].getT();
                    double distance1 = Math.sqrt(dx1*dx1 + dy1*dy1 + dt1*dt1);
                    System.out.println("NN (tree search) " + (i+1) + ": X = " + nn[i].getX() + ", Y = " + nn[i].getY() + ", T = " + nn[i].getT() + ", D = " + distance1);
                }
                System.out.println("");
                for (int i = 0; i < nnn; i++) {
                    double dx2 = tp.getX() - nnBF[i].getX();
                    double dy2 = tp.getY() - nnBF[i].getY();
                    double dt2 = tp.getT() - nnBF[i].getT();
                    double distance2 = Math.sqrt(dx2*dx2 + dy2*dy2 + dt2*dt2);
                    System.out.println("NN (brute force) " + (i+1) + ": X = " + nnBF[i].getX() + ", Y = " + nnBF[i].getY() + ", T = " + nnBF[i].getT() + ", D = " + distance2);
                }
            }
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
}