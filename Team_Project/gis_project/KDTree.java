package gis_project;

import java.util.*;

public class KDTree {

    private int k;
    private int size;
    private Node root;
    
    public KDTree(DataSet ds) {
        k = 3;
        root = buildTree(ds, 0);
    }
    
    private Node buildTree(DataSet ds, int depth) {
        final int axis = depth % k;
        
        Arrays.sort(ds.getData(), new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint dp1 , DataPoint dp2) {
                return Double.compare(dp1.getAxis(axis), dp2.getAxis(axis));
            }
        });
        int median = (ds.getSize() / 2);
        
        // make sure that all locations >= median are partitioned to right child
        while (median > 0 && ds.get(median-1).getAxis(axis) == ds.get(median).getAxis(axis)) {
            median--;
        }
        
        Node current = new Node(ds.get(median));
        if (median > 0) {
            Node leftChild =  buildTree(DataSet.copyOfRange(ds, 0, median), depth+1);
            leftChild.setParent(current);
            current.setLeftChild(leftChild);
        }
        if (ds.getSize() > median + 1) {
            Node rightChild = buildTree(DataSet.copyOfRange(ds, median+1, ds.getSize()), depth+1);
            rightChild.setParent(current);
            current.setRightChild(rightChild);
        }
        return current;
    }
    
    public Neighbor[] nearestNeighbors(DataPoint dp, int nnn) {
        Neighbor[] nn = new Neighbor[nnn];
        ArrayList<Neighbor> al = nearestNeighbors(dp, nnn, root, 0);
        for (int i = 0; i < nnn; i++) {
            nn[i] = (al.get(i));
        }
        return nn;
    }
    
    public ArrayList<Neighbor> nearestNeighbors(DataPoint dp, int nnn, Node start, int depth) {
        Node current = start;
        Node skippedSubRoot;
        ArrayList<Neighbor> nearest = new ArrayList<Neighbor>(6);
        nearest.add(new Neighbor(current.getDp(), distanceSquared(current.getDp(), dp)));
        ArrayList<Neighbor> nearestAlt = new ArrayList<Neighbor>(6);
        
        // "goal" distance is the highest distance of the nearest neighbor list
        // any new neighbor with a lower distance can squeeze into the list
        double goalDistSq = distanceSquared(dp, (nearest.get(0)).getDp());
        double testDistSq;
        int axis;
        int k = this.k;
        
        // search down
        while (current.getLeftChild() != null || current.getRightChild() != null) { // until a leaf node is found
            axis = depth % k;
            if (dp.getAxis(axis) < current.getDp().getAxis(axis)) {
                if (current.getLeftChild() != null) {
                    current = current.getLeftChild();
                } else { // must continue until leaf node, so go other way
                    current = current.getRightChild();
                }
            } else {
                if (current.getRightChild() != null) {
                    current = current.getRightChild();
                } else { // must continue until leaf node, so go other way
                    current = current.getLeftChild();
                }
            }
            testDistSq = distanceSquared(dp, current.getDp());
            if (testDistSq < goalDistSq || nearest.size() < nnn) {
                nearest.add(new Neighbor(current.getDp(), testDistSq));
                Neighbor.sort(nearest, nnn);
                goalDistSq = (nearest.get(nearest.size()-1)).getDistSq();
            }
            depth++;
        }
        // backtrack and check abandoned sub-trees
        while (current != start) {
            depth--;
            current = current.getParent();
            axis = depth % k;
            testDistSq = Math.pow((current.getDp().getAxis(axis) - dp.getAxis(axis)),2);
            if (testDistSq < goalDistSq || nearest.size() < nnn) {
                if (dp.getAxis(axis) < current.getDp().getAxis(axis)) {
                    // must check BOTH children are valid...
                    // otherwise you'll go down the same path you just came up
                    // and visit the same node twice (BAD)
                    if (current.getLeftChild() != null && current.getRightChild() != null) {
                        skippedSubRoot = current.getRightChild();
                    } else {
                        continue; // continue unwinding, cannot dig toward skipped side
                    }
                } else {
                    if (current.getLeftChild() != null && current.getRightChild() != null) {
                        skippedSubRoot = current.getLeftChild();
                    } else {
                        continue; // continue unwinding, cannot dig toward skipped side
                    }
                }
                nearestAlt = nearestNeighbors(dp, nnn, skippedSubRoot, depth+1);
                nearest.addAll(nearestAlt);
                Neighbor.sort(nearest, nnn);
                goalDistSq = (nearest.get(nearest.size()-1)).getDistSq(); // unneeded?
            }
        }
        return nearest;
    }
    
    private double distanceSquared(DataPoint dp1, DataPoint dp2) {
        double distanceSquared = 0;
        distanceSquared += Math.pow((dp1.getX() - dp2.getX()), 2);
        distanceSquared += Math.pow((dp1.getY() - dp2.getY()), 2);
        distanceSquared += Math.pow((dp1.getT() - dp2.getT()), 2);
        return distanceSquared;
    }
}