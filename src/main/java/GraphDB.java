import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class GraphDB {


    public Graph graph = new Graph();
    public TST<Vertex> tst = new TST<>();

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    static String normalizeString(String s) {
        // Should match all strings that are not alphabetical
        String regex = "[^a-zA-Z]";
        return s.replaceAll(regex, "").toLowerCase();
    }

    private void clean() {
        // Remove the vertices with no incoming and outgoing connections from your graph
        graph.clean();
    }

    public double distance(Vertex v1, Vertex v2) {
        // Return the euclidean distance between two vertices
        Double latDif = Math.pow(v2.getLat() - v1.getLat(), 2);
        Double lngDif = Math.pow(v2.getLng() - v1.getLng(), 2);
        return Math.sqrt(latDif + lngDif);
    }

    public long closest(double lon, double lat) {
        // Returns the closest vertex to the given latitude and longitude values
        double minDistance = Double.MAX_VALUE;
        long vertexID = -1;
        Vertex baseCoordinates = new Vertex(lat, lon, 0L);
        for(Vertex vertex: graph.getAdjacencyList().keySet()){
            double currentDistance = distance(vertex, baseCoordinates);
            if( currentDistance < minDistance){
                minDistance = currentDistance;
                vertexID = vertex.getId();
            }
        }
        return vertexID;
    }

    double lon(long v) {
        // Returns the longitude of the given vertex, v is the vertex id
        return graph.get(v).getLng();
    }


    double lat(long v) {
        // Returns the latitude of the given vertex, v is the vertex id
        return graph.get(v).getLat();
    }
}
