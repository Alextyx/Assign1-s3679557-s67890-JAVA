package nearestNeigh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class is required to be implemented.  Naive approach implementation.
 */
public class NaiveNN implements NearestNeigh {
    private List<Point> data;

    public NaiveNN() {
    }


    @Override
    public void buildIndex(List<Point> points) {
        this.data = points;

    }

    @Override
    public List<Point> search(Point q, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("Invalid k: " + k);
        }
        if (k > data.size()) {
            throw new IllegalArgumentException("k length is larger than the data size");
        }
        double[] dist = data.stream().parallel().mapToDouble(x -> q.distTo(x)).toArray();

        // use PriorityQueue as MaxHeap to get Top k min point.
        PriorityQueue<Point> maxHeap = new PriorityQueue<>(k, (p1, p2) -> Double.compare(p2.dist, p1.dist));
        for (int i = 0; i < k; i++) {
            Point point = data.get(i);
            point.dist = dist[i];
            maxHeap.add(point);
        }

        for (int i = k; i < dist.length; i++) {
            if (dist[i] < maxHeap.peek().dist) {
                maxHeap.poll();
                Point point = data.get(i);
                point.dist = dist[i];
                maxHeap.add(point);
            }
        }
        return new ArrayList<Point>(maxHeap);
    }

    @Override
    public boolean addPoint(Point point) {

        return false;
    }

    @Override
    public boolean deletePoint(Point point) {
        // To be implemented.
        return false;
    }

    @Override
    public boolean isPointIn(Point point) {
        // To be implemented.
        return false;
    }

}
