package gis_project;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class LOOCV {

    private static int[] n = {3,4,5,6,7};
    private static double[] e  = {1.0,1.5,2.0,2.5,3.0,3.5,4.0,4.5,5.0};
    
    private static double percentComplete;
    
    public static double[][] calc(DataSet ds, KDTree tree, final JLabel status) {
        
        double[][] va = new double[ds.getSize()][n.length*e.length];
        
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File("loocv_idw.txt")));
            bw.write(String.format("%-9s", "original"));
            for (int j = 0; j < n.length; j++) {
                for (int k = 0; k < e.length; k++) {
                    bw.write(String.format("%-1s", "n"));
                    bw.write(String.format("%-1d", n[j]));
                    bw.write(String.format("%-1s", "e"));
                    bw.write(String.format("%-1.1f", e[k]));
                    bw.write(" ");
                }
            }
            bw.write("\n");
            for (int i = 0; i < ds.getSize(); i++) {
                percentComplete = ((double)i/(double)ds.getSize()) * 100;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        status.setText("<html>Validating results...<br/>[" + String.format("%2.1f", percentComplete) + "% complete]");
                    }
                });
                DataPoint tp = ds.get(i);
                bw.write(String.format("%-9.1f", tp.getMeasurement()));
                for (int j = 0; j < n.length; j++) {
                    Neighbor[] na = tree.nearestNeighbors(tp, n[j]+1); // find n+1 neighbors since "closest" neighbor will be self
                    na = Arrays.copyOfRange(na, 1, na.length);         // remove first neighbor (self)
                    for (int k = 0; k < e.length; k++) {
                        double interpVal = IDW.calc(tp, na, e[k]);
                        bw.write(String.format("%-7.1f", interpVal));
                        va[i][j*e.length + k] = interpVal;
                    }
                }
                bw.write("\n");
            }
            bw.close();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    status.setText("<html>Results written to<br/>loocv_idw.txt</html>");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return va;
    }
    
    public static void errorCalc(DataSet ds, double[][] loocv, final JLabel status) {
    
        double[] mae = new double[loocv[0].length];
        double[] mse = new double[loocv[0].length];
        double[] rmse = new double[loocv[0].length];
        double[] mare = new double[loocv[0].length];
        double[] msre = new double[loocv[0].length];
        double[] rmsre = new double[loocv[0].length];
        
        for (int i = 0; i < mae.length; i++) {
            mae[i] = 0;
        }
        for (int i = 0; i < mse.length; i++) {
            mse[i] = 0;
        }
        for (int i = 0; i < rmse.length; i++) {
            rmse[i] = 0;
        }
        for (int i = 0; i < mare.length; i++) {
            mare[i] = 0;
        }
        for (int i = 0; i < msre.length; i++) {
            msre[i] = 0;
        }
        for (int i = 0; i < rmsre.length; i++) {
            rmsre[i] = 0;
        }
        
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File("error_statistics_idw.txt")));
            bw.write(String.format("%-7s", ""));
            for (int j = 0; j < n.length; j++) {
                for (int k = 0; k < e.length; k++) {
                    bw.write(String.format("%-1s", "n"));
                    bw.write(String.format("%-1d", n[j]));
                    bw.write(String.format("%-1s", "e"));
                    bw.write(String.format("%-1.1f", e[k]));
                    bw.write(" ");
                }
            }
            bw.write("\n");
            
            for (int i = 0; i < ds.getSize() ; i++) {
                percentComplete = ((double)i/(double)ds.getSize()) * 100;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        status.setText("<html>Calculating error...<br/>[" + String.format("%2.1f", percentComplete) + "% complete]");
                    }
                });
                DataPoint tp = ds.get(i);
                for (int j = 0; j < loocv[0].length; j++) {
                    mae[j] += Math.abs(loocv[i][j] - tp.getMeasurement());
                    mse[j] += Math.pow((loocv[i][j] - tp.getMeasurement()), 2);
                    mare[j] += (Math.abs(loocv[i][j] - tp.getMeasurement())) / tp.getMeasurement();
                    msre[j] += (Math.pow((loocv[i][j] - tp.getMeasurement()), 2)) / tp.getMeasurement();
                }
            }
            bw.write(String.format("%-7s", "MAE"));
            for (int i = 0; i < mae.length; i++) {
                mae[i] /= ds.getSize();
                bw.write(String.format("%-7.1f", mae[i]));
            }
            bw.write("\n");
            bw.write(String.format("%-7s", "MSE"));
            for (int i = 0; i < mse.length; i++) {
                mse[i] /= ds.getSize();
                bw.write(String.format("%-7.1f", mse[i]));
            }
            bw.write("\n");
            bw.write(String.format("%-7s", "RMSE"));
            for (int i = 0; i < rmse.length; i++) {
                rmse[i] = Math.sqrt(mse[i]);
                bw.write(String.format("%-7.1f", rmse[i]));
            }
            bw.write("\n");
            bw.write(String.format("%-7s", "MARE"));
            for (int i = 0; i < mare.length; i++) {
                mare[i] /= ds.getSize();
                bw.write(String.format("%-7.1f", mare[i]));
            }
            bw.write("\n");
            bw.write(String.format("%-7s", "MSRE"));
            for (int i = 0; i < msre.length; i++) {
                msre[i] /= ds.getSize();
                bw.write(String.format("%-7.1f", msre[i]));
            }
            bw.write("\n");
            bw.write(String.format("%-7s", "RMSRE"));
            for (int i = 0; i < rmsre.length; i++) {
                rmsre[i] = Math.sqrt(msre[i]);
                bw.write(String.format("%-7.1f", rmsre[i]));
            }
            bw.write("\n");
            bw.close();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    status.setText("<html>Results written to<br/>error_statistics_idw.txt</html>");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return;
    }
}