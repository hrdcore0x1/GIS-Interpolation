/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gis_project;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author cory.nance
 */
public class KDTreeBuilder extends RecursiveTask<Node> {
           
    
    private final int depth;
    private final DataSet ds;
    private final int k = 3;

    public KDTreeBuilder(DataSet ds, int depth){
        this.depth = depth;
        this.ds = ds;
    }
    
    @Override
    protected Node compute() {
        //System.out.println("Starting new compute(); Threads = " + myPool.getParallelism() + " Queued: " +  myPool.getQueuedSubmissionCount() + " Active Thread: " + myPool.getActiveThreadCount());
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
            Node leftChild =  new KDTreeBuilder(DataSet.copyOfRange(ds, 0, median), depth+1).compute();
            leftChild.setParent(current);
            current.setLeftChild(leftChild);
        }
        if (ds.getSize() > median + 1) {
            Node rightChild = new KDTreeBuilder(DataSet.copyOfRange(ds, median+1, ds.getSize()), depth+1).compute();
            rightChild.setParent(current);
            current.setRightChild(rightChild);
        }
        return current;
    }
}
