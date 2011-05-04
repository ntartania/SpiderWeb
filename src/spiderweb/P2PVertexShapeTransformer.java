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
	//public static final int DOC_MIN = 10000;
	public static final int DOC_SIZE = 8;
	public static final int PEER_SIZE = 25;
	
	    public P2PVertexShapeTransformer() 
	    {
	    	super ( new P2PVertexSizeFunction(DOC_SIZE,PEER_SIZE), new ConstantTransformer(1.0f));
	    }
	    /*public P2PVertexShapeTransformer(Transformer<V,Integer> vsf, Transformer<V,Float> varf)
	    {
	        super(vsf, varf);
	    }*/
	    
	    public Shape transform(P2PVertex v)
	    {
	    		if (((P2PVertex) v).isPeer())
	    			return factory.getEllipse(v);
	    		else
	    			return factory.getRectangle(v);
	    }
	

}