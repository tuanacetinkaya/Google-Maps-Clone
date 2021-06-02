import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;


public class GraphBuildingHandler extends DefaultHandler {


    public static class Way {
        private String id;
        private String name;
        private String speed;
        private boolean isOneWay;

        private final ArrayList<Long> listOfNodes = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public ArrayList<Long> getListOfNodes() {
            return listOfNodes;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setOneWay(boolean oneway) {
            this.isOneWay = oneway;
        }

        public boolean isOneWay() {
            return this.isOneWay;
        }
    }


    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));

    private String activeState = "";
    private final GraphDB g;
    private Way currentWay = new Way();
    private boolean flag = false;
    private Vertex currentVertex = null;

    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("node")) {
            activeState = "node";

            // For the case of new nodes, you should add a new vertex to your graph and save other necessary information
            // to be used depending on your implementation (i.e. saving the last saved vertex).

            Vertex vertex = new Vertex(Double.parseDouble(attributes.getValue("lat")),
                    Double.parseDouble(attributes.getValue("lon")),
                    Long.parseLong(attributes.getValue("id")));

            g.graph.addVertex(vertex);
            currentVertex = vertex;

        } else if (qName.equals("way")) {

            activeState = "way";

            // The first one is encountering a new "way" start tag. In this scenario, you should instantiate
            // a new Way, while saving its id.
            currentWay = new Way();
            currentWay.setId(attributes.getValue("id"));
            flag = false;

        } else if (activeState.equals("way") && qName.equals("nd")) {

            // The second scenario is encountering a new "nd" tag while the active state is "way". This
            // tag references a node in the current way by its node id. When this tag is encountered, you
            // should add the id to the listOfNodes in that Way.
            currentWay.listOfNodes.add(Long.parseLong(attributes.getValue("ref")));

        } else if (activeState.equals("way") && qName.equals("tag")) {

            String k = attributes.getValue("k");
            String v = attributes.getValue("v");

            if (k.equals("maxspeed")) {

                // Set the speed for the current way
                currentWay.setSpeed(v.split(" ")[0]);
            } else if (k.equals("highway")) {

                // At this point, you should check if the parsed highway type is specified to be allowed in the
                // list ALLOWED_HIGHWAY_TYPES. If the type is allowed, you should set a flag as an
                // instance variable to specify that this way should be used to connect vertices in the graph.
                if (ALLOWED_HIGHWAY_TYPES.contains(v)){
                    flag=true;
                }else {
                    flag=false;
                }
            } else if (k.equals("name")) {
                // Set the name for the current way
                currentWay.setName(v);
            } else if (k.equals("oneway")) {
                // Set the oneway property for the current way
                if(v.equals("yes")){
                    currentWay.setOneWay(true);
                }else {
                    currentWay.setOneWay(false);
                }

            }

        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            // For the case of encountering a "tag" tag with the attribute "name" while the active state is
            // "node", you should set the name of the current vertex, as well as insert the location name to
            // your ternary search tree. Do not forget to normalize the text by supplying the regular expression
            // given in the coding template (GraphDB.normalizeString())
            // Insert the normalized text as a key along with a list which contains the Vertex if the key is not in GraphDB.locations
            // If the key is in GraphDB.locations, retrieve the list and add the Vertex to the list
            currentVertex.setName(attributes.getValue("v"));
            String normalized = GraphDB.normalizeString(attributes.getValue("v"));
            if(normalized.isEmpty()) return;
            this.g.tst.put( normalized, currentVertex);



        }
    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            // In endElement callback, you should handle ending of a way tag. If the way is marked as a valid
            // one in the third scenario described above, you should connect the vertices specified by the list
            // of references in the listOfNodes. In this step, you should be careful if the way is one-directional
            // or not. If the way you were parsing is not one-directional, you should add two edges to your
            // directed graph (in both directions).
            if(!flag || currentWay.listOfNodes.isEmpty())return; // flag has to be set in order to add the edges
            Vertex previousNode = g.graph.get(currentWay.listOfNodes.get(0));
            for(int i = 1; i < currentWay.listOfNodes.size(); i++){
                Vertex nextNode = g.graph.get(currentWay.listOfNodes.get(i));
                Edge edge = new Edge();
                edge.setSpeed(currentWay.speed);
                edge.setSource(previousNode);
                edge.setDestination(nextNode);
                edge.setWeight(g.distance(edge.getSource(), edge.getDestination()));

                g.graph.addEdge(edge, currentWay.isOneWay);
                previousNode = nextNode;
            }
            flag = false;

        }
    }
}