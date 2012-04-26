package gis_project;

import java.util.*;

public class Neighbor {
    
    private DataPoint dp;
    private double distSq; // distance squared from test point
    private double weight;
    
    public Neighbor(DataPoint dp, double distSq) {
        this.dp = dp;
        this.distSq = distSq;
    }
    
    public DataPoint getDp() {
        return dp;
    }
    
    public double getDistSq() {
        return distSq;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public double getWeight() {
        return weight;
    }
    
    // sorts AND trims to max length nnn
    // used to find N nearest neighbors from two combined subtree searches totalling more than N results
    public static void sort(ArrayList<Neighbor> al, int nnn) {
        Collections.sort(al, new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor n1, Neighbor n2) {
                return Double.compare(n1.getDistSq(), n2.getDistSq());
            }
        });
        while (al.size() > nnn) {
            al.remove(al.size()-1);
        }
    }
}