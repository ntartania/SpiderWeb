package spiderweb.visualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;

import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PNetworkGraph;
import spiderweb.graph.P2PVertex;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * This is a TrustGraphViewer component
 * It displays the graph passed to it from inside the layout parameter
 * @author Andrew O'Hara
 */
public class PersonalizedVisualizationViewer extends edu.uci.ics.jung.visualization.VisualizationViewer<P2PVertex,P2PConnection>  {

	/**eclipse generated Serial UID*/
	private static final long serialVersionUID = 3695469692236559338L;


	/**
	 * 
	 * @param layout
	 * @param width
	 * @param height
	 * @param gm
	 * @param mouseClickListener
	 * @param graph
	 */
    public PersonalizedVisualizationViewer(final Layout<P2PVertex,P2PConnection> layout, int width, int height, 
    		DefaultModalGraphMouse<P2PVertex,P2PConnection>  gm, MouseAdapter mouseClickListener, P2PNetworkGraph referenceGraph) {
        super(layout, new Dimension(width, height));
        // the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
        setGraphMouse(gm);

        //the vertex labeler will use the tostring method which is fine, the Agent class has an appropriate toString() method implementation
        getRenderContext().setVertexLabelTransformer(new ToStringLabeller<P2PVertex>());
        getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //the Edge labeler will use the tostring method which is fine, each testbedEdge subclass has an appropriate toString() method implementation
        //getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<P2PConnection>());
        //Sets the predicates which decide whether the vertices and edges are displayed
        
        getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(getPickedVertexState()));
		// P2PVertex objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.
        
        getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(referenceGraph));
        getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(referenceGraph));

        setForeground(Color.WHITE);
        setBackground(Color.GRAY);
        setBounds(0, 0, width, height);
        validate();
        
        addMouseListener(mouseClickListener); //This listener handles the mouse clicks to see if a popup event was done

        addComponentListener(new ComponentAdapter() {

            /**
             * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
             */
            @Override
            public void componentResized(ComponentEvent arg0) {
                super.componentResized(arg0);
            }
        });
    }
    
    public void setSpecialTransformers(VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
			EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape) {
		
		//add my own vertex shape & colour fill transformers
		getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(peerShape,documentShape, peerDocumentShape));
		// note :the colour depends on being picked.
		
		//make the p2p edges different from the peer to doc edges 
		getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
		getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer(P2PEdgeShape,P2DocEdgeShape,Doc2PDocEdgeShape,P2PDocEdgeShape)); //stroke width
		
	}
}