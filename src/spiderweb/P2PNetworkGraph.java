package spiderweb;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class P2PNetworkGraph extends UndirectedSparseGraph<P2PVertex, P2PConnection> implements Graph<P2PVertex, P2PConnection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int edgecounter= 0;
	
	 /** adding a peer in the network*/
    public void addPeer( int peernumber){
    	P2PVertex v1 = new P2PVertex(P2PVertex.PEER, peernumber);
    	super.addVertex(v1);
    }
    
    /** adding a document connected to a peer*/
    public void addDocument(int docnumber, int peer){
    	P2PVertex vdoc = P2PVertex.PeerPublishesDoc(peer, docnumber);
		addVertex(vdoc);
		//create a vertex that we can compare with the ones in the graph to find the peer vertex
		P2PVertex vpeer = P2PVertex.makePeerVertex(peer);
		Integer label = new Integer(++edgecounter); // increment edgecounter then add edge 
		addEdge(new P2PConnection(P2PConnection.P2DOC,label), vdoc, vpeer);
    }
    
    /**removing a peer : must remove all associated docs*/
    public void removePeer(int peernum){
    	P2PVertex peer = P2PVertex.makePeerVertex(peernum);
    	Collection<P2PConnection> edgeset = getIncidentEdges(peer);
    	Set<P2PVertex> docsToRemove = new TreeSet<P2PVertex>();
    	for (P2PConnection e: edgeset){
    		P2PVertex node = getOpposite(peer, e); //node is what the edge connects the peer to  
    		if (!node.isPeer())//if that node is a document
    			docsToRemove.add(node);
    	}
    			
    	for (P2PVertex n :docsToRemove){
    		removeVertex(n);
    	}
    	removeVertex(peer);
    	
    }
    
    /**
     * add an edge to the graph
     * @param number
     */
    public void connectPeers(int from, int to) {

    	Integer edge = new Integer(++edgecounter);
    	
    	addEdge(new P2PConnection(P2PConnection.P2P,edge), getVertexInGraph(P2PVertex.makePeerVertex(from)), getVertexInGraph(P2PVertex.makePeerVertex(to)));

    }

    /** apply a log event to transform a graph*/
	public void event(LogEvent gev) {
		if (gev.getType().equals("online")){
			addPeer(gev.getParam(1));
		} else if (gev.getType().equals("offline")){
			removePeer(gev.getParam(1));
		} else if(gev.getType().equals("connect")){
			connectPeers(gev.getParam(1), gev.getParam(2));
		} else if(gev.getType().equals("publish")){
			addDocument(gev.getParam(2), gev.getParam(1));
		} 
	}
	/**
	 * this methods gets a vertex already in the graph that is equal to the input vertex
	 * to be used when adding edges; the edge should relate two vertices actually in the graph, not copies of these vertices.
	 * @param input a P2PVertex object
	 * @return a P2PVertex v such that v.equals(input) and v is in the graph 
	 */
	private P2PVertex getVertexInGraph(P2PVertex input){
		for (P2PVertex v : vertices.keySet()){
			if (v.equals(input))
				return v;
		}
		return null;
	}

	//override these methods so the underlying collection is not unmodifiable
	@Override
	public Collection<P2PConnection> getEdges()
    {
        return edges.keySet();
    }

	@Override
    public Collection<P2PVertex> getVertices()
    {
        return vertices.keySet();
    }
}
