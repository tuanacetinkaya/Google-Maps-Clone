import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    // outgoing edges are stored here
    private HashMap<Vertex, ArrayList<Edge>> adjacencyList;

    // to simplify the clear process I also track the incoming edges in a map
    private HashMap<Long, ArrayList<Edge>> incomingEdges;

    // in order to access the vertexes more easily, we track their ids in another map
    private HashMap<Long, Vertex> vertexHashMap;


    public Graph() {
        adjacencyList = new HashMap<>();
        incomingEdges = new HashMap<>();
        vertexHashMap = new HashMap<>();
    }

    public void addVertex(Vertex newVertex){
        adjacencyList.put(newVertex, new ArrayList<>());
        incomingEdges.put(newVertex.getId(), new ArrayList<>());
        vertexHashMap.put(newVertex.getId(), newVertex);
    }

    //Todo: delete below if not used
//    public void addEdge(Vertex source, Vertex destination){
//        Edge edge = new Edge();
//    }
    private void addToGraph(Edge edge){
        // in case we cleared the graph before and deleted the source node, we check if the source is present
        if(adjacencyList.containsKey(edge.getSource())){
            adjacencyList.get(edge.getSource()).add(edge);
        }else {
            ArrayList<Edge> edges = new ArrayList<>();
            edges.add(edge);
            adjacencyList.put(edge.getSource(), edges);
            vertexHashMap.put(edge.getSource().getId(), edge.getSource());
        }

        // in case we cleared the graph before and deleted the destination node, we check if the destination is present
        if(incomingEdges.containsKey(edge.getDestination().getId())){
            incomingEdges.get(edge.getDestination().getId()).add(edge);
        }else {
            ArrayList<Edge> edges = new ArrayList<>();
            edges.add(edge);
            incomingEdges.put(edge.getDestination().getId(), edges);
            vertexHashMap.put(edge.getDestination().getId(), edge.getDestination());
        }
    }

    public void addEdge(Edge edge, boolean isOneWay){
        addToGraph(edge);
        if(!isOneWay){
//          Create a counter edge that goes from destination to source when the connection is not one way
            Edge counterEdge = new Edge();
            counterEdge.setName(edge.getName());
            counterEdge.setDestination(edge.getSource());
            counterEdge.setSource(edge.getDestination());
            counterEdge.setSpeed(edge.getSpeed());
            counterEdge.setWeight(edge.getWeight());
            addToGraph(counterEdge);
        }

    }

    public boolean hasVertex(Vertex vertex){return adjacencyList.containsKey(vertex);}

    public boolean hasEdge(Vertex source, Vertex destination){return adjacencyList.get(source).contains(destination);}

    public boolean hasEdge(Edge edge){return adjacencyList.get(edge.getSource()).contains(edge);}

    public void clean(){
        // traverse all vertices to clear the ones with no connections from both maps
        if(adjacencyList.isEmpty()) return;

        ArrayList<Vertex> blackList = new ArrayList<>();
        for (Vertex source: adjacencyList.keySet()) {
            // if source vertex has no outgoing edges and no incoming edges, delete the vertex
            if(adjacencyList.get(source).isEmpty() && incomingEdges.get(source.getId()).isEmpty()){
                blackList.add(source); // added to blacklist in order to remove later
                incomingEdges.remove(source.getId());
                vertexHashMap.remove(source.getId());
            }
        }
        // to avoid ConcurrentModificationException
        for(Vertex removed: blackList){
            adjacencyList.remove(removed);
        }

    }

    public Vertex get(Long id){
        return vertexHashMap.get(id);
    }

    public HashMap<Long, Vertex> getVertexHashMap() {
        return vertexHashMap;
    }

    public HashMap<Vertex, ArrayList<Edge>> getAdjacencyList() {
        return adjacencyList;
    }
}
