import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;


public class QuadTree {
    public QTreeNode root;
    private String imageRoot;

    public QuadTree(String imageRoot) {
        // Instantiate the root element of the tree with depth 0
        // Use the ROOT_ULLAT, ROOT_ULLON, ROOT_LRLAT, ROOT_LRLON static variables of MapServer class
        root = new QTreeNode("root", MapServer.ROOT_ULLAT, MapServer.ROOT_ULLON, MapServer.ROOT_LRLAT, MapServer.ROOT_LRLON, 0);
        // Call the build method with depth 1
        build(root, 1);
        // Save the imageRoot value to the instance variable
        this.imageRoot = imageRoot;

    }

    public void build(QTreeNode subTreeRoot, int depth) {
        // Recursive method to build the tree as instructed
        String buildName = subTreeRoot.getName();
        if(subTreeRoot.getName().equals("root")){
            buildName = "" ;
        }
        // until we find a better base case, we check the depth to make sure we don't go too far
        if(depth >= 9){
            return;
        }
        double midLongitude = (subTreeRoot.getLowerRightLongtitude() + subTreeRoot.getUpperLeftLongtitude()) / 2;
        double midLatitude = (subTreeRoot.getUpperLeftLatitude() + subTreeRoot.getLowerRightLatitude()) / 2;

        subTreeRoot.NW = new QTreeNode(buildName + "1",
                subTreeRoot.getUpperLeftLatitude(),
                subTreeRoot.getUpperLeftLongtitude(),
                midLatitude ,
                midLongitude,
                depth);
        build(subTreeRoot.NW, depth + 1);

        subTreeRoot.NE = new QTreeNode(buildName + "2",
                subTreeRoot.getUpperLeftLatitude(),
                 midLongitude,
                midLatitude ,
                subTreeRoot.getLowerRightLongtitude(),
                depth);
        build(subTreeRoot.NE, depth + 1);

        subTreeRoot.SW = new QTreeNode(buildName + "3",
                midLatitude,
                subTreeRoot.getUpperLeftLongtitude(),
                subTreeRoot.getLowerRightLatitude(),
                midLongitude,
                depth);
        build(subTreeRoot.SW, depth + 1);

        subTreeRoot.SE = new QTreeNode(buildName + "4",
                midLatitude,
                midLongitude,
                subTreeRoot.getLowerRightLatitude(),
                subTreeRoot.getLowerRightLongtitude(),
                depth);
        build(subTreeRoot.SE, depth + 1);

    }

    public Map<String, Object> search(Map<String, Double> params) {
        /*
         * Parameters are:
         * "ullat": Upper left latitude of the query box
         * "ullon": Upper left longitude of the query box
         * "lrlat": Lower right latitude of the query box
         * "lrlon": Lower right longitude of the query box
         * "w"    : Width of the screen
         * "h"    : height of the screen
         * */

        // Instantiate a QTreeNode to represent the query box defined by the parameters
        QTreeNode queryBox = new QTreeNode("queryBox", params.get("ullat"), params.get("ullon"), params.get("lrlat"), params.get("lrlon"), 0);
        // Calculate the lonDpp value of the query box
        double lonDppQueryBox = (queryBox.getLowerRightLongtitude() - queryBox.getUpperLeftLongtitude()) / params.get("w");
        // Call the search() method with the query box and the lonDpp value
        ArrayList<QTreeNode> validNodes = new ArrayList<>();
        search(queryBox, root, lonDppQueryBox, validNodes);
        // Call and return the result of the getMap() method to return the acquired nodes in an appropriate way
        return getMap(validNodes);
    }

