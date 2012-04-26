package gis_project;

import java.util.*;

public class IDWTest {

    // shows that finding neighbors is an order of magnitude more costly than the actual IDW
    // so, for LOOCV when nested loops are necessary for multiple IDW calcs on each input data point,
    // make sure neighbor search is done in outer loop
    public static void main(String[] args) {

        int k = 3;
        
        int numDps = 146000;
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

        long time1 = System.nanoTime();
        Neighbor[] nN = testTree.nearestNeighbors(tp, nnn);
        long time2 = System.nanoTime();
        
        DataPoint[] nn = new DataPoint[nnn];
        for (int i = 0; i < nnn; i++) {
            nn[i] = (nN[i].getDp());
        }            

        System.out.println("");
        System.out.println("Test Point:         X = " + tp.getX() + ", Y = " + tp.getY() + ", T = " + tp.getT());
        System.out.println("");
        for (int i = 0; i < nnn; i++) {
            double dx1 = tp.getX() - nn[i].getX();
            double dy1 = tp.getY() - nn[i].getY();
            double dt1 = tp.getT() - nn[i].getT();
            double distance1 = Math.sqrt(dx1*dx1 + dy1*dy1 + dt1*dt1);
            System.out.println("neighbor value = " + nn[i].getMeasurement() + " (X = " + nn[i].getX() + ", Y = " + nn[i].getY() + ", T = " + nn[i].getT() + ", D = " + distance1 + ")");
        }
        System.out.println("");
        
        long time3 = System.nanoTime();
        double interpVal = IDW.calc(tp, nN, 2);
        long time4 = System.nanoTime();
        System.out.println("Interpolated value = " + interpVal);
        
        System.out.println("\nTime to find neighbors:   " + (time2-time1) + " ns");
        System.out.println("Time to do interpolation: " + (time4-time3) + " ns");
    }
}