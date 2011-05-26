

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
		public static final Color DEF_PEER_COLOR = Color.RED;
		public static final Color DEF_PICKED_PEER_COLOR = Color.YELLOW;		
		public static final Color DEF_PEER_ANSWERING_COLOR = Color.PINK;
		public static final Color DEF_PEER_QUERY_COLOR = Color.MAGENTA; 
		public static final Color DEF_PEERDOC_QUERYHIT_COLOR = Color.LIGHT_GRAY; 
		public static final Color DEF_PEERDOC_COLOR = Color.ORANGE; 
		public static final Color DEF_DOC_COLOR = Color.BLUE; 
		
		private Color peerColor = DEF_PEER_COLOR;
		private Color pickedPeerColor = DEF_PICKED_PEER_COLOR;
		private Color peerQueryColor = DEF_PEER_QUERY_COLOR;
		private Color peerDocQueryHitColor = DEF_PEERDOC_QUERYHIT_COLOR;
		private Color peerDocColor = DEF_PEERDOC_COLOR;
		private Color docColor = DEF_DOC_COLOR;		
		
		    public P2PVertexFillPaintTransformer(PickedInfo<P2PVertex> pi) 
		    {
		    	super(pi, DEF_PEER_COLOR, DEF_PICKED_PEER_COLOR);
		    }
		    public P2PVertexFillPaintTransformer(PickedInfo<P2PVertex> pi, Color peerColor, Color pickedPeerColor,
		    		Color peerQueryColor, Color peerDocQueryHitColor, Color peerDocColor, Color docColor) 
		    {
		    	super(pi, peerColor, pickedPeerColor);
		    	this.peerColor = peerColor;
		    	this.pickedPeerColor = pickedPeerColor;
		    	this.peerQueryColor = peerQueryColor;
		    	this.peerDocQueryHitColor = peerDocQueryHitColor;
		    	this.peerDocColor = peerDocColor;
		    	this.docColor = docColor;
		    	
		    }
		    
		    public Paint transform(P2PVertex v)
		    {
		    	//is it a peer
	    		if (v instanceof PeerVertex) {
	    			if (pi.isPicked(v)) { //differentiate picked peers with unpicked peers.
	    				return pickedPeerColor;
	    			}
	    			else {
	    				if( ((PeerVertex)v).hasOutgoingQueries() ) {
	    					return peerQueryColor;
	    				}
	    				return peerColor;
	    			}
	    		}
	    		else if (v instanceof PeerDocumentVertex) {
	    			if ( ((PeerDocumentVertex)v).isQueryHit() ) {
	    				return peerDocQueryHitColor;
	    			}
	    			else {
	    				return peerDocColor;
	    			}
	    		}
	    		else {
	    			return docColor;
	    		}

		    }
		

	}