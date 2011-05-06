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
    public void addPeer( int peernumber){
    	P2PVertex v1 = new P2PVertex(P2PVertex.PEER, peernumber);
    	super.addVertex(v1);
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
    
    /** adding a document connected to a peer*/
    public void addDocument(int docnumber, int peer){
    	P2PVertex vdoc = P2PVertex.PeerPublishesDoc(peer, docnumber);
		addVertex(vdoc);
		//create a vertex that we can compare with the ones in the graph to find the peer vertex
		P2PVertex vpeer = P2PVertex.makePeerVertex(peer);
		Integer label = new Integer(++edgecounter); // increment edgecounter then add edge 
		addEdge(new P2PConnection(P2PConnection.P2DOC,label), vdoc, vpeer);
    }
    
    public void removeDocument(int docnumber, int peer) {
    	P2PVertex doc = getVertexInGraph(P2PVertex.PeerPublishesDoc(peer, docnumber));
    	P2PVertex publisher = getVertexInGraph(P2PVertex.makePeerVertex(peer));
    	P2PConnection edge = findEdge(publisher,doc);
    	
    	super.removeEdge(edge);
    	removeVertex(doc);
    }
    
    
    /**
     * add an edge to the graph
     * @param number
     */
    public void connectPeers(int from, int to) {
    	Integer edge = new Integer(++edgecounter);
    	System.out.println(edgecounter);
    	addEdge(new P2PConnection(P2PConnection.P2P,edge), getVertexInGraph(P2PVertex.makePeerVertex(from)), getVertexInGraph(P2PVertex.makePeerVertex(to)));
    }
    /**
     * remove an edge from the graph
     * @param number
     */
    public void disconnectPeers(int from, int to) {
    	P2PConnection edge = findEdge(getVertexInGraph(P2PVertex.makePeerVertex(from)), getVertexInGraph(P2PVertex.makePeerVertex(to)));
    	
    	super.removeEdge(edge);
    }

    /** apply a log event to transform a graph*/
	public void event(LogEvent gev, boolean reverse) {
		if(!reverse) {
			if (gev.getType().equals("online")){
				addPeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				removePeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				connectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("disconnect")){
				disconnectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("publish")){
				addDocument(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("depublish")){
				removeDocument(gev.getParam(2), gev.getParam(1));
			}
		} else {
			if (gev.getType().equals("online")){
				removePeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				addPeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				disconnectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("disconnect")){
				connectPeers(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("publish")){
				removeDocument(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("depublish")){
				addDocument(gev.getParam(2), gev.getParam(1));
			}
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
