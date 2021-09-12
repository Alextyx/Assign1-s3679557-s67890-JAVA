package nearestNeigh;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is required to be implemented.  Naive approach implementation.
 */
public class NaiveNN implements NearestNeigh {
    private List<Point> pointList;

    public NaiveNN() {
    }


    @Override
    public void buildIndex(List<Point> points) {
        this.pointList = points;
    }

    @Override
    public List<Point> search(Point q, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("Invalid k: " + k);
        }
        if (k > pointList.size()) {
            throw new IllegalArgumentException("k length is larger than the data size");
        }

        // calculate each point x's distance to q and set it. sort according to dist. and return the sorted List<Point>
        List<Point> sorted = pointList.stream()
                .parallel()
                .map(x -> x.setDist(q)).sorted().collect(Collectors.toList());
        List<Point> result = new ArrayList<>();

        // select k nearest neighbour points in q's cat. if not enough neighbour, return what's included. e.g k=10, 6 in cat. return 6.
        for (int i = 0; i < sorted.size(); i++) {
            Point p = sorted.get(i);
            if (p.cat == q.cat) {
                result.add(p);
                k--;
            }
            if (k == 0)
                break;
        }

        return result;
    }

    @Override
    public boolean addPoint(Point point) {
        if (this.pointList.contains(point))
            return false;
        return this.pointList.add(point);
    }

    @Override
    public boolean deletePoint(Point point) {
        return this.pointList.remove(point);
    }

    @Override
    public boolean isPointIn(Point point) {
        return this.pointList.contains(point);
    }

}
