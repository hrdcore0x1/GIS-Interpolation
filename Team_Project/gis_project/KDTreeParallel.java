package gis_project;


import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class KDTreeParallel {

    /* Added for concurrency */
        /* Intels hyperthreading technology allows this function to return 2x the number of cores, even though there are 2x core's preformance will decrease
        *  because these extra threads only allow a 20% improvement over 1 thread per core.
        * TODO: See if there is a way to detect hyperthreading and divide by 2 for PROCESSORS
      */
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private final ForkJoinPool myPool = new ForkJoinPool(PROCESSORS);
   
    
    /* End concurrency */
    
    private int k;
    private int size;
    private Node root;
    
    public KDTreeParallel(DataSet ds) {
        super();
        //System.out.println("Using " + PROCESSORS + " processors...");
        k = 3;
        root = buildTree(ds, 0);
    }
    
    private Node buildTree(DataSet ds, int depth) {     
        KDTreeBuilder myBuilder = new KDTreeBuilder(ds, depth);
        return myPool.invoke(myBuilder);
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