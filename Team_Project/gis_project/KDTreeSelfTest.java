package gis_project;

import java.util.*;

public class KDTreeSelfTest {

    // shows that an input datapoint is its own nearest neighbor (must be removed from nn set for LOOCV)
    public static void main(String[] args) {
        
        int k = 3;
        
        DataSet ds = new DataSet(5, 4);

        int[] time = {0, 0, 0};
        DataPoint dp1 = new DataPoint(1, time, 1, 1, 0);
        DataPoint dp2 = new DataPoint(1, time, 2, 2, 0);
        DataPoint dp3 = new DataPoint(1, time, 3, 3, 0);
        DataPoint dp4 = new DataPoint(1, time, 4, 4, 0);
        DataPoint dp5 = new DataPoint(1, time, 5, 5, 0);
        ds.set(0, dp1);
        ds.set(1, dp2);
        ds.set(2, dp3);
        ds.set(3, dp4);
        ds.set(4, dp5);

        ds.setAllT();
        
        KDTreeParallel testTree = new KDTreeParallel(ds);
        
        int nnn = 4;
        
        DataPoint tp = dp1;

        Neighbor[] nN = testTree.nearestNeighbors(tp, nnn);

        System.out.println("\nNearest neighbors of [1.0, 1.0]:");
        for (int i = 0; i < nnn; i++) {
            DataPoint p = (nN[i].getDp());
            System.out.println(p.getX() + ", " + p.getY());
        }
        nN = Arrays.copyOfRange(nN, 1, nN.length);
        System.out.println();
        
        System.out.println("After removing \"self\" from set:");
        for (int i = 0; i < nN.length; i++) {
            DataPoint p = (nN[i].getDp());
            System.out.println(p.getX() + ", " + p.getY());
        }
    }
}