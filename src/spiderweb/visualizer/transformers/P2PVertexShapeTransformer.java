/*
 * File:         P2PVertexShapeTransformer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       >Matthew Smith
 * 				 Alan Davoust
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.transformers;

import java.awt.Shape;

import org.apache.commons.collections15.Transformer;

import spiderweb.graph.PeerDocumentVertex;
import spiderweb.graph.PeerVertex;
import spiderweb.visualizer.VertexShapeType;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;

/**
 * A "transformer" that maps vertices to shapes. There will be two types of vertices, peers and documents. Peers are large and round (size PEER_SIZE = 15), documents are smaller (size DOC_SIZE=6) and rectangular. Peers have numbers less than DOC_MIN. Documents have numbers above DOC_MIN. This class also uses P2PVertexSizeFunction to assign sizes to the vertices.
 * @author  adavoust
 * @param  <V >
 */
public class P2PVertexShapeTransformer<V, E> extends AbstractVertexShapeTransformer<V> 
		implements Transformer<V,Shape>
{
	public static final int DOC_SIZE = 20;
	public static final int PEER_SIZE = 25;
	public static final int PEER_DOC_SIZE = 15;
	
	private VertexShapeType peerShape;
	private VertexShapeType documentShape;
	private VertexShapeType peerDocumentShape;
	
	private P2PVertexSizeTransformer<V, E> sizeTransformer;
	
		public P2PVertexShapeTransformer(Graph<V,E> graphIn, VertexShapeType peerShape, 
				VertexShapeType documentShape, VertexShapeType peerDocumentShape) 
	    {
			this(graphIn, peerShape, documentShape, peerDocumentShape, PEER_SIZE, DOC_SIZE, PEER_DOC_SIZE);
	    }
	    
		public P2PVertexShapeTransformer(Graph<V,E> graphIn, VertexShapeType peerShape, VertexShapeType documentShape, 
				VertexShapeType peerDocumentShape, int peerSize, int documentSize, int peerDocumentSize) 
	    {	    	
	    	sizeTransformer = new P2PVertexSizeTransformer<V, E>(peerSize,documentSize,peerDocumentSize, graphIn);
	    	setSizeTransformer(sizeTransformer);
	    	this.peerShape = peerShape;
	    	this.documentShape = documentShape;
	    	this.peerDocumentShape = peerDocumentShape;
	    }
	    
	    public Shape transform(V v)
	    {
	    		if (v instanceof PeerVertex) {
	    			return shapeChooser(v, peerShape);
	    		}
	    		if (v instanceof PeerDocumentVertex) {
	    			return shapeChooser(v, peerDocumentShape);
	    		}
	    		else {
	    			return shapeChooser(v, documentShape);
	    			
	    		}
	    }
	    
	    private Shape shapeChooser (V v, VertexShapeType chosenShape) {
	    	//factory.getEllipse(v);
	    	switch(chosenShape) {
		    	case ELLIPSE: 
		    		return factory.getEllipse(v);
		    	case RECTANGLE: 
		    		return factory.getRectangle(v);
		    	case PENTAGON: 
		    		return factory.getRegularPolygon(v,5);
		    	case STAR: 
		    		return factory.getRegularStar(v,8);
		    	case ROUND_RECTANGLE: 
		    		return factory.getRoundRectangle(v);
		    		
	    	}
	    	return factory.getRectangle(v);
	    }
	    
	    public void setScaling(boolean scale) {
	    	sizeTransformer.setScaling(scale);
	    }
}