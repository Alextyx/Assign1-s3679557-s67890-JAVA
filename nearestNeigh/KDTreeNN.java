package nearestNeigh;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is required to be implemented.  Kd-tree implementation.
 */
public class KDTreeNN implements NearestNeigh {

    private Node root;
    private List<Point> data;
    private int D;
    private int N;
    private List<Point>[] points_sorted;
    private int K;
    private Set<Point> set = new HashSet<>();

    public KDTreeNN() {
    }

    @Override
    public void buildIndex(List<Point> points) {
        if (points.size() > 0) {
            D = 2;
            this.data = points;
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

    private List<Point>[][] split(List<Point>[] sortedPoints, int cd, Point p) {
        List<Point> sorted_cd = sortedPoints[cd];
        Set<Point> left = new HashSet<>();
        Set<Point> right = new HashSet<>();
        // split by p to left and right sets. p not included
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
                {       // sorted list which only in left set, in 0 and 1 cutting dimension
                        sortedPoints[0].stream().filter(x -> left.contains(x)).collect(Collectors.toList()),
                        sortedPoints[1].stream().filter(x -> left.contains(x)).collect(Collectors.toList())
                },
                {       // sorted list which only in right set, in 0 and 1 cutting dimension
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
        // add point to node.
        set.add(node.point);

        List<Point>[][] splited = split(points_sorted, cd, node.point);
        node.child[0] = splited[0][0].size() > 0 ? buildKdTree(new Node(), splited[0], next_cd) : null;
        node.child[1] = splited[1][0].size() > 0 ? buildKdTree(new Node(), splited[1], next_cd) : null;
        return node;
    }

    private void search(Node node, Point point, PriorityQueue<Point> heap) {
        if (node == null)
            return;
        {
            double distance = node.point.distTo(point);
            node.point.setDist(point);
            if (node.isLeaf()) {
                if (node.point.cat == point.cat) {
                    if (heap.size() < K)
                        heap.add(node.point);
                    else {
                        Point top = heap.peek();
                        if (top != null && distance < top.dist) {
                            heap.poll();
                            heap.add(node.point);
                        }
                    }
                }
            } else {
                Node near = node.child[node.getCloserChild(point)];
                search(near, point, heap);
                // look in other half
                Point cur_best = heap.peek();
                if (cur_best == null || (cur_best != null && cur_best.dist > node.getDistToSplitLine(point))) {
                    search(node.child[node.getFurtherChild(point)], point, heap);
                }
                if (node.point.cat == point.cat) {
                    // after searching all subtree, if still heap not full, add current node.
                    if (heap.size() < K)
                        heap.add(node.point);
                    else {
                        // check if this node's dist should be added to heap
                        if (heap.peek() != null && distance < heap.peek().dist) {
                            heap.poll();
                            heap.add(node.point);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean addPoint(Point point) {

        if (this.isPointIn(point))
            return false;
        try {
            this.root = this.root.add(root, point, 0);
            this.set.add(point);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }

    }

    @Override
    public List<Point> search(Point q, int k) {

        if (k <= 0) {
            throw new IllegalArgumentException("Invalid k: " + k);
        }
        if (k > data.size()) {
            throw new IllegalArgumentException("k length is larger than the data size");
        }
        K = k;
        // use PriorityQueue as MaxHeap to get Top k min point.
        PriorityQueue<Point> maxHeap = new PriorityQueue<>(k, (p1, p2) -> Double.compare(p2.dist, p1.dist));

        search(root, q, maxHeap);

        List<Point> result = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            result.add(0, maxHeap.poll());
        }

        return result;

    }


    @Override
    public boolean deletePoint(Point point) {
        if (!isPointIn(point))
            return false;
        try {
            this.root = delete(point, root, 0);
            this.set.remove(point);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }

    }

    @Override
    public boolean isPointIn(Point point) {
        return this.set.contains(point);
    }


    public Node delete(Point x, Node t, int cd) {
        if (t == null)
            throw new IllegalArgumentException("node is not found");
        int next_cd = (cd + 1) % D;
        if (t.point.equals(x)) {
            // right tree not null, use findMin and swap the node.
            if (t.child[1] != null) {
                t.point = findMin(t.child[1], cd, next_cd);
                t.child[1] = delete(t.point, t.child[1], next_cd);
            }// left tree not null, use min(cd) from new right
            else if (t.child[0] != null) {
                t.point = findMin(t.child[0], cd, next_cd);
                t.child[1] = delete(t.point, t.child[0], next_cd);
            } else {
                // leaf node, remove.
                t = null;
            }
        } else {
            t.child[t.getCloserChild(x)] =
                    delete(x, t.child[t.getCloserChild(x)], next_cd);
        }
        return t;
    }


    /**
     * find the minimum value point in given dimension
     *
     * @param T   node
     * @param dim given dimension
     * @param cd  cutting dimension
     * @return
     */
    public Point findMin(Node T, int dim, int cd) {
        // empty tree
        if (T == null)
            return null;
        if (cd == dim)
            if (T.child[0] == null)
                return T.point;
            else
                return findMin(T.child[0], dim, (cd + 1) % D);
        else {
            Point l = findMin(T.child[0], dim, (cd + 1) % D);
            Point r = findMin(T.child[1], dim, (cd + 1) % D);
            Point result = Node.minPoint(T.point, l, r, dim);
            return result;
        }

    }

    public static class Node {
        Point point;
        // the cutting dimension for node. 0 stands for x, 1 stands for y.
        int cd;
        // node array. Node[0] is lower node, Node[1] is upper node.
        Node[] child;
        // total dimension. for this program is len(point)=2.
        int D;
        // double array to store point data [lat, lon]
        double[] pointArray;


        public Node(Point point, int cuttingDimension) {
            this.child = new Node[2];
            this.D = 2;
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

        // get the further node to the query point depending on current node's cutting dimension.
        public int getFurtherChild(Point point) {
            if (point.data[this.cd] >= this.pointArray[this.cd])
                // got low node
                return 0;
            else
                // got upper node
                return 1;
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
            else if (p.equals(t.point))
                throw new IllegalArgumentException("duplicate point");
            else
                t.child[t.getCloserChild(p)] =
                        add(t.child[t.getCloserChild(p)], p, (cd + 1) % D);
            return t;
        }

        // A utility function to find minimum of three node
        public static Point minPoint(Point root, Point left, Point right, int d) {
            Point res = root;
            if (left != null && left.data[d] < res.data[d])
                res = left;
            if (right != null && right.data[d] < res.data[d])
                res = right;
            return res;
        }

        /**
         * get the distance from point to splitting line from this node's point
         *
         * @param p
         * @return
         */
        public double getDistToSplitLine(Point p) {
            Point splitLine = new Point();
            int cd = this.cd;
            if (cd == 0) {
                splitLine.lat = this.point.lat;
                splitLine.lon = p.lon;
            } else {
                splitLine.lat = p.lat;
                splitLine.lon = this.point.lon;
            }
            return p.distTo(splitLine);
        }

        @Override
        public String toString() {
            return "Node{" +
                    "cd=" + cd +
                    ", pointArray=" + Arrays.toString(pointArray) +
                    ", D=" + D +
                    ", point=" + point +
                    ", child=" + Arrays.toString(child) +
                    '}';
        }
    }

}
