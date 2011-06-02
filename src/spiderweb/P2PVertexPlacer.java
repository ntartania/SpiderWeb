package spiderweb;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * this vertex placer is used in the spring layout to initialize the position of a new node in the graph when calculating the layout.
 * Peers should start in random positions, but docs should start next to (here at the exact same location as) the peer that publishes the doc 
 * @author alan
 *
 */
public class P2PVertexPlacer implements Transformer<P2PVertex, Point2D> {

	private Layout<P2PVertex,P2PConnection> existinglayout;
	private RandomLocationTransformer<P2PVertex> rt;
	
	public P2PVertexPlacer(Layout<P2PVertex,P2PConnection> l, Dimension d){
		existinglayout = l;
		rt= new RandomLocationTransformer<P2PVertex>(d);
	}
	@Override
	public Point2D transform(P2PVertex v) {
		if (v instanceof PeerVertex) {
			return rt.transform(v); //placing a peer
		}
		else if(v instanceof PeerDocumentVertex) {
			return existinglayout.transform(new PeerVertex(((PeerDocumentVertex)v).getPeerNumber()));
		}
		else { //placing a document : put it on it's publisher's position in the layout...
			return rt.transform(v);
		}
	}
}
