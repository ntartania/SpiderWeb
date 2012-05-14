/*
 * File:         GraphPopupMenu.java
 * Created:      05/08/2011
 * Last Changed: Date: 16/08/2011 
 * Author:       Andrew O'Hara
 * 				 Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import java.util.Timer;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PNetworkGraph;
import spiderweb.graph.P2PVertex;
import spiderweb.visualizer.NetworkGraphVisualizer;;

/**
 * 
 * 
 * @author Andrew O'Hara
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 22/08/2011
 */
public class GraphPopupMenu extends JPopupMenu implements ActionListener {

	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 1429587389581580356L;

	protected DefaultModalGraphMouse<P2PVertex,P2PConnection> gm;
	protected NetworkGraphVisualizer visualizer;

	protected JMenuItem balloonLayout;
	protected JMenuItem treeLayout;
	protected JMenuItem radialTreeLayout;
	protected JMenuItem treeSeparator;
	
	private Timer stopAnimateTimer;
	
	/**
	 * Initializes the right-click menu components and event handlers
	 * @param visualizer the <code>NetworkGraphVisualizer</code> to
	 * @param gm the graphMouse
	 */
	public GraphPopupMenu(NetworkGraphVisualizer visualizer, final DefaultModalGraphMouse<P2PVertex,P2PConnection> gm) {
		this.visualizer = visualizer;
		this.gm = gm;
		JMenuItem picking = new JMenuItem("Picking");
		JMenuItem transforming = new JMenuItem("Transforming");
		JMenuItem hideSelected = new JMenuItem("Hide Selected");
		JMenuItem showAll = new JMenuItem("Show All");
		JMenuItem circleLayout = new JMenuItem("Circle Layout");
		JMenuItem frLayout = new JMenuItem("FR Layout");
		JMenuItem isomLayout = new JMenuItem("ISOM Layout");
		JMenuItem kkLayout = new JMenuItem("KK Layout");
		JMenuItem springLayout = new JMenuItem("Spring Layout");
		
		radialTreeLayout = new JMenuItem("Radial Tree Layout");
		balloonLayout = new JMenuItem("Balloon Layout"); //Layouts for collapsed document view
		treeLayout = new JMenuItem("Tree Layout");
		treeSeparator = new JMenuItem("Tree Type Layouts:");
		treeSeparator.setEnabled(false);

		picking.addActionListener(this);
		transforming.addActionListener(this);
		hideSelected.addActionListener(this);
		showAll.addActionListener(this);
		kkLayout.addActionListener(this);
		frLayout.addActionListener(this);
		isomLayout.addActionListener(this);
		circleLayout.addActionListener(this);
		springLayout.addActionListener(this);
		balloonLayout.addActionListener(this);
		treeLayout.addActionListener(this);
		radialTreeLayout.addActionListener(this);

		add("Mouse Mode:").setEnabled(false);
		add(picking);
		add(transforming);
		addSeparator();
		
		add("Vertex Hiding:").setEnabled(false);
		add(hideSelected);
		add(showAll);
		addSeparator();
		
		add("Set Layout:").setEnabled(false);
		add(circleLayout);
		add(frLayout);
		add(isomLayout);
		add(kkLayout);
		add(springLayout);

		stopAnimateTimer = new Timer("freeze layout timer", true);
	}

	/**
	 * Shows the popup menu on the current viewer
	 */
	public void showPopupMenu(int x, int y) {
		setEnabled(true);
		if(!visualizer.getName().equals("Collapsed Document View")) {
			remove(treeSeparator);
			remove(balloonLayout);
			remove(treeLayout);
			remove(radialTreeLayout);
		}
		else {
			add(treeSeparator);
			add(balloonLayout);
			add(treeLayout);
			add(radialTreeLayout);
		}
		show(visualizer, x, y);
	}

