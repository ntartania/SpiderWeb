

package spiderweb;

import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * A "transformer" that maps vertices to colors.
	 * 
	 * There will be two types of vertices, peers and documents, which will have different colors.
	 * 
	 * @author adavoust
	 *
	 * @param <V>
	 */
	public class P2PVertexFillPaintTransformer extends PickableVertexPaintTransformer<P2PVertex> implements Transformer<P2PVertex,Paint>
	{
		public static final Color PEER_COLOR = Color.RED;
		public static final Color PICKED_PEER_COLOR = Color.YELLOW;		
		public static final Color PEER_ANSWERING_COLOR = Color.PINK;
		public static final Color PEER_QUERY_COLOR = Color.MAGENTA; 
		public static final Color DOC_QUERYHIT_COLOR = Color.LIGHT_GRAY; 
		public static final Color DOC_COLOR = Color.BLUE; 
		
		
		    public P2PVertexFillPaintTransformer(PickedInfo<P2PVertex> pi) 
		    {
		    	super(pi, PEER_COLOR, PICKED_PEER_COLOR);
		    }
		    
		    public Paint transform(P2PVertex v)
		    {
		    	
		    		if (v.isPeer())
		    			if (pi.isPicked(v)) //differentiate picked peers with unpicked peers.
		    				return PICKED_PEER_COLOR;
		    			else
		    				if(v.getQueryState()==P2PVertex.QUERYING)
		    					return PEER_QUERY_COLOR;
		    				else if (v.getQueryState()==P2PVertex.ANSWERING)
		    					return PEER_ANSWERING_COLOR;
		    				else
		    					return PEER_COLOR;
		    		else // it's a document then !
		    			if (v.getQueryState()==P2PVertex.MATCHING_DOC)
		    				return DOC_QUERYHIT_COLOR;
		    			else
		    				return DOC_COLOR;

		    }
		

	}
