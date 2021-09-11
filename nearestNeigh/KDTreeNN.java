package nearestNeigh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is required to be implemented.  Kd-tree implementation.
 */
public class KDTreeNN implements NearestNeigh {

    private Node root;
    private List<Point> data;
    private int D;
    private int N;
    private Point[] points;
    private List<Point>[] points_sorted;

    public KDTreeNN() {
    }

    @Override
    public void buildIndex(List<Point> points) {
        if (points.size() > 0) {
            D = 2;
            this.data = points;
            this.points = points.stream().parallel().toArray(Point[]::new);
            List<Point> sorted_lat = points.stream().
                    parallel().
                    sorted((s1, s2) -> Double.compare(s1.lat, s2.lat)).collect(Collectors.toList());
            List<Point> sorted_lon = points.stream().
                    parallel().
                    sorted((s1, s2) -> Double.compare(s1.lon, s2.lon)).collect(Collectors.toList());
            this.points_sorted = new List[]{sorted_lat, sorted_lon};
            root = new Node();
            buildKdTree(root, points_sorted, 0);
        }
    }

    public List<Point>[][] split(List<Point>[] sortedPoints, int cd, Point p) {
        List<Point> sorted_cd = sortedPoints[cd];
        List<Point> left = new ArrayList<>();
        List<Point> right = new ArrayList<>();
        for (Point point : sorted_cd) {
            // median p not included
            if (point != p) {
                if (point.data[cd] < p.data[cd])
                    left.add(point);
                else
                    right.add(point);
            }
        }

        return new List[][]{
                {
                        sortedPoints[0].stream().filter(x -> left.contains(x)).collect(Collectors.toList()),
                        sortedPoints[1].stream().filter(x -> left.contains(x)).collect(Collectors.toList())
                },
                {
                        sortedPoints[0].stream().filter(x -> right.contains(x)).collect(Collectors.toList()),
                        sortedPoints[1].stream().filter(x -> right.contains(x)).collect(Collectors.toList())
                }
        };
    }

    private Node buildKdTree(Node node, List<Point>[] points_sorted, int cd) {
        N = points_sorted[cd].size();
        node.D = D;
        node.cd = cd;
        // get median point by cd
        node.point = points_sorted[cd].get(Math.floorDiv(N, 2));
        node.pointArray = node.point.toArray();
        // update cd
        int next_cd = (cd + 1) % D;

        List<Point>[][] splited = split(points_sorted, cd, node.point);
        node.child[0] = splited[0][0].size() > 0 ? buildKdTree(new Node(), splited[0], next_cd) : null;
        node.child[1] = splited[1][0].size() > 0 ? buildKdTree(new Node(), splited[1], next_cd) : null;
        return node;
    }

    @Override
    public boolean addPoint(Point point) {

        try {
            this.root = this.root.add(root, point, 0);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    @Override
    public List<Point> search(Point searchTerm, int k) {
        // To be implemented.
        return new ArrayList<Point>();
    }

    public static class Node {
        Point point;
        // the cutting dimension for node. 0 stands for x, 1 stands for y.
        int cd;
        // node array. Node[0] is lower node, Node[1] is upper node.
        Node[] child;
        // double array to store point data [lat, lon]
        int D;
        double[] pointArray;
        // total dimension. for this program is len(point)=2.


        public Node(Point point, int cuttingDimension) {
            this.child = new Node[2];
            this.D = this.pointArray.length;
            this.cd = cuttingDimension;
            this.point = point;
            this.pointArray = point.toArray();
        }

        public Node() {
            this.child = new Node[2];
        }

        public boolean isLeaf() {
            return child[0] == null && child[1] == null;
        }

        // get the closer node to the query point depending on current node's cutting dimension.
        public int getCloserChild(Point point) {
            if (point.data[this.cd] >= this.pointArray[this.cd])
                // got upper node
                return 1;
            else
                // got low node
                return 0;
        }

        /**
         * insert a point p at node t
         *
         * @param t  Node to insert
         * @param p  Point to insert
         * @param cd the current cutting dimension. 0 for lat, 1 for lon
         * @return Node after insert
         */
        public Node add(Node t, Point p, int cd) {
            // a leaf node
            if (t == null)
                t = new Node(p, cd);
            else if (p == t.point)
                throw new IllegalArgumentException("duplicate point");
            else
                t.child[t.getCloserChild(p)] =
                        add(t.child[t.getCloserChild(p)], p, (cd + 1) % D);
            return t;
        }

        @Override
        public String toString() {
            return "Node{" +
                    ", cd=" + cd +
                    ", pointArray=" + Arrays.toString(pointArray) +
                    ", D=" + D +
                    ", point=" + point +
                    "child=" + Arrays.toString(child) +
                    '}';
        }
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
