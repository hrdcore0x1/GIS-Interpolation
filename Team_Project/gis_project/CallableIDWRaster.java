/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gis_project;

import java.util.concurrent.Callable;

/**
 *
 * @author cory
 */
public class CallableIDWRaster implements Callable<DataSet> {

    private DataSet ds;
    private KDTree inputTree;
    private int nnn;
    private double exp;

    public CallableIDWRaster(KDTree inputTree, DataSet ds, int nnn, double exp) {
        this.ds = ds;
        this.inputTree = inputTree;
        this.nnn = nnn;
        this.exp = exp;
    }

    @Override
    public DataSet call() throws Exception {
        int outputSize = ds.getData().length;
        
        for (int i = 0; i < outputSize; i++) {
            DataPoint tp = ds.get(i);
            Neighbor[] nn = inputTree.nearestNeighbors(tp, nnn);
            double interpVal = IDW.calc(tp, nn, exp);
            tp.setMeasurement(interpVal);
            PM25GUI.percentComplete2Increment1();
        }
        return ds;
    }
}
