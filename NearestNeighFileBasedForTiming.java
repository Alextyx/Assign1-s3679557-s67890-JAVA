import nearestNeigh.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This is to be the main class when we run the program in file-based point.
 * It uses the data file to initialise the set of points.
 * It takes a command file as input and output into the output file.
 */
public class NearestNeighFileBasedForTiming {

    /**
     * Name of class, used in error messages.
     */
    protected static final String progName = "NearestNeighFileBased";

    /**
     * Print help/usage message.
     */
    public static void usage(String progName) {
        System.err.println(progName + ": <approach> [data fileName] [command fileName] [output fileName]");
        System.err.println("<approach> = <naive | kdtree>");
        System.exit(1);
    } // end of usage

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // read command line arguments
        if (args.length != 4) {
            System.err.println("Incorrect number of arguments.");
            usage(progName);
        }

        // initialise search agent
        NearestNeigh agent = null;
        switch (args[0]) {
            case "naive":
                agent = new NaiveNN();
                break;
            case "kdtree":
                agent = new KDTreeNN();
                break;
            default:
                System.err.println("Incorrect argument value.");
                usage(progName);
        }


        String testDir = "evaluation";
        String testEvaluationFileName = testDir + "/" + args[0] + "_" + args[2] + ".out";

        Path path = Paths.get("", testEvaluationFileName);
        path = path.toAbsolutePath();
        File file = path.toFile();
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pw = new PrintWriter(bw);
        long timeElapsed;


        Instant start = Instant.now();
        // read in data file of initial set of points
        String dataFileName = args[1];
        List<Point> points = new ArrayList<Point>();
        try {
            File dataFile = new File(dataFileName);
            Scanner scanner = new Scanner(dataFile);
            while (scanner.hasNext()) {
                String id = scanner.next();
                Category cat = Point.parseCat(scanner.next());
                Point point = new Point(id, cat, scanner.nextDouble(), scanner.nextDouble());
                points.add(point);
            }
            scanner.close();
            Instant buildStart = Instant.now();
            agent.buildIndex(points);
            Instant buildEnd = Instant.now();
            timeElapsed = Duration.between(buildStart, buildEnd).toMillis();
            pw.printf("Build finished in {%d} ms %n", timeElapsed);
        } catch (FileNotFoundException e) {
            System.err.println("Data file doesn't exist.");
            usage(progName);
        }


        String commandFileName = args[2];
        String outputFileName = args[3];
        File commandFile = new File(commandFileName);
        File outputFile = new File(outputFileName);

        // parse the commands in commandFile
        try {
            Scanner scanner = new Scanner(commandFile);
            PrintWriter writer = new PrintWriter(outputFile);

            // operating commands
            while (scanner.hasNext()) {
                String command = scanner.next();
                String id;
                Category cat;
                // remember lat = latitude (approximately correspond to x-coordinate)
                // remember lon = longitude (approximately correspond to y-coordinate)
                double lat;
                double lon;
                int k;
                Point point;
                switch (command) {
                    // search
                    case "S":
                        cat = Point.parseCat(scanner.next());
                        lat = scanner.nextDouble();
                        lon = scanner.nextDouble();
                        k = scanner.nextInt();
                        point = new Point("searchTerm", cat, lat, lon);
                        Instant searchStart = Instant.now();
                        List<Point> searchResult = agent.search(point, k);
                        Instant searchEnd = Instant.now();
                        timeElapsed = Duration.between(searchStart, searchEnd).toMillis();
                        pw.printf("Search finished in {%d} ms %n", timeElapsed);
                        for (Point writePoint : searchResult) {
                            writer.println(writePoint.toString());
                        }
                        break;
                    // add
                    case "A":
                        id = scanner.next();
                        cat = Point.parseCat(scanner.next());
                        lat = scanner.nextDouble();
                        lon = scanner.nextDouble();
                        Instant addStart = Instant.now();
                        point = new Point(id, cat, lat, lon);
                        if (!agent.addPoint(point)) {
                            writer.println("Add point failed.");
                        }
                        Instant addEnd = Instant.now();
                        timeElapsed = Duration.between(addStart, addEnd).toMillis();
                        pw.printf("Add finished in {%d} ms %n", timeElapsed);
                        break;
                    // delete
                    case "D":
                        id = scanner.next();
                        cat = Point.parseCat(scanner.next());
                        lat = scanner.nextDouble();
                        lon = scanner.nextDouble();
                        Instant deleteStart = Instant.now();
                        point = new Point(id, cat, lat, lon);
                        if (!agent.deletePoint(point)) {
                            writer.println("Delete point failed.");
                        }
                        Instant deleteEnd = Instant.now();
                        timeElapsed = Duration.between(deleteStart, deleteEnd).toMillis();
                        pw.printf("Delete finished in {%d} ms %n", timeElapsed);
                        break;
                    // check
                    case "C":
                        id = scanner.next();
                        cat = Point.parseCat(scanner.next());
                        lat = scanner.nextDouble();
                        lon = scanner.nextDouble();
                        point = new Point(id, cat, lat, lon);
                        writer.println(agent.isPointIn(point));
                        break;
                    default:
                        System.err.println("Unknown command.");
                        System.err.println(command + " " + scanner.nextLine());
                }
            }
            scanner.close();
            writer.close();

            Instant finish = Instant.now();
            timeElapsed = Duration.between(start, finish).toMillis();
            pw.printf("All finished in %d ms %n", timeElapsed);
            pw.close();
        } catch (FileNotFoundException e) {
            System.err.println("output file doesn't exist.");
            usage(progName);
        }
    }
}
