/*
 * File:         NetworkGraphVisualizer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      09/08/2011
 * Last Changed: Date: 16/08/2011 
 * Author:       Andrew O'Hara
 * 				 Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;

import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;

import spiderweb.graph.DocumentVertex;
import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;
import spiderweb.graph.PeerDocumentVertex;
import spiderweb.graph.PeerVertex;
import spiderweb.graph.ReferencedNetworkGraph;
import spiderweb.visualizer.predicates.OptionsPredicate;
import spiderweb.visualizer.transformers.P2PEdgeShapeTransformer;
import spiderweb.visualizer.transformers.P2PEdgeStrokeTransformer;
import spiderweb.visualizer.transformers.P2PVertexFillPaintTransformer;
import spiderweb.visualizer.transformers.P2PVertexShapeTransformer;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * This is a TrustGraphViewer component
 * It displays the graph passed to it from inside the layout parameter
 * @author Andrew O'Hara
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 */
public class NetworkGraphVisualizer extends VisualizationViewer<P2PVertex,P2PConnection> implements ViewChangeListener  {

	//default size for the swing graphic components
	public static final int DEFWIDTH = 1360;
	public static final int DEFHEIGHT = 768;

	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 3695469692236559338L;

	private ReferencedNetworkGraph graph;
	private boolean scaleVertices;

	private VisualOptionsPanel optionsPanel;
	private ViewState currentView;
	
	private OptionsPredicate includePredicate;

	public NetworkGraphVisualizer(final Layout<P2PVertex,P2PConnection> layout, DefaultModalGraphMouse<P2PVertex,P2PConnection>  gm, 
			ReferencedNetworkGraph graph, int width, int height) {
		super(layout, new Dimension(width, height));

		// the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
		setGraphMouse(gm);

		this.graph = graph;

		scaleVertices = false;

		optionsPanel = new VisualOptionsPanel();
		optionsPanel.addViewListener(this);
		
		//
		includePredicate = new OptionsPredicate(graph.getDynamicGraph());
		getRenderContext().setVertexIncludePredicate(includePredicate);

		//the vertex labeler uses the toString method and displays the label at the center of the vertex
		getRenderContext().setVertexLabelTransformer(new ToStringLabeller<P2PVertex>());
		getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

		validate();
	}

	public NetworkGraphVisualizer(final Layout<P2PVertex, P2PConnection> layout, DefaultModalGraphMouse<P2PVertex,P2PConnection>  gm, 
			ReferencedNetworkGraph graph) {
		this(layout, gm, graph, DEFWIDTH, DEFHEIGHT);
	}

	public static NetworkGraphVisualizer getPersonalizedVisualizer(final Layout<P2PVertex,P2PConnection> layout, 
			DefaultModalGraphMouse<P2PVertex,P2PConnection>  gm, ReferencedNetworkGraph graph, int width, int height) {

		NetworkGraphVisualizer visualizer = new NetworkGraphVisualizer(layout, gm, graph, width, height);

		visualizer.currentView = ViewState.CollapsedPeerView;
		visualizer.optionsPanel.setView(visualizer.currentView); //will trigger the event to switch the view of the visualizer

		


		visualizer.setForeground(Color.WHITE);
		visualizer.setBackground(Color.GRAY);

		return visualizer;
	}

	private void setSpecialTransformers(VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
			EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape) {

		//add my own vertex shape & colour fill transformers
		getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer<P2PVertex, P2PConnection>(graph.getReferenceGraph(), peerShape, documentShape, peerDocumentShape));
		// note :the colour depends on being picked.

		//make the p2p edges different from the peer to doc edges 
		getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
		getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(P2PEdgeShape,P2DocEdgeShape,Doc2PDocEdgeShape,P2PDocEdgeShape)); //stroke width
	}

	/**
	 * Returns the options panel component for displaying.
	 * @return The visual options JPanel corresponding to this visualizer.
	 */
	public JPanel getOptionsPanel() {
		return optionsPanel;
	}

	/**
	 * Sets the view predicate corresponding to the passed ViewState
	 * @param view The new ViewState to switch to
	 */
	public void setView(ViewState view) {
		setName(view.toString());
		currentView = view;
		switch(view) {
		case FullView:
			setExcludeType(null);
			break;
		case CollapsedPeerView:
			setExcludeType(DocumentVertex.class);
			break;
		case CollapsedDocumentView:
			setCollapsedDocumentView();
			break;
		case CollapsedPeerAndDocumentView:
			setExcludeType(PeerDocumentVertex.class);
			break;
		}
		setScaling();
		repaint();
	}

	private void setCollapsedDocumentView() {	
		includePredicate.setExcludeClass(PeerVertex.class);
		
		getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(
				getPickedVertexState(),Color.RED, Color.YELLOW, Color.MAGENTA, Color.RED, Color.RED, Color.BLUE));

		getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer<P2PVertex, P2PConnection>(
				graph.getReferenceGraph(), VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.ELLIPSE, 
				P2PVertexShapeTransformer.PEER_SIZE, P2PVertexShapeTransformer.DOC_SIZE, P2PVertexShapeTransformer.PEER_SIZE));

		getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,EdgeShapeType.LINE,EdgeShapeType.LINE));
	}
	
	private void setExcludeType(Class<? extends P2PVertex> exclude) {
		includePredicate.setExcludeClass(exclude);
		
		getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(getPickedVertexState()));
		
		setSpecialTransformers(VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
				EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,
				EdgeShapeType.LINE,
				EdgeShapeType.LINE);
	}
	
	

	/**
	 * want to do this with a listener, so not going to document
	 * @param scale
	 */
	public void setScaling(boolean scale) {
		scaleVertices = scale;

		Transformer<P2PVertex, Shape> shapeTransformer = getRenderContext().getVertexShapeTransformer();
		if(shapeTransformer.getClass().equals(P2PVertexShapeTransformer.class)) {
			P2PVertexShapeTransformer<P2PVertex, P2PConnection> scalableTransformer = 
				(P2PVertexShapeTransformer<P2PVertex, P2PConnection>)shapeTransformer;

			scalableTransformer.setScaling(scaleVertices);
		}
		repaint();
	}

	/**
	 * Helper to set the scaling to the current value of scaleVertices
	 */
	private void setScaling() {
		setScaling(scaleVertices);
	}

	@Override
	public void viewChanged(ViewState view) {
		setView(view);
	}
}