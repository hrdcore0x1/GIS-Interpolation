package gis_project;

public class IDW {

    // calculates value for test point based on nearest neighors
    public static double calc(DataPoint tp, Neighbor[] nn, double p) {
        int n = nn.length;
        double sumWeights = 0;
        for (int i = 0; i < n; i++) {
            sumWeights += neighborWeight(nn[i], p);
        }
        double interpVal = 0;
        for (int i = 0; i < n; i++) {
            interpVal += ((nn[i].getWeight() * nn[i].getDp().getMeasurement()) / sumWeights);
        }
        return interpVal;
    }
    
    public static double neighborWeight(Neighbor x, double p) {
        double distSq = x.getDistSq();
        double dist = Math.sqrt(distSq);
        double weight = 1 / Math.pow(dist, p);
        x.setWeight(weight);
        return weight;
    }
}