/*
 * File:         NetworkGraphVisualizer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      09/08/2011
 * Last Changed: Date: 13/08/2011 
 * Author:       Andrew O'Hara
 * 				 Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.collections15.Transformer;

import spiderweb.graph.DocumentVertex;
import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;
import spiderweb.graph.PeerDocumentVertex;
import spiderweb.graph.PeerVertex;
import spiderweb.graph.ReferencedNetworkGraph;
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
public class NetworkGraphVisualizer extends VisualizationViewer<P2PVertex,P2PConnection>  {

	//default size for the swing graphic components
	public static final int DEFWIDTH = 1360;
	public static final int DEFHEIGHT = 768;
	
	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 3695469692236559338L;
	
	private ReferencedNetworkGraph graph;
	private boolean scaleVertices;
	
    public NetworkGraphVisualizer(final Layout<P2PVertex,P2PConnection> layout, DefaultModalGraphMouse<P2PVertex,P2PConnection>  gm, 
    		ReferencedNetworkGraph graph, int width, int height) {
        super(layout, new Dimension(width, height));
        
        // the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
        setGraphMouse(gm);
        
        this.graph = graph;
        
        scaleVertices = false;
        
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
    	
    	visualizer.setCollapsedPeerView();
    	
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
    
    public JPanel getOptionsPanel() {
    	
    	ButtonGroup buttonGroup = new ButtonGroup();
    	
    	JPanel viewPanel = new JPanel(new GridLayout(4,1));
    	viewPanel.setBorder(BorderFactory.createTitledBorder("Graph View"));
    	
    	ChangeViewListener changeViewListener = new ChangeViewListener(this);
    	
    	JRadioButton fullView = new JRadioButton("Full View");
    	fullView.addActionListener(changeViewListener);
    	viewPanel.add(fullView);
    	buttonGroup.add(fullView);
    	
    	JRadioButton collapsedPeerView = new JRadioButton("Collapsed Peer View");
    	collapsedPeerView.addActionListener(changeViewListener);
    	viewPanel.add(collapsedPeerView);
    	buttonGroup.add(collapsedPeerView);
    	
    	JRadioButton collapsedDocumentView = new JRadioButton("Collapsed Document View");
    	collapsedDocumentView.addActionListener(changeViewListener);
    	viewPanel.add(collapsedDocumentView);
    	buttonGroup.add(collapsedDocumentView);
    	
    	JRadioButton collapsedPeerAndDocumentView = new JRadioButton("Collapsed Peer and Document View");
    	collapsedPeerAndDocumentView.addActionListener(changeViewListener);
    	viewPanel.add(collapsedPeerAndDocumentView);
    	buttonGroup.add(collapsedPeerAndDocumentView);
    	
    	if(getName().equals(fullView.getText())) {
    		fullView.setSelected(true);
    	} else if(getName().equals(collapsedPeerView.getText())) {
    		collapsedPeerView.setSelected(true);
    	} else if(getName().equals(collapsedDocumentView.getText())) {
    		collapsedDocumentView.setSelected(true);
    	} else if(getName().equals(collapsedPeerAndDocumentView.getText())) {
    		collapsedPeerAndDocumentView.setSelected(true);
    	}
    	
    	JPanel visualInfo = new JPanel(new GridLayout(1,1)); 
    	visualInfo.setBorder(BorderFactory.createTitledBorder("Visual Options"));
    	
    	JCheckBox scaleButton = new JCheckBox("Scale Vertices");
    	scaleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				
				Transformer<P2PVertex, Shape> shapeTransformer = getRenderContext().getVertexShapeTransformer();
				if(shapeTransformer.getClass().equals(P2PVertexShapeTransformer.class)) {
					P2PVertexShapeTransformer<P2PVertex, P2PConnection> scalableTransformer = (P2PVertexShapeTransformer<P2PVertex, P2PConnection> )shapeTransformer;
					if(((JCheckBox)ae.getSource()).isSelected()) {
						scalableTransformer.setScaling(true);
					}
					else {
						scalableTransformer.setScaling(false);
					}
				}
				repaint();
			}
    	});
    	visualInfo.add(scaleButton);
    	
    	JPanel optionsPanel =  new JPanel(new BorderLayout());
    	optionsPanel.add(viewPanel,BorderLayout.NORTH);
    	optionsPanel.add(visualInfo,BorderLayout.CENTER);
    	return optionsPanel;
    }
    
    public void setCollapsedDocumentView() {
    	setName("Collapsed Document View");		
		getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(
				getPickedVertexState(),Color.RED, Color.YELLOW, Color.MAGENTA, Color.RED, Color.RED, Color.BLUE));
		getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer<P2PVertex, P2PConnection>(
				graph.getReferenceGraph(), VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.ELLIPSE, 
				P2PVertexShapeTransformer.PEER_SIZE, P2PVertexShapeTransformer.DOC_SIZE, P2PVertexShapeTransformer.PEER_SIZE));
		getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,EdgeShapeType.LINE,EdgeShapeType.LINE));
		

		getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(graph.getDynamicGraph(),PeerVertex.class));
		repaint();
    }
    
    public void setCollapsedPeerView() {
		setName("Collapsed Peer View");
		getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(getPickedVertexState()));
		getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(graph.getDynamicGraph(), DocumentVertex.class));
		setSpecialTransformers(VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
				EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,
				EdgeShapeType.LINE,
				EdgeShapeType.LINE);
		repaint();
    }
    
    public void setCollapsedPeerAndDocumentView() {
		setName("Collapsed Peer and Document View");
		getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(getPickedVertexState()));
		getRenderContext().setVertexIncludePredicate(new ExclusiveVertexInOtherGraphPredicate(graph.getDynamicGraph(),PeerDocumentVertex.class));
			
		setSpecialTransformers(VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
				EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,
				EdgeShapeType.LINE,
				EdgeShapeType.LINE);
		repaint();
    }
    
    public void setFullView() {
    	setName("Full View");
    	getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(getPickedVertexState()));
    	getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(graph.getDynamicGraph()));
		
        setSpecialTransformers(VertexShapeType.ELLIPSE,VertexShapeType.PENTAGON,VertexShapeType.RECTANGLE,
				EdgeShapeType.QUAD_CURVE,
				EdgeShapeType.CUBIC_CURVE,
				EdgeShapeType.LINE,
				EdgeShapeType.LINE);
        repaint();
    }
}