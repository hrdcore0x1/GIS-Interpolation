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
public class CallableScaledData implements Callable<DataSet> {

    private DataSet ds;
    private int xyscale;
    private int yoffset;
    private int xoffset;

    public CallableScaledData(DataSet ds, int xyscale, int yoffset, int xoffset) {
        this.ds = ds;
        this.xyscale = xyscale;
        this.yoffset = yoffset;
        this.xoffset = xoffset;
    }

    @Override
    public DataSet call() throws Exception {
        int outputSize = ds.getSize();
        for (int j = 0; j < outputSize; j++) {
            ds.get(j).setX(ds.get(j).getX() * xyscale);
            ds.get(j).setY(ds.get(j).getY() * xyscale);
            ds.get(j).setX(ds.get(j).getX() + xoffset);
            ds.get(j).setY(ds.get(j).getY() + yoffset);
        }
        return ds;
    }
}
