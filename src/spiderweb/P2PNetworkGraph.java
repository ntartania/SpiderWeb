package spiderweb;

import java.util.Collection;
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
    public void addPeer(int peernumber){
    	P2PVertex v1 = new PeerVertex(peernumber);
    	super.addVertex(v1);
    }
    /**removing a peer : must remove all associated docs*/
    public void removePeer(int peernum){
    	P2PVertex peer = new PeerVertex(peernum);
    	Collection<P2PConnection> edgeset = getIncidentEdges(peer);
    	Set<P2PVertex> docsToRemove = new TreeSet<P2PVertex>();
    	for (P2PConnection e: edgeset){
    		P2PVertex node = getOpposite(peer, e); //node is what the edge connects the peer to  
    		if (node instanceof DocumentVertex)//if that node is a document
    			docsToRemove.add(node);
    	}
    			
    	for (P2PVertex n :docsToRemove){
    		removeVertex(n);
    	}
    	removeVertex(peer);
    }
    
    
    
    /** adding a document connected to a peer*/
    public void addDocument(int docnumber, int peer){
    	addDocument(docnumber, peer, new Integer(++edgecounter));
    }
    
    public void addDocument(int docnumber, int peer, Integer edgeKey) {
    	P2PVertex vdoc = new DocumentVertex(peer, docnumber);
		addVertex(vdoc);
		//create a vertex that we can compare with the ones in the graph to find the peer vertex
		P2PVertex vpeer = new PeerVertex(peer);
		addEdge(new P2PConnection(P2PConnection.P2DOC,edgeKey), vdoc, vpeer);
    }
    
    public void removeDocument(int docnumber, int peer) {
    	P2PVertex doc = getVertexInGraph(new DocumentVertex(peer, docnumber));
    	P2PVertex publisher = getVertexInGraph(new PeerVertex(peer));
    	P2PConnection edge = findEdge(publisher,doc);
    	
    	super.removeEdge(edge);
    	removeVertex(doc);
    }
    
    /**
     * add an edge to the graph
     * @param from	peer 1 (vertex 1)
     * @param to	peer 2 (vertex 2)
     */
    public void connectPeers(int from, int to) {
    	connectPeers(from,to,new Integer(++edgecounter));
    }
    /**
     * add an edge to the graph
     * @param from	peer 1 (vertex 1)
     * @param to	peer 2 (vertex 2)
     */
    public void connectPeers(int from, int to, Integer key) {
    	P2PConnection p = new P2PConnection(P2PConnection.P2P,key);
    	addEdge(p, getVertexInGraph(new PeerVertex(from)), getVertexInGraph(new PeerVertex(to)));
    }
    /**
     * remove an edge from the graph
     * @param number
     */
    public void disconnectPeers(int from, int to) {
    	P2PConnection edge = findPeerConnection(from, to);
    	
    	super.removeEdge(edge);
    }

	/**
	 * this methods gets a vertex already in the graph that is equal to the input vertex
	 * to be used when adding edges; the edge should relate two vertices actually in the graph, not copies of these vertices.
	 * @param input a P2PVertex object
	 * @return a P2PVertex v such that v.equals(input) and v is in the graph 
	 */
	public P2PVertex getVertexInGraph(P2PVertex input){
		for (P2PVertex v : vertices.keySet()){
			if (v.equals(input))
				return v;
		}
		return null;
	}
	
	public PeerVertex getPeer(int peerNumber) {
    	return (PeerVertex)getVertexInGraph(new PeerVertex(peerNumber));
    }
	
	public DocumentVertex getDocument(int publisherNumber, int documentNumber) {
		return (DocumentVertex)getVertexInGraph(new DocumentVertex(publisherNumber,documentNumber));
	}
	
	/**
	 * Returns an edge that connects the vertex which the peer number peerFrom represents to the vertex which peer number peerTo represents.
	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
	 * @param peerTo	Peer that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two peers.
	 */
	public P2PConnection findPeerConnection(int peerFrom, int peerTo){
		return findEdge(new PeerVertex(peerFrom), new PeerVertex(peerTo));
	}
	/**
	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
	 * @param peerTo	Peer that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two peers.
	 */
	public P2PConnection findDocConnection(int peer, int docnumber){
		return findEdge(new PeerVertex(peer), new DocumentVertex(peer, docnumber));
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
	
	
	/**
	 * Limited version of graphEvent for construction a graph for layout purposes
	 * @param gev	The Log event which needs to be handled.
	 * @param g		The Graph to perform the event on.
	 */
	public static void graphConstructionEvent(LogEvent gev, P2PNetworkGraph g) {
		if (gev.getType().equals("online")){
			g.addPeer(gev.getParam(1));
		} else if(gev.getType().equals("connect")){
			g.connectPeers(gev.getParam(1), gev.getParam(2));
		} else if(gev.getType().equals("publish")){
			g.addDocument(gev.getParam(2), gev.getParam(1));
		}
	}
	
	/**
	 * Handles the Log Events which affect the structure of the graph.
	 * @param gev				The Log event which needs to be handled.
	 * @param forward			<code>true</code> if play-back is playing forward.
	 * @param eventGraph		The Graph to perform the event on.
	 * @param referenceGraph	The Graph to get edge numbers from.
	 */
	public static void graphEvent(LogEvent gev, boolean forward, P2PNetworkGraph eventGraph, P2PNetworkGraph referenceGraph) {
		
		if(forward) {
			if (gev.getType().equals("online")){
				eventGraph.addPeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				eventGraph.removePeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				eventGraph.connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("disconnect")){
				eventGraph.disconnectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("publish")){
				eventGraph.addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("depublish")){
				eventGraph.removeDocument(gev.getParam(2), gev.getParam(1));
			}
		} else {
			if (gev.getType().equals("online")){
				eventGraph.removePeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				eventGraph.addPeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				eventGraph.disconnectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("disconnect")){
				eventGraph.connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("publish")){
				eventGraph.removeDocument(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("depublish")){
				eventGraph.addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
			}
		}
	}
}
