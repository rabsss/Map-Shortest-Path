import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import static javax.swing.JOptionPane.showMessageDialog;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    final static boolean shouldFill = true;
    final static boolean shouldWeightX = true;

    public static JFrame frame;
    public static JXMapViewer mapViewer;


    public static void addComponentsToPane(Container pane) throws IOException {

        JButton button;
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        if (shouldFill) {
            //natural height, maximum width
            c.fill = GridBagConstraints.HORIZONTAL;
        }

        JButton button1 = new JButton("Add Starting Point");

        c.weightx = 0.25;
        c.weighty = 0.10;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(button1, c);
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.print("Button1");
                GeoPosition click = OSMMap.getClick();
                if(click==null){
                    showMessageDialog(frame,"Seect a point with right click first");
                }else {
                    OSMNode near = OSMMap.getNearestNode(OSMMap.graph, click);
                    if (near != null) {
                        OSMMap.setStart(near);
                    } else {
                        showMessageDialog(frame, "choose a point near KU");

                    }
                }
            }
        });

        JButton button2 = new JButton("Add Ending Point");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.25;
        c.weighty = 0.10;
        c.gridx = 1;
        c.gridy = 0;
        pane.add(button2, c);
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.print("Button2");
                GeoPosition click = OSMMap.getClick();
                if(click==null){
                    showMessageDialog(frame,"Seect a point with right click first");
                }else {
                    OSMNode near = OSMMap.getNearestNode(OSMMap.graph, click);
                    if (near != null) {
                        OSMMap.setEnd(near);
                    } else {
                        showMessageDialog(frame, "choose a point near KU");

                    }
                }
            }
        });

        JButton button3 = new JButton("Find Path");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.25;
        c.weighty = 0.10;
        c.gridx = 2;
        c.gridy = 0;
        pane.add(button3, c);
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (OSMMap.start == null || OSMMap.end == null) {
                    showMessageDialog(frame, "Select Both Start and End Positions First");
                }
                else {
                    OSMMap.addRoute();
                    StringBuilder vertices = new StringBuilder("\n\n");
                    for(OSMNode n: OSMMap.route.getVertexList()){
                        vertices.append(n.toString());
                        vertices.append("\n");

                    }
                    showMessageDialog(frame, "Route Found Sucessfully!\n\n" +
                            String.format("Route Length %.2f meters", OSMMap.route.getWeight()) +
                            vertices

                    );
                }
            }
        });

        JButton button4 = new JButton("Info");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.25;
        c.weighty = 0.10;
        c.gridx = 3;
        c.gridy = 0;
        pane.add(button4, c);
        button4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showMessageDialog(frame, "JAVA MiniProject on Shortest Path Finding on OSM Map\n\n " +
                        "Use Left Mouse to Pan , Right to select a point\n" +
                        "Scroll or pinch to zoom\n\n" +
                        "Created by ::\nArun Bhoomi\n" +
                        "Pradeep Upadhyay\n" +
                        "Prawal Parajuli\n" +
                        "Rabina Poudyal\n\n" +
                        "Submitted to :: Mr. Uma Shankar Panday ");
            }
        });

        mapViewer = new JXMapViewer();
        OSMMap.setupMap();
        OSMMap.setupMapViewer(mapViewer);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 4;
        c.weightx = 1;
        c.weighty = 0.80;
        c.insets = new Insets(5, 5, 5, 5);
        pane.add(mapViewer, c);

        String clickedposmsg = "";
        JTextArea posmsg= new JTextArea(clickedposmsg,1,1);
        pane.add(posmsg);

        if (OSMMap.mouseClick == null) {
            clickedposmsg = "No point Selected";
        } else {
           clickedposmsg = "Current Selected Coordinate : " + OSMMap.mouseClick.toString();
        }
    }

    /**
     * This is the main class.
     */
    public static void main(String[] args) throws IOException {

//        //Test that OSM node class works
//        OSMNode mynode = new OSMNode("2121");
//        System.out.print(mynode);

        frame = new JFrame("GridBagLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);

        //Set up the content pane.
        addComponentsToPane(frame.getContentPane());

        //Display the window.
//        frame.pack();
        frame.setVisible(true);


    }
}