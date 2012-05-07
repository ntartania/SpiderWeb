package spiderweb;

import org.apache.commons.collections15.Transformer;
/**
 * Size function for the vertices in a graph representing a P2P network.
 * The size of the shapes representing the nodes are given in the constructor.
 * This class has three attributes, that indicate the size of vertices representing peers, 
 * the size of vertices representing documents stored by the peers, and a parameter that indicates 
 * the limit between integers (vertex labels) representing documents and integers representing peers   
 * @author adavoust
 *
 * 
 */
public class P2PVertexSizeFunction implements Transformer<P2PVertex,Integer>{

	int my_doc_size;
	int my_peer_doc_size;
	int my_peer_size;
	
	/**
	 * Constructor
	 * @param dmin min value for document vertex labels (below = peers) 
	 * @param ds document vertex size
	 * @param ps peer document size
	 */
	public P2PVertexSizeFunction(int ds, int ps, int pds){
		
		my_doc_size = ds;
		my_peer_size = ps;
		my_peer_doc_size = pds;
	}
	
	@Override
	public Integer transform(P2PVertex vertexID) {
		
		if (vertexID instanceof PeerVertex) {
			return new Integer(my_peer_size);
		}
		else if (vertexID instanceof PeerDocumentVertex) {
			return new Integer(my_peer_doc_size);
		}
		else {
			return new Integer(my_doc_size);
		}
	}
	    
}