	/**
	 * Handles popup menu button clicks
	 * @param text The text of the button that was just clicked
	 */
	public void popupMenuEvent(String text) {
		if (text.contains(("Layout"))) {
			Layout<P2PVertex,P2PConnection> currentLayout = visualizer.getGraphLayout();
			Layout<P2PVertex,P2PConnection> newLayout = null;
			P2PNetworkGraph graph = (P2PNetworkGraph) visualizer.getGraphLayout().getGraph();
			if (text.equals("FR Layout")) {
				newLayout = new FRLayout<P2PVertex,P2PConnection>(graph, visualizer.getSize());
			} else if (text.equals("ISOM Layout")) {
				newLayout = new ISOMLayout<P2PVertex,P2PConnection>(graph);
			} else if (text.equals("KK Layout")) {
				newLayout = new KKLayout<P2PVertex,P2PConnection>(graph);
			} else if (text.equals("Spring Layout")) {
				newLayout = new SpringLayout2<P2PVertex,P2PConnection>(graph);
			} else if (text.equals("Balloon Layout")) {
				newLayout = new BalloonLayout<P2PVertex, P2PConnection>(P2PNetworkGraph.makeTreeGraph(graph));
			} else if (text.equals("Radial Tree Layout")) {
				newLayout = new RadialTreeLayout<P2PVertex,P2PConnection>(P2PNetworkGraph.makeTreeGraph(graph));
			} else if (text.equals("Tree Layout")) {
				newLayout = new TreeLayout<P2PVertex,P2PConnection>(P2PNetworkGraph.makeTreeGraph(graph));
			} else { //use circle layout as default as it is fast
				newLayout = new CircleLayout<P2PVertex,P2PConnection>(graph);
			}
			newLayout.setInitializer(visualizer.getGraphLayout());
			if(!newLayout.getClass().equals(TreeLayout.class)) { //Tree layout cannot call setSize()
				newLayout.setSize(visualizer.getSize());
			}
			
			//animate between one layout and the next
			LayoutTransition<P2PVertex,P2PConnection> transition =
				new LayoutTransition<P2PVertex,P2PConnection>(visualizer, currentLayout, newLayout);
			Animator transitionAnimator = new Animator(transition);
			transitionAnimator.start(); 
			
			stopAnimateTimer.schedule(new StopAnimateTask<P2PVertex,P2PConnection>(transitionAnimator, newLayout), 5000);
			
			visualizer.getRenderContext().getMultiLayerTransformer().setToIdentity();
		} 
		else if (text.equals("Picking")) {
			gm.setMode(Mode.PICKING);
		} 
		else if (text.equals("Transforming")) {
			gm.setMode(Mode.TRANSFORMING);
		} 
		else if (text.equals("Hide Selected")) {
			for(P2PVertex v : visualizer.getPickedVertexState().getPicked()) {
				v.setHidden(true);
			}
		}
		else if (text.equals("Show All")) {
			for(P2PVertex v : visualizer.getGraphLayout().getGraph().getVertices()) {
				v.setHidden(false);
			}
		}
		else {
			JOptionPane.showMessageDialog(this, "GraphPopupMenu.popupMenuEvent(): "+text, "Uncaught menu action",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		visualizer.repaint();
		setVisible(false);
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String buttonText = ((AbstractButton)e.getSource()).getText();
		popupMenuEvent(buttonText);
	}

	/**
	 * task class which stops the layout transition animator and locks the verices on the layout.
	 * 
	 * This class can run as a java.util.Timer Timer Task 
	 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
	 * @param <V> The type of graph Vertex used in the layout
	 * @param <E> The type of graph Edge used in the layout
	 * @see java.util.Timer
	 * @see java.util.TimerTask
	 */
	private class StopAnimateTask<V, E> extends TimerTask {

		private Animator animatorToStop;
		private Layout<V, E> layoutToLock;
		
		/**
		 * This Task stops the animator and locks all the vertices in graph for the passed layout.
		 * @param animatorToStop The Layout Animator used for the transition
		 * @param layoutToLock The new layout being transitioned to
		 */
		public StopAnimateTask(Animator animatorToStop, Layout<V,E> layoutToLock) {
			this.animatorToStop = animatorToStop;
			this.layoutToLock = layoutToLock;
		}
		
		@Override
		public void run() {
			animatorToStop.stop();
			for(V v : layoutToLock.getGraph().getVertices()) {
				layoutToLock.lock(v, true);
			}
		}
		
	}
}