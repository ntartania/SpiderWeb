/*
 * File:         GraphPopupMenu.java
 * Created:      05/08/2011
 * Last Changed: Date: 5/08/2011 
 * Author:       Andrew O'Hara
 * 				 Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PNetworkGraph;
import spiderweb.graph.P2PVertex;
import spiderweb.visualizer.PersonalizedVisualizationViewer;

/**
 * Initializes the right-click menu components and event handlers (Does not contain the listeners)
 * 
 * @author Andrew O'Hara
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 05/08/2011
 */
public class GraphPopupMenu extends JPopupMenu {

	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 1429587389581580356L;

	private DefaultModalGraphMouse<P2PVertex,P2PConnection> gm;
	private P2PGraphViewer applet;

	JMenuItem balloonLayout = new JMenuItem("Balloon Layout");
	JMenuItem treeLayout = new JMenuItem("Tree Layout");

	public GraphPopupMenu(P2PGraphViewer applet, final DefaultModalGraphMouse<P2PVertex,P2PConnection> gm, 
			ActionListener listener) {
		this.applet = applet;
		this.gm = gm;
		JMenuItem picking = new JMenuItem("Picking");
		JMenuItem transforming = new JMenuItem("Transforming");
		JMenuItem kkLayout = new JMenuItem("KK Layout");
		JMenuItem frLayout = new JMenuItem("FR Layout");
		JMenuItem isomLayout = new JMenuItem("ISOM Layout");
		JMenuItem circleLayout = new JMenuItem("Circle Layout");
		balloonLayout = new JMenuItem("Balloon Layout");
		treeLayout = new JMenuItem("Tree Layout");

		picking.addActionListener(listener);
		transforming.addActionListener(listener);
		kkLayout.addActionListener(listener);
		frLayout.addActionListener(listener);
		isomLayout.addActionListener(listener);
		circleLayout.addActionListener(listener);
		balloonLayout.addActionListener(listener);
		treeLayout.addActionListener(listener);

		add("Mouse Mode:").setEnabled(false);
		add(picking);
		add(transforming);
		addSeparator();
		add("Set Layout:").setEnabled(false);
		add(circleLayout);
		add(frLayout);
		add(isomLayout);
		add(kkLayout);

	}

	/**
	 * Shows the popup menu on the current viewer
	 */
	public void showPopupMenu(int x, int y) {
		setEnabled(true);
		PersonalizedVisualizationViewer currentViewer = applet.getCurrentViewer();
		if(!currentViewer.getName().equals("Collapsed Document View")) {
			remove(balloonLayout);
			remove(treeLayout);
		}
		else {
			add(balloonLayout);
			add(treeLayout);
		}
		show(currentViewer, x, y);
	}

	/**
	 * Handles popup menu button clicks
	 * @param text The text of the button that was just clicked
	 */
	public void popupMenuEvent(String text) {
		if (text.contains(("Layout"))) {
			PersonalizedVisualizationViewer currentViewer = applet.getCurrentViewer();
			Layout<P2PVertex,P2PConnection> graphLayout = null;
			P2PNetworkGraph graph = applet.getGraph().getReferenceGraph();
			if (text.equals("FR Layout")) {
				graphLayout = new FRLayout<P2PVertex,P2PConnection>(graph, currentViewer.getSize());
			} else if (text.equals("ISOM Layout")) {
				graphLayout = new ISOMLayout<P2PVertex,P2PConnection>(graph);
			} else if (text.equals("KK Layout")) {
				graphLayout = new KKLayout<P2PVertex,P2PConnection>(graph);
			} else if (text.equals("Balloon Layout")) {
				graphLayout = new BalloonLayout<P2PVertex, P2PConnection>(P2PNetworkGraph.makeTreeGraph(graph));
				//graphLayout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH,DEFHEIGHT)));
			} else if (text.equals("Tree Layout")) {
				graphLayout = new TreeLayout<P2PVertex,P2PConnection>(P2PNetworkGraph.makeTreeGraph(graph));
			} else {
				graphLayout = new CircleLayout<P2PVertex,P2PConnection>(graph);
			}
			currentViewer.getModel().setGraphLayout(graphLayout);
			
			for(P2PVertex v : graphLayout.getGraph().getVertices()) {
				graphLayout.lock(v, true);
			} // the lock all method does not exist in the layout interface
			//graphLayout.lock(true);
		} else if (text.equals("Picking")) {
			gm.setMode(Mode.PICKING);
		} else if (text.equals("Transforming")) {
			gm.setMode(Mode.TRANSFORMING);
		} else {
			JOptionPane.showMessageDialog(this, "GraphPopupMenu.popupMenuEvent(): "+text, "Uncaught menu action",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setVisible(false);
		setEnabled(false);
	}

}