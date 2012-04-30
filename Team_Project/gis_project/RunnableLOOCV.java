/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gis_project;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cory
 */
public class RunnableLOOCV implements Runnable {

    private int size;
    private DataSet ds;
    private BufferedWriter bw;
    private KDTree tree;
    private double e[];
    private int n[];

    private double va[][];
    
    
    public RunnableLOOCV(int size, BufferedWriter bw, KDTree tree, double va[][]) {
        this.size = size;
        this.bw = bw;
        this.tree = tree;
        this.e = LOOCV.e;
        this.n = LOOCV.n;
        this.va = va;
    }

    @Override
    public void run(){

        DataPoint tp;
        double len = ds.getSize();
        for (int i = 0; i < len; i++) {
            try {
                tp = ds.get(i);
                bw.write(String.format("%-9.1f", tp.getMeasurement()));
                for (int j = 0; j < n.length; j++) {
                    Neighbor[] na = tree.nearestNeighbors(tp, n[j] + 1); // find n+1 neighbors since "closest" neighbor will be self
                    na = Arrays.copyOfRange(na, 1, na.length);         // remove first neighbor (self)
                    for (int k = 0; k < e.length; k++) {
                        double interpVal = IDW.calc(tp, na, e[k]);
                        bw.write(String.format("%-7.1f", interpVal));
                        va[i][j * e.length + k] = interpVal;
                        PM25GUI.percentComplete3Increment1();
                    }
                }
                bw.write("\n");
                bw.flush();
            } catch (IOException ex) {
                System.err.println("Error: " + ex.toString());
            }
        }
    }
}
