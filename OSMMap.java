import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.util.SupplierUtil;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;


public class OSMMap {

    static OSMNode start;
    static OSMNode end;

    public static Graph<OSMNode,
            DefaultWeightedEdge> graph = GraphTypeBuilder
            .directed().weighted(true).allowingMultipleEdges(true).allowingSelfLoops(true)
            .vertexSupplier(new CustomVertexSupplier())
            .edgeSupplier(SupplierUtil.createDefaultWeightedEdgeSupplier()).buildGraph();

    public static   GraphPath<OSMNode, DefaultWeightedEdge> route;
    public static GeoPosition mouseClick;

    public static void setEnd(OSMNode end) {
        OSMMap.end = end;
    }

    public static OSMNode getEnd() {
        return end;
    }

    public static OSMNode getStart() {
        return start;
    }

    public static void setStart(OSMNode start) {
        OSMMap.start = start;
    }

    private static GraphImporter<OSMNode, DefaultWeightedEdge> createImporter() {
        /*
         * Create the graph importer.
         */
        GraphMLImporter<OSMNode, DefaultWeightedEdge> importer = new GraphMLImporter<>();

        /*
         * Add vertex attribute consumer to read back vertex color from file.
         */
        importer.addVertexAttributeConsumer((p, attrValue) -> {
            OSMNode v = p.getFirst();
            String attrName = p.getSecond();
//            if (attrName.equals("id")) {
//                v.setId(attrValue.getValue());
//            }
            if (attrName.equals("lat")) {
                v.setLat(Double.parseDouble(attrValue.getValue()));
            }

            if (attrName.equals("lon")) {
                v.setLon(Double.parseDouble(attrValue.getValue()));
            }

        });
        return importer;
    }

    static class CustomVertexSupplier
            implements Supplier<OSMNode> {

        private int id = 0;

        @Override
        public OSMNode get() {
            return new OSMNode(
                    String.valueOf(id++));
        }
    }

    public static GeoPosition getClick(){
        return mouseClick;
    }

    public static OSMNode getNearestNode(Graph<OSMNode,DefaultWeightedEdge> graph , GeoPosition clicked){
        double clicked_lat = clicked.getLatitude();
        double clicked_lon = clicked.getLongitude();
        double min = 100;
        OSMNode ans = null;
        for(OSMNode n : graph.vertexSet()){
            double temp = Haversine.haversine(clicked_lat,clicked_lon,n.getLat(),n.getLon());
            if(temp < min){
                min = temp;
                ans = n;
            }
        }
        return ans;
    }
    public static void setupMapViewer(JXMapViewer mapViewer) {
        // Create a TileFactoryInfo for OpenStreetMap
//        TileFactoryInfo info = new OSMTileFactoryInfo("ZIP archive", "jar:file:/D:/sixth_sem/java_miniproject/ij_javafx/ShortestPath/ku_offline.zip!");
        TileFactoryInfo info = new OSMTileFactoryInfo("ZIP archive", "jar:file:ku_offline.zip!");

        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        GeoPosition KU = new GeoPosition(27.6195, 85.5386);

        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(KU);

// Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);

        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Add a selection painter
        SelectionAdapter sa = new SelectionAdapter(mapViewer);
        SelectionPainter sp = new SelectionPainter(sa);
        mapViewer.addMouseListener(sa);
        mapViewer.addMouseMotionListener(sa);
        mapViewer.setOverlayPainter(sp);



        //Add mouse click listner
        mapViewer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                    java.awt.Point p = e.getPoint();
                    GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                    mouseClick = geo;
                    System.out.println("X:" + geo.getLatitude() + ",Y:" + geo.getLongitude());
                }
            }
        });
        //Somehow first node gets 0,0 as its val so for quick fix skip first node

    }

    public static void addRoute(){
        DijkstraShortestPath<OSMNode, DefaultWeightedEdge> shortestPaths = new DijkstraShortestPath<>(graph);
        route = shortestPaths.findPathBetween(graph, start,end);


        List<GeoPosition> track = new LinkedList<>();
        for (OSMNode n : route.getVertexList()) {

            track.add(new GeoPosition(n.getLat(), n.getLon()));
        }
        track.remove(0);
        System.out.println(track);

        RoutePainter routePainter = new RoutePainter(track);

        // Set the focus
        Main.mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.7);

//        // Create waypoints from the geo-positions
//        List<Waypoint> waypointlist = new LinkedList<>();
//        for(GeoPosition x : track){
//            waypointlist.add(new DefaultWaypoint(x));
//        }

        // Create waypoints from the geo-positions
        List<Waypoint> waypointlist = new LinkedList<>();
        waypointlist.add(new DefaultWaypoint(track.get(1)));
        waypointlist.add(new DefaultWaypoint(track.get(track.toArray().length - 1)));


        Set<Waypoint> waypoints = new HashSet<Waypoint>(waypointlist);

        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        waypointPainter.setWaypoints(waypoints);

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<org.jxmapviewer.painter.Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        Main.mapViewer.setOverlayPainter(painter);

    }


    public static void setupMap() throws IOException {
        Path fileName
                = Path.of("weightedGraphKU.graphml");

        // Now calling Files.readString() method to
        // read the file
        String graphAsStr = Files.readString(fileName);

        // import it back+
        System.out.println("-- Importing graph back from GraphML");


        GraphImporter<OSMNode, DefaultWeightedEdge> importer = createImporter();
        importer.importGraph(graph, new StringReader(graphAsStr));






    }


}
