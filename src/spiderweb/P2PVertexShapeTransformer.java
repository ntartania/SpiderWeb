package spiderweb;

import java.awt.Shape;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;

/**
 * A "transformer" that maps vertices to shapes.
 * 
 * There will be two types of vertices, peers and documents.
 * Peers are large and round (size PEER_SIZE = 15), documents are smaller (size DOC_SIZE=6) and rectangular.
 * Peers have numbers less than DOC_MIN. Documents have numbers above DOC_MIN.
 * 
 * This class also uses P2PVertexSizeFunction to assign sizes to the vertices.
 * 
 * @author adavoust
 *
 * @param <V>
 */
public class P2PVertexShapeTransformer extends AbstractVertexShapeTransformer<P2PVertex> 
		implements Transformer<P2PVertex,Shape>
{
	public static final int DOC_SIZE = 20;
	public static final int PEER_SIZE = 25;
	public static final int PEER_DOC_SIZE = 15;
	
	private VertexShapeType peerShape;
	private VertexShapeType documentShape;
	private VertexShapeType peerDocumentShape;
	
	    @SuppressWarnings({ "rawtypes", "unchecked" })
		public P2PVertexShapeTransformer(VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape) 
	    {
	    	super ( new P2PVertexSizeFunction(DOC_SIZE,PEER_SIZE,PEER_DOC_SIZE), new ConstantTransformer(1.0f));
	    	this.peerShape = peerShape;
	    	this.documentShape = documentShape;
	    	this.peerDocumentShape = peerDocumentShape;
	    }
	    
	    @SuppressWarnings({ "rawtypes", "unchecked" })
		public P2PVertexShapeTransformer(VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
										 int peerSize, int documentSize, int peerDocumentSize) 
	    {
	    	super ( new P2PVertexSizeFunction(peerSize,documentSize,peerDocumentSize), new ConstantTransformer(1.0f));
	    	this.peerShape = peerShape;
	    	this.documentShape = documentShape;
	    	this.peerDocumentShape = peerDocumentShape;
	    }
	    
	    public Shape transform(P2PVertex v)
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
	    
	    private Shape shapeChooser (P2PVertex v, VertexShapeType chosenShape) {
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
	

}