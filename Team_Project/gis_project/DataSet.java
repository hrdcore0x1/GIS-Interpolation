package gis_project;

import java.util.Arrays;


public class DataSet {

    private int size;
    private int timeDomain;

    public double getTimeScale() {
        return timeScale;
    }
    private DataPoint[] data;
    
    // for "open research question to address" regarding how time scale affects interpolation
    private double timeScale = .0316;
    
    public DataSet(int size, int timeDomain) {
        this.size = size;
        this.timeDomain = timeDomain;
        this.data = new DataPoint[size];
    }
    
    public DataSet(DataSet sourceCopy) {
        this.size = sourceCopy.size;
        this.timeDomain = sourceCopy.timeDomain;
        DataPoint[] dps = new DataPoint[size];
        for (int i = 0; i < sourceCopy.size; i++) {
            int id = sourceCopy.get(i).getId();
            int[] time = new int[sourceCopy.get(i).getTime().length];
            for (int j = 0; j < sourceCopy.get(i).getTime().length; j++) {
                time[j] = sourceCopy.get(i).getTime()[j];
            }
            double x = sourceCopy.get(i).getX();
            double y = sourceCopy.get(i).getY();
            double t = sourceCopy.get(i).getT();
            double measurement = sourceCopy.get(i).getMeasurement();
            DataPoint dp = new DataPoint(id, time, x, y, measurement);
            dp.setT(t);
            dps[i] = dp;
        }
        this.data = dps;
    }
    
    public static DataSet combineDataSet(DataSet a, DataSet b){
        //System.out.println("A_len/size: " + a.data.length + "/" + a.size +" | B_len/size: " + b.data.length + "/" + b.size);
        int size = a.size + b.size;
        //System.out.println("Size of ds = " + size);
        if (a.timeDomain != b.timeDomain){
            //this shouldn't happen but if it does...
            System.out.println("DataSet A's timedomain doesn't match DataSet B's timedomain!!");
            return null;
        }
        DataSet ds = new DataSet(size, a.timeDomain);
        ds.data = DataSet.concat(a.data, b.data);
        //System.out.println("DS_len/size: " + ds.data.length + "/" + ds.size);
        return ds;
    }
    
    public static <T> T[] concat(T[] first, T[] second){
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    
    public static DataSet copyOfRange(DataSet original, int from, int to) {
        DataSet copy = new DataSet(to-from, original.timeDomain);
        copy.data = Arrays.copyOfRange(original.data, from, to);
        return copy;
    }
    
    public int timeUnitQty() {
        if (timeDomain == 1) {
            return 1;
        } else if (timeDomain == 2 || timeDomain == 3) {
            return 2;
        } else if (timeDomain == 4) {
            return 3;
        } else {
            return -1;
        }
    }
    
    public int startYear() {
        return (data[0].getTime())[0];
    }
    
    public int endYear() {
        return (data[size-1].getTime())[0];
    }
    
    public int numYears() {
        return (endYear() - startYear() + 1);
    }
    
    public double getX(int index) {
        return data[index].getX();
    }
    
    public double getY(int index) {
        return data[index].getY();
    }
    
    // return will be useless if called before calling setAllT()
    public double getT(int index) {
        return data[index].getT();
    }
    
    // set t for all DataPoints in set
    // value relies on timeDomain and numYears()
    public void setAllT() {
        int startYear = this.startYear();
        for (int i = 0; i < size; i++) {
            DataPoint dp = data[i];
            int dpYear = dp.getTime()[0];
        
            if (timeDomain == 1) {
                dp.setT(dpYear - startYear + 1);
            } else if (timeDomain == 2) {
                dp.setT((12 * (dpYear - startYear)) + (dp.getTime())[1]);
            } else if (timeDomain == 3) {
                dp.setT((4 * (dpYear - startYear)) + (dp.getTime())[1]);
            } else { // (timeDomain == 4)
                int sum1 = (365 * (dpYear - startYear));
                int month = (dp.getTime())[1];
                int sum2 = 0;
                if (month == 1) {
                    sum2 = 0;
                } else if (month == 2) {
                    sum2 = 31;
                } else if (month == 3) {
                    sum2 = 59;
                } else if (month == 4) {
                    sum2 = 90;
                } else if (month == 5) {
                    sum2 = 120;
                } else if (month == 6) {
                    sum2 = 151;
                } else if (month == 7) {
                    sum2 = 181;
                } else if (month == 8) {
                    sum2 = 212;
                } else if (month == 9) {
                    sum2 = 243;
                } else if (month == 10) {
                    sum2 = 273;
                } else if (month == 11) {
                    sum2 = 304;
                } else if (month == 12) {
                    sum2 = 334;
                }
                int sum3 = (dp.getTime())[2];
                dp.setT(sum1 + sum2 + sum3);
            }
        }
        scaleT();
    }
    
    // for "open research question to address" regarding how time scale affects interpolation
    public void scaleT() {
        for (int i = 0; i < size; i++) {
            DataPoint dp = data[i];
            dp.setT(dp.getT() * timeScale);
        }
    }
    
    // prints [x, y, t] (encoded time values)
    public void printEnc() {
        System.out.printf("%-12s%-12s%-12s\n", "x", "y", "t");
        for (int i = 0; i < size; i++) {
            System.out.printf("%-12f%-12f%-12f\n", getX(i), getY(i), getT(i));
        }
    }
    
    // prints [id, time(domain), x, y, measurement] (raw time values)
    public void print() {
        if (timeDomain == 1) {
            System.out.printf("%-12s%-6s%-12s%-12s%-6s\n", "id", "year", "x", "y", "pm25");
            for (int i = 0; i < size; i++) {
                System.out.printf("%-12d%-6d%-12f%-12f%-6.1f\n", data[i].getId(), data[i].getTime()[0], getX(i), getY(i), data[i].getMeasurement());
            }
        } else if (timeDomain == 2) {
            System.out.printf("%-12s%-6s%-6s%-12s%-12s%-6s\n", "id", "year", "month", "x", "y", "pm25");
            for (int i = 0; i < size; i++) {
                System.out.printf("%-12d%-6d%-6d%-12f%-12f%-6.1f\n", data[i].getId(), data[i].getTime()[0], data[i].getTime()[1], getX(i), getY(i), data[i].getMeasurement());
            }
        } else if (timeDomain == 3) {
            System.out.printf("%-12s%-6s%-6s%-12s%-12s%-6s\n", "id", "year", "quarter", "x", "y", "pm25");
            for (int i = 0; i < size; i++) {
                System.out.printf("%-12d%-6d%-6d%-12f%-12f%-6.1f\n", data[i].getId(), data[i].getTime()[0], data[i].getTime()[1], getX(i), getY(i), data[i].getMeasurement());
            }
        } else if (timeDomain == 4) {
            System.out.printf("%-12s%-6s%-6s%-6s%-12s%-12s%-6s\n", "id", "year", "month", "day", "x", "y", "pm25");
            for (int i = 0; i < size; i++) {
                System.out.printf("%-12d%-6d%-6d%-6d%-12f%-12f%-6.1f\n", data[i].getId(), data[i].getTime()[0], data[i].getTime()[1], data[i].getTime()[2], getX(i), getY(i), data[i].getMeasurement());
            }
        }
    }

    public void set(int index, DataPoint dp) {
        data[index] = dp;
    }
    
    public DataPoint get(int index) {
        return data[index];
    }
    
    public int getSize() {
        return size;
    }
    
    public int getTimeDomain() {
        return timeDomain;
    }
    
    public DataPoint[] getData() {
        return data;
    }
    
    public void setData(DataPoint[] data) {
        this.data = data;
    }
}