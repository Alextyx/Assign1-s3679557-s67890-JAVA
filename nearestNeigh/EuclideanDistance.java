package nearestNeigh;

public class EuclideanDistance {

    public EuclideanDistance() {
    }

    public static double distance(double x[], double[] y) {
        if (x.length != y.length)
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));

        int n = x.length;
        int m = 0;

        double dist = 0.0;

        for (int i = 0; i < n; i++) {
            if (!Double.isNaN(x[i]) && !Double.isNaN(y[i])) {
                m++;
                double d = x[i] - y[i];
                dist += d * d;
            }
        }

        if (m == 0)
            dist = Double.NaN;
        else
            dist = n * dist / m;

        return Math.sqrt(dist);
    }
}