    private Map<String, Object> getMap(ArrayList<QTreeNode> list) {
        Map<String, Object> map = new HashMap<>();

        // Check if the root intersects with the given query box
        if (list.isEmpty()){
            map.put("query_success", false);
            return map;
        }

        // Use the get2D() method to get organized images in a 2D array
        String[][] grid = get2D(list);
        map.put("render_grid", grid);


//        For Debug Purposes!!
//        System.out.println("Below Code Prints the Order of Tiles in the Grid from Upper Left to Lower Right");
//        for(int i = 0; i< grid.length; i++){
//            for(int j = 0; j< grid[0].length; j++){
//                System.out.println(grid[i][j].split("/")[1].split("\\.")[0]);
//            }
//        }

        //! Remember the list gets sorted in a way that all rows are added end to end
        // Upper left latitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_ul_lat", list.get(0).getUpperLeftLatitude());

        // Upper left longitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_ul_lon", list.get(0).getUpperLeftLongtitude());

        // Upper lower right latitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_lr_lat", list.get(list.size()-1).getLowerRightLatitude());

        // Upper lower right longitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_lr_lon", list.get(list.size()-1).getLowerRightLongtitude());

        // Depth of the grid (can be thought as the depth of a single image)
        map.put("depth", list.get(0).getDepth());

        map.put("query_success", true);
        return map;
    }

    private String[][] get2D(ArrayList<QTreeNode> list) {
        // After you retrieve the list of images using the recursive search mechanism described above, you
        // should order them as a grid. This grid is nothing more than a 2D array of file names. To order
        // the images, you should determine correct row and column for each image (node) in the retrieved
        // list. As a hint, you should consider the latitude values of images to place them in the row, and
        // the file names of the images to place them in a column.

        ArrayList<Double> latitudes = new ArrayList<>();
        ArrayList<Double> longitudes = new ArrayList<>();
        for(QTreeNode node: list){
            if(!latitudes.contains(node.getUpperLeftLatitude())){
                latitudes.add(node.getUpperLeftLatitude());
            }
            if(!longitudes.contains(node.getUpperLeftLongtitude())){
                longitudes.add(node.getUpperLeftLongtitude());
            }
        }

        String[][] images = new String[latitudes.size()][longitudes.size()];

//      sort the array in the order that puts the rows side by side respectfully
        list.sort(Comparator.comparing(QTreeNode::getUpperLeftLatitude, Collections.reverseOrder()).thenComparing(QTreeNode::getUpperLeftLongtitude));

        int count = 0;
        for(QTreeNode node: list){
            // longitudes size gives the number of grids in the x axis
            // so dividing by that size gives us the y coordinate and the remainder gives the x coordinate
            images[count / longitudes.size()][count % longitudes.size()] = "img/" + node.getName() + ".png";
            count++;
        }

        return images;
    }

    public void search(QTreeNode queryBox, QTreeNode tile, double lonDpp, ArrayList<QTreeNode> list) {
        // The first part includes a recursive search in the tree. This process should consider both the
        // lonDPP property (discussed above) and if the images in the tree intersect with the query box.
        // (To check the intersection of two tiles, you should use the checkIntersection() method)
        // To achieve this, you should retrieve the first depth (zoom level) of the images which intersect
        // with the query box and have a lower lonDPP than the query box. (cant go further add to list)
        // else search its children
        // This method should fill the list given by the "ArrayList<QTreeNode> list" parameter

        // if any of tile's children is null that means we've seen the end
        if(checkIntersection(tile, queryBox)){
            if (tile.NW == null) {
                list.add(tile); // we should add the tile to list since we won't be moving further
                return; // we zoomed as much as we can and we're out of children to replace
            }
            if (lonDpp < tile.getLonDPP()) {
                search(queryBox, tile.NW, lonDpp, list);
                search(queryBox, tile.NE, lonDpp, list);
                search(queryBox, tile.SW, lonDpp, list);
                search(queryBox, tile.SE, lonDpp, list);
            }else{
                list.add(tile);
            }
        }
    }

    public boolean checkIntersection(QTreeNode tile, QTreeNode queryBox) {
        // Return true if two tiles are intersecting with each other
        // Eliminate the outer cases for simplicity
        return !(tile.getUpperLeftLatitude() < queryBox.getLowerRightLatitude()) && // not below the query box
                !(tile.getLowerRightLatitude() > queryBox.getUpperLeftLatitude()) && //not above the query box
                !(tile.getUpperLeftLongtitude() > queryBox.getLowerRightLongtitude()) && //not outside right
                !(tile.getLowerRightLongtitude() < queryBox.getUpperLeftLongtitude()); // not outside left
    }

}