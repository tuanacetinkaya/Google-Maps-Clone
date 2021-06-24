import java.util.*;
import java.util.jar.JarEntry;


public class Router {

    private static List<Vertex> stops = new ArrayList<>();
    private static Vertex start, end;


    //settled and unsettled nodes are held here
    private static HashSet<Long> settledNodes = new HashSet<>();
//    private static ArrayList<Long> unsettledNodes = new ArrayList<>();

    // hashmap holds vertex ids and their distances
    private static HashMap<Long, Double> distanceMap = new HashMap<>();
    // hashmap that holds the previous node that leads to that vertex with the distance in distanceMap
    private static HashMap<Long, Long> previousNode = new HashMap<>();

//    // also another hashmap to hold the visited nodes parayla degil ya map yapmak
//    private static HashMap<Long, Boolean> visitedNodes = new HashMap<>();

    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {

        //Comparator for the comparison of vertexes depending on their distances to start vertex
        Comparator<Vertex> distanceSorter = new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
//                if(distanceMap.get(o1.getId()) == Double.MAX_VALUE || distanceMap.get(o1.getId()) == Double.MAX_VALUE)
//                    return (int) Math.min( distanceMap.get(o1.getId()), distanceMap.get(o2.getId()));
                return Double.compare(distanceMap.get(o1.getId()), distanceMap.get(o2.getId()));
//                return (int) (g.distance(start, o1) - g.distance(start, o2));
            }
        };

        // priority queue to decide the closest node
        PriorityQueue<Vertex> queue = new PriorityQueue<>(distanceSorter);
        // Return the shortest path between start and end points
        // Use g.closest() to get start and end vertices
        // Return ids of vertices as a linked list
        LinkedList<Long> pathList = new LinkedList<>();

        Vertex v1 = g.graph.get(g.closest(stlon, stlat));
        Vertex v2 = g.graph.get(g.closest(destlon, destlat));
        // if there are no start and end means we're not adding a new stop
        if (start == null && end == null) {
            // set the start vertex
            start = v1;
            // set the end vertex
            end = v2;
        }

        // set the distances of all nodes to "infinity"
        for (Long id : g.graph.getVertexHashMap().keySet()) {
            distanceMap.put(id, Double.POSITIVE_INFINITY);
        }
        //override the start vertex to have a distance of zero
        distanceMap.put(v1.getId(), 0D);
        queue.add(v1);

        while (!queue.isEmpty()) {
            // remove the closest node and save it's value
            Vertex closest = queue.remove();
            settledNodes.add(closest.getId());
            graphAdjacentNodes(closest, queue, g);
        }

        // now track backwards from end vertex to find the shortest path
        Vertex track = v2;
        pathList.add(track.getId());

//        LinkedList<Long> reversedPaths = new LinkedList<>();
        while (track != v1) {
            pathList.addFirst(previousNode.get(track.getId()));
            track = g.graph.get(previousNode.get(track.getId()));
        }
        return pathList;
    }

    public static LinkedList<Long> addStop(GraphDB g, double lat, double lon) {
        // Find the closest vertex to the stop coordinates using g.closest()
        // Add the stop to the stop list
        // Recalculate your route when a stop is added and return the new route

        Vertex newStop = g.graph.get(g.closest(lon, lat));
        stops.add(newStop);

        LinkedList<Long> route = shortestPath(g, start.getLng(), start.getLat(), stops.get(0).getLng(), stops.get(0).getLat());
//        route.removeLast();
        for (int i = 1; i < stops.size(); i++) {
            LinkedList<Long> subWay = shortestPath(g, stops.get(i - 1).getLng(), stops.get(i - 1).getLat(), stops.get(i).getLng(), stops.get(i).getLat());
//            subWay.removeLast();
            route.addAll(subWay);
        }
//        if(stops.size() == 1){
//            route.removeLast();
//        }
        route.addAll(shortestPath(g, stops.get(stops.size() - 1).getLng(), stops.get(stops.size() - 1).getLat(), end.getLng(), end.getLat()));
        return route;
    }

    public static void clearRoute() {
        start = null;
        end = null;
        stops = new ArrayList<>();
        settledNodes = new HashSet<>();
        distanceMap = new HashMap<>();
        previousNode = new HashMap<>();
    }

    private static void graphAdjacentNodes(Vertex vertex, PriorityQueue<Vertex> queue, GraphDB g) {
        Double edgeDistance = -1D;
        Double newDistance = -1D;

        // for each neighbour of given vertex
        for (Edge edge : g.graph.getAdjacencyList().get(vertex)) {
            Vertex dest = edge.getDestination();

            edgeDistance = edge.getWeight();
            // source node's total distance + new path's weight = destination vertex distance
            newDistance = distanceMap.get(vertex.getId()) + edgeDistance;

            if (newDistance < distanceMap.get(dest.getId())) {
                distanceMap.put(dest.getId(), newDistance);
                previousNode.put(dest.getId(), vertex.getId()); // record where we came from
                queue.add(dest);
            }
        }
    }

}
