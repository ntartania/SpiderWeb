package spiderweb.graph;

import spiderweb.visualizer.eventplayer.LogEvent;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;

public class P2PNetworkGraph extends DirectedSparseGraph<P2PVertex, P2PConnection> implements Graph<P2PVertex, P2PConnection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int edgecounter= 0;
	
	/**
	 * Copy Constructor.
	 * @param copy the P2PNetworkGraph to copy.
	 */
	//public P2PNetworkGraph(P2PNetworkGraph copy) {
	//	this.edgecounter = copy.edgecounter;
	//}
	
	public P2PNetworkGraph() {
		super();
	}
	
	 /** adding a peer in the network*/
    public void addPeer(int peernumber){
    	P2PVertex v1 = new PeerVertex(peernumber);
    	super.addVertex(v1);
    }
    /**removing a peer : must remove all associated docs*/
    public void removePeer(int peernum){
    	P2PVertex peer = new PeerVertex(peernum);
    	Collection<P2PConnection> edgeset = getIncidentEdges(peer);
    	Set<PeerDocumentVertex> docsToRemove = new TreeSet<PeerDocumentVertex>();
    	//TODO use an iterator?
    	//TODO use removeDocument
    	for (P2PConnection e: edgeset){
    		P2PVertex node = getOpposite(peer, e); //node is what the edge connects the peer to  
    		if (node instanceof PeerDocumentVertex)//if that node is a document
    			docsToRemove.add((PeerDocumentVertex)node);
    	}
    			
    	for (PeerDocumentVertex n :docsToRemove){
    		removeDocument(n.getDocumentNumber(),n.getPeerNumber());
    	}
    	removeVertex(peer);
    }
    
    
    public void addDocument(int docnumber, int peer){
    	addDocument(docnumber, peer, new Integer(++edgecounter));
    	addPeerDocument(docnumber,peer,new Integer(++edgecounter),new Integer(++edgecounter));
    }
    
    public void addPeerDocument(int docnumber, int peer, Integer peerEdgeKey, Integer docEdgeKey) {
    	P2PVertex pDocV = new PeerDocumentVertex(peer, docnumber);
    	addVertex(pDocV);
    	
    	P2PVertex vpeer = new PeerVertex(peer);
    	P2PVertex vdoc = new DocumentVertex(docnumber);
		addEdge(new P2PConnection(P2PConnection.P2PDOC,peerEdgeKey), vpeer, pDocV);
		//System.out.println("addEdge: "+pDocV+" to "+vdoc);
			addEdge(new P2PConnection(P2PConnection.DOC2PDOC,docEdgeKey), pDocV,vdoc);
    	
		//System.out.println("crash");
    }
    
    public void addDocument(int docnumber, int peer, Integer edgeKey) {
    	P2PVertex vdoc = new DocumentVertex(docnumber);
		addVertex(vdoc);
		//create a vertex that we can compare with the ones in the graph to find the peer vertex
		P2PVertex vpeer = getVertexInGraph(new PeerVertex(peer));
		addEdge(new P2PConnection(P2PConnection.P2DOC,edgeKey), vpeer, vdoc);
    }
    
    
    public void removeDocument(int docnumber, int peer) {
    	P2PVertex peerDoc = getVertexInGraph(new PeerDocumentVertex(peer, docnumber));
    	P2PVertex publisher = getVertexInGraph(new PeerVertex(peer));
    	P2PVertex document = getVertexInGraph(new DocumentVertex(docnumber));
    	P2PConnection peerToPeerDocEdge = findEdge(publisher,peerDoc);
    	P2PConnection peerToDocEdge = findEdge(publisher, document);
    	P2PConnection docToPeerDocEdge = findEdge(document,peerDoc);
    	
    	
    	removeEdge(peerToDocEdge);
    	removeEdge(peerToPeerDocEdge);
    	removeEdge(docToPeerDocEdge);
    	removeVertex(peerDoc);
    	if(getIncidentEdges(document).size() == 0) {
    		removeVertex(document);
    	}
    }
    
    public void connectDocuments(int doc1, int doc2) {
    	connectDocuments(doc1, doc2, new Integer(++edgecounter));
    }
    
    public void connectDocuments(int doc1, int doc2, Integer edgeKey) {
    	P2PVertex documentOne = getVertexInGraph(new DocumentVertex(doc1));
    	P2PVertex documentTwo = getVertexInGraph(new DocumentVertex(doc2));
    	
    	addEdge(new P2PConnection(P2PConnection.DOC2DOC,edgeKey), documentOne, documentTwo);
    }
    
    public void disconnectDocuments(int from, int to) {
    	P2PConnection edge = findDocumentToDocumentConnection(from, to);
    	
    	super.removeEdge(edge);
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
    	P2PConnection edge = new P2PConnection(P2PConnection.P2P,key);
    	if(!containsEdge(edge)) {
	    	addEdge(edge, getVertexInGraph(new PeerVertex(from)), getVertexInGraph(new PeerVertex(to)));
    	}
    	
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
	
	public DocumentVertex getDocument(int documentNumber) {
		return (DocumentVertex)getVertexInGraph(new DocumentVertex(documentNumber));
	}
	
	public PeerDocumentVertex getPeerDocument(int publisherNumber, int documentNumber) {
		return (PeerDocumentVertex)getVertexInGraph(new PeerDocumentVertex(publisherNumber,documentNumber));
	}
	
	/**
	 * Returns an edge that connects the vertex which the peer number peerFrom represents to the vertex which peer number peerTo represents.
	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
	 * @param peerTo	Peer that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two peers.
	 */
	public P2PConnection findPeerConnection(int peerFrom, int peerTo){
		P2PConnection connection = findEdge(new PeerVertex(peerFrom), new PeerVertex(peerTo));
		return connection;
	}
	
	/**
	 * Returns an edge that connects the two Document vertices which the numbers represent.
	 * @param peerFrom	Document that the edge emerges from (vertex 1)
	 * @param peerTo	Document that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two Documents.
	 */
	public P2PConnection findDocumentToDocumentConnection(int docFrom, int docTo){
		P2PConnection connection = findEdge(new DocumentVertex(docFrom), new DocumentVertex(docTo));
		return connection;
	}
	
	/**
	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
	 * @param docnumber	Document that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two peers.
	 */
	public P2PConnection findPeerToDocConnection(int peer, int docnumber){
		P2PVertex p = getVertexInGraph(new PeerVertex(peer));
		P2PVertex doc = getVertexInGraph(new DocumentVertex(docnumber));
		P2PConnection connection = findEdge(p, doc);
		return connection;
	}
	
	/**
	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
	 * @param docnumber	Document that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two peers.
	 */
	public P2PConnection findPeerToPeerDocConnection(int peer, int docnumber){
		return findEdge(new PeerVertex(peer), new PeerDocumentVertex(peer, docnumber));
	}
	
	/**
	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
	 * @param docnumber	Document that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two peers.
	 */
	public P2PConnection findDocToPeerDocConnection(int peer, int docnumber){
		return findEdge(new PeerDocumentVertex(peer, docnumber), new DocumentVertex(docnumber));
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
	 * Returns a tree graph of documents and the peers which host them.
	 * @param graph	The source which the tree Graph will be made from
	 * @return	The Document Tree Graph
	 */
	public static Forest<P2PVertex, P2PConnection> makeTreeGraph(P2PNetworkGraph graph) {
		Forest<P2PVertex, P2PConnection> tree = new DelegateForest<P2PVertex, P2PConnection>();
		for(P2PVertex documentVertex : graph.getVertices()) { //iterate over all vertices in the graph
			if(documentVertex.getClass().equals(DocumentVertex.class)) { //a document represents the root of a tree in the graph
				//DocumentVertex docVertex = new DocumentVertex(p2pV);
				tree.addVertex(documentVertex);
				for(P2PConnection edge : graph.getOutEdges(documentVertex)) { //get all the document's edges to find all the peers connected to it
					P2PVertex opposite =  graph.getOpposite(documentVertex, edge);
					if(opposite.getClass().equals(DocumentVertex.class)) {
						tree.addEdge(edge, documentVertex, opposite);
					}
				}
				for(P2PConnection edge : graph.getInEdges(documentVertex)) {
					P2PVertex opposite =  graph.getOpposite(documentVertex, edge);
					if(opposite.getClass().equals(PeerDocumentVertex.class)) {
						tree.addEdge(edge, documentVertex, opposite);
					}
				}
			}
		}
		return tree;
	}
	
	/**
	 * Limited version of graphEvent for construction a graph for layout purposes
	 * @param gev	The Log event which needs to be handled.
	 * @param g		The Graph to perform the event on.
	 */
	public void graphConstructionEvent(LogEvent gev) {
		try{
		if (gev.getType().equals("online")){
			addPeer(gev.getParam(1));
		} else if(gev.getType().equals("connect")){
			connectPeers(gev.getParam(1), gev.getParam(2));
			connectPeers(gev.getParam(2), gev.getParam(1));
		} else if(gev.getType().equals("publish")){
			addDocument(gev.getParam(2), gev.getParam(1));
		} else if(gev.getType().equals("linkdocument")) {
			connectDocuments(gev.getParam(1), gev.getParam(2));
		}
		}
		catch(Exception e) {
			System.out.println(gev);
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles the Log Events which affect the structure of the graph.
	 * @param gev				The Log event which needs to be handled.
	 * @param forward			<code>true</code> if play-back is playing forward.
	 * @param eventGraph		The Graph to perform the event on.
	 * @param referenceGraph	The Graph to get edge numbers from.
	 */
	public void graphEvent(LogEvent gev, boolean forward, P2PNetworkGraph referenceGraph) {
		
		if(forward) {
			if (gev.getType().equals("online")){
				addPeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				removePeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
				connectPeers(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerConnection(gev.getParam(2), gev.getParam(1)).getKey());
			} else if(gev.getType().equals("disconnect")){
				disconnectPeers(gev.getParam(1), gev.getParam(2));
				disconnectPeers(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("publish")){				
				addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
				addPeerDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey(),
																				 referenceGraph.findDocToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("depublish")){
				removeDocument(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("linkdocument")) {
				connectDocuments(gev.getParam(1), gev.getParam(2),referenceGraph.findDocumentToDocumentConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("delinkdocument")) {
				disconnectDocuments(gev.getParam(1), gev.getParam(2));
			}
		} else {
			if (gev.getType().equals("online")){
				removePeer(gev.getParam(1));
			} else if (gev.getType().equals("offline")){
				addPeer(gev.getParam(1));
			} else if(gev.getType().equals("connect")){
				disconnectPeers(gev.getParam(1), gev.getParam(2));
				disconnectPeers(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("disconnect")){
				connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
				connectPeers(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerConnection(gev.getParam(2), gev.getParam(1)).getKey());
			} else if(gev.getType().equals("publish")){
				removeDocument(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("depublish")){
				addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
				addPeerDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey(),
																				 referenceGraph.findDocToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("linkdocument")) {
				disconnectDocuments(gev.getParam(1), gev.getParam(2));
			} else if(gev.getType().equals("delinkdocument")) {
				connectDocuments(gev.getParam(1), gev.getParam(2),referenceGraph.findDocumentToDocumentConnection(gev.getParam(1), gev.getParam(2)).getKey());
			}
		}
	}
	/*
	@Override
	public P2PNetworkGraph clone() {
		try {
			P2PNetworkGraph cloned = (P2PNetworkGraph)super.clone();
			cloned.edgecounter = this.edgecounter;
			return cloned;
		} 
		catch(CloneNotSupportedException e) {
			System.out.println(e);
			return null;
		}
	}
	*/
}