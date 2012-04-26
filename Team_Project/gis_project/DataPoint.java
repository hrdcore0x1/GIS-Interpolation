package gis_project;

public class DataPoint {

    private int id;
    private int[] time;
    private double x;
    private double y;
    private double t;
    private double measurement;
    
    public DataPoint(int id, int[] time, double x, double y, double measurement) {
        this.id = id;
        this.time = time;
        this.t = -1; // t is encoded time value; relies on year range and domain
                     // and therefore should only be set by DataSet.setAllT() !!!
                     // double precision is used for consistency with x and y
                     // (easier for 3D calcs if each dimension is same data type)
                     // and for the ability to test different encoding schemes
        this.x = x;
        this.y = y;
        this.measurement = measurement;
    }
    
    public double getAxis(int axis) {
        if (axis == 0) {
            return getX();
        } else if (axis == 1) {
            return getY();
        } else if (axis == 2) {
            return getT();
        } else {
            return -1;
        }
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int[] getTime() {
        return time;
    }
    
    public void setTime(int[] time) {
        this.time = time;
    }
    
    // return will be useless if called before calling DataSet.setAllT()
    public double getT() {
        return t;
    }
    
    // should only be called by DataSet.setAllT() or DataSet.scaleT()
    public void setT(double t) {
        this.t = t;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getMeasurement() {
        return measurement;
    }
    
    public void setMeasurement(double measurement) {
        this.measurement = measurement;
    }
}