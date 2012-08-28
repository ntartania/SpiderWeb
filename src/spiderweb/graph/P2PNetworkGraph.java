/*
 * File:         P2PNetworkGraph.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 05/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;

/**
 * P2PNetworkGraph represents a graph which has 
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 05/08/2011 
 */
public class P2PNetworkGraph extends DirectedSparseGraph<P2PVertex, P2PConnection> implements Graph<P2PVertex, P2PConnection> {

	/**eclipse generated serialUID*/
	private static final long serialVersionUID = -5569894854823217541L;
	
	//used in counting the number of edges that are in the graph
	private int edgecounter = 0;

	/**
	 * 
	 */
	public P2PNetworkGraph() {
		super();
	}

	/**
	 * adds a new <code>PeerVertex</code> with the passed peer number as its identifier
	 * @param peernumber the new <code>PeerVertex</code>'s identifier
	 */
	protected void addPeer(int peernumber){
		P2PVertex v1 = new PeerVertex(peernumber);
		super.addVertex(v1);
	}

	/**
	 * removes the <code>PeerVertex</code> corresponding to the passed peer number;
	 * also removes any <code>PeerDocumentVertex</code> that are attached to this peer.
	 * @param peernum the identifier of the <code>PeerVertex</code> to remove.
	 */
	protected void removePeer(int peernum){
		P2PVertex peer = new PeerVertex(peernum);
		Collection<P2PConnection> edgeset = getIncidentEdges(peer);
		Set<PeerDocumentVertex> docsToRemove = new TreeSet<PeerDocumentVertex>();

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


	protected void addDocument(int docnumber, int peer){
		addDocument(docnumber, peer, new Integer(++edgecounter));
		addPeerDocument(docnumber,peer,new Integer(++edgecounter),new Integer(++edgecounter));
	}

	protected void addPeerDocument(int docnumber, int peer, Integer peerEdgeKey, Integer docEdgeKey) {
		P2PVertex pDocV = new PeerDocumentVertex(peer, docnumber);
		addVertex(pDocV);

		P2PVertex vpeer = new PeerVertex(peer);
		P2PVertex vdoc = new DocumentVertex(docnumber);
		addEdge(new P2PConnection(P2PConnection.P2PDOC,peerEdgeKey), vpeer, pDocV);
		//System.out.println("addEdge: "+pDocV+" to "+vdoc);
		addEdge(new P2PConnection(P2PConnection.DOC2PDOC,docEdgeKey), pDocV,vdoc);

		//System.out.println("crash");
	}

	protected void addDocument(int docnumber, int peer, Integer edgeKey) {
		P2PVertex vdoc = getDocument(docnumber);
		
		if(vdoc==null) {
			vdoc = new DocumentVertex(docnumber);
			addVertex(vdoc);
		}
		//create a vertex that we can compare with the ones in the graph to find the peer vertex
		P2PVertex vpeer = getVertexInGraph(new PeerVertex(peer));
		addEdge(new P2PConnection(P2PConnection.P2DOC,edgeKey), vpeer, vdoc);
	}


	protected void removeDocument(int docnumber, int peer) {
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
		if(document != null) {
			if(getIncidentEdges(document).size() == 0) {
				removeVertex(document);
			}
		}
	}

	protected void connectDocuments(int doc1, int doc2) {
		connectDocuments(doc1, doc2, new Integer(++edgecounter));
	}

	protected void connectDocuments(int doc1, int doc2, Integer edgeKey) {
		P2PVertex documentOne = getVertexInGraph(new DocumentVertex(doc1));
		P2PVertex documentTwo = getVertexInGraph(new DocumentVertex(doc2));

		addEdge(new P2PConnection(P2PConnection.DOC2DOC,edgeKey), documentOne, documentTwo);
	}

	protected void disconnectDocuments(int from, int to) {
		P2PConnection edge = findDocumentToDocumentConnection(from, to);

		super.removeEdge(edge);
	}

	protected void connectPeers(int from, int to) {
		connectPeers(from,to,new Integer(++edgecounter));
	}
	/**
	 * add an edge to the graph
	 * @param from	peer 1 (vertex 1)
	 * @param to	peer 2 (vertex 2)
	 */
	protected void connectPeers(int from, int to, Integer edgeKey) {
		P2PConnection edge = new P2PConnection(P2PConnection.P2P,edgeKey);
		if(!containsEdge(edge)) {
			addEdge(edge, getVertexInGraph(new PeerVertex(from)), getVertexInGraph(new PeerVertex(to)));
		}

	}
	protected void disconnectPeers(int from, int to) {
		P2PConnection edge = findPeerConnection(from, to);

		super.removeEdge(edge);
	}

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

	public boolean isPeerOnline(int peerNumber) {
		return getPeer(peerNumber) != null;
	}

	public boolean isDocumentPublished(int documentNumber) {
		return getDocument(documentNumber) != null;
	}

	public P2PConnection findPeerConnection(int peerFrom, int peerTo){
		P2PConnection connection = findEdge(new PeerVertex(peerFrom), new PeerVertex(peerTo));
		return connection;
	}

	public boolean arePeersConnected(int peer1, int peer2) {
		return (findPeerConnection(peer1, peer2) != null) && (findPeerConnection(peer2, peer1) != null);
	}

	public P2PConnection findDocumentToDocumentConnection(int docFrom, int docTo){
		P2PConnection connection = findEdge(new DocumentVertex(docFrom), new DocumentVertex(docTo));
		return connection;
	}

	public boolean areDocumentsConnected(int docFrom, int docTo) {
		return (findDocumentToDocumentConnection(docFrom, docTo) != null);
	}

	public P2PConnection findPeerToDocConnection(int peer, int docnumber){
		P2PVertex p = getVertexInGraph(new PeerVertex(peer));
		P2PVertex doc = getVertexInGraph(new DocumentVertex(docnumber));
		P2PConnection connection = findEdge(p, doc);
		return connection;
	}

	/**
	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
	 * @param peer	Peer that the edge emerges from (vertex 1)
	 * @param docnumber	Document that the edge terminates at (vertex 2)
	 * @return			The edge that connects the two peers.
	 */
	public P2PConnection findPeerToPeerDocConnection(int peer, int docnumber){
		return findEdge(new PeerVertex(peer), new PeerDocumentVertex(peer, docnumber));
	}

	/**
	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
	 * @param peer	Peer that the edge emerges from (vertex 1)
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
	 * @param evt	The Log event which needs to be handled.
	 */
	public void graphEvent(LogEvent evt) {
		if (evt.getType().equals("online")){
			int peerNumber = evt.getParam(1);
			//check to make sure peer is not online already
			if(!isPeerOnline(peerNumber)) {
				addPeer(peerNumber);
			}
		} else if(evt.getType().equals("connect")){
			int peer1 = evt.getParam(1);
			int peer2 = evt.getParam(2);
			//check peers are online and that they are not already connected
			//if peer is not online, come online
			if(!isPeerOnline(peer1)) {
				addPeer(peer1);
			}
			if(!isPeerOnline(peer2)) {
				addPeer(peer2);
			}
			if(!arePeersConnected(peer1, peer2)) {
				connectPeers(peer1, peer2);
				connectPeers(peer2, peer1);
			}
		} else if(evt.getType().equals("publish")){
			int peer = evt.getParam(1);
			int document = evt.getParam(2);
			//check peer is online, check peer has not already published document
			//if peer is not online, come online
			if(!isPeerOnline(peer)) {
				addPeer(peer);
			}
			addDocument(document, peer);
		} else if(evt.getType().equals("linkdocument")) {
			int doc1 = evt.getParam(1);
			int doc2 = evt.getParam(2);
			//check documents are both published
			if(isDocumentPublished(doc1) && isDocumentPublished(doc2)) {
				connectDocuments(doc1, doc2);
			}
		} 
	}
	

	/**
	 * Handles the Log Events which affect the structure of the graph.
	 * @param evt				The Log event which needs to be handled.
	 * @param forward			<code>true</code> if play-back is playing forward.
	 */
	public void graphEvent(LogEvent evt, boolean forward, P2PNetworkGraph referenceGraph) {
		if(forward) {
			if (evt.getType().equals("online")){
				addPeer(evt.getParam(1));
			} else if (evt.getType().equals("offline")){
				removePeer(evt.getParam(1));
			} else if(evt.getType().equals("connect")){
				connectPeers(evt.getParam(1), evt.getParam(2), referenceGraph.findPeerConnection(evt.getParam(1), evt.getParam(2)).getKey());
				connectPeers(evt.getParam(2), evt.getParam(1), referenceGraph.findPeerConnection(evt.getParam(2), evt.getParam(1)).getKey());
			} else if(evt.getType().equals("disconnect")){
				disconnectPeers(evt.getParam(1), evt.getParam(2));
				disconnectPeers(evt.getParam(2), evt.getParam(1));
			} else if(evt.getType().equals("publish")){
				addDocument(evt.getParam(2), evt.getParam(1), referenceGraph.findPeerToDocConnection(evt.getParam(1), evt.getParam(2)).getKey());
				addPeerDocument(evt.getParam(2), evt.getParam(1), referenceGraph.findPeerToPeerDocConnection(evt.getParam(1), evt.getParam(2)).getKey(),
						referenceGraph.findDocToPeerDocConnection(evt.getParam(1), evt.getParam(2)).getKey());
			} else if(evt.getType().equals("remove")){
				removeDocument(evt.getParam(2), evt.getParam(1));
			} else if(evt.getType().equals("linkdocument")) {
				connectDocuments(evt.getParam(1), evt.getParam(2),referenceGraph.findDocumentToDocumentConnection(evt.getParam(1), evt.getParam(2)).getKey());
			} else if(evt.getType().equals("delinkdocument")) {
				disconnectDocuments(evt.getParam(1), evt.getParam(2));
			}
		} else {
			if (evt.getType().equals("online")){
				removePeer(evt.getParam(1));
			} else if (evt.getType().equals("offline")){
				addPeer(evt.getParam(1));
			} else if(evt.getType().equals("connect")){
				disconnectPeers(evt.getParam(1), evt.getParam(2));
				disconnectPeers(evt.getParam(2), evt.getParam(1));
			} else if(evt.getType().equals("disconnect")){
				connectPeers(evt.getParam(1), evt.getParam(2), referenceGraph.findPeerConnection(evt.getParam(1), evt.getParam(2)).getKey());
				connectPeers(evt.getParam(2), evt.getParam(1), referenceGraph.findPeerConnection(evt.getParam(2), evt.getParam(1)).getKey());
			} else if(evt.getType().equals("publish")){
				removeDocument(evt.getParam(2), evt.getParam(1));
			} else if(evt.getType().equals("remove")){
				addDocument(evt.getParam(2), evt.getParam(1), referenceGraph.findPeerToDocConnection(evt.getParam(1), evt.getParam(2)).getKey());
				addPeerDocument(evt.getParam(2), evt.getParam(1), referenceGraph.findPeerToPeerDocConnection(evt.getParam(1), evt.getParam(2)).getKey(),
						referenceGraph.findDocToPeerDocConnection(evt.getParam(1), evt.getParam(2)).getKey());
			} else if(evt.getType().equals("linkdocument")) {
				disconnectDocuments(evt.getParam(1), evt.getParam(2));
			} else if(evt.getType().equals("delinkdocument")) {
				connectDocuments(evt.getParam(1), evt.getParam(2),referenceGraph.findDocumentToDocumentConnection(evt.getParam(1), evt.getParam(2)).getKey());
			}
		}
	}
	
	/**
	 * Handles the Log Events which affect the structure of the graph and compensates for 
	 * discrepancy with graph in its current state.
	 * 
	 * Primarily used for online as the edges are keyed similar to how edges are keyed when a graph is constructed.
	 * @param events The list of log events (will be modified if discrepancy found)
	 * @param currentIndex The index of the event to handle.
	 */
	public void robustGraphEvent(List<LogEvent> events, int currentIndex) {
		LogEvent evt = events.get(currentIndex);

		if (evt.getType().equals("online")){
			int peerNumber = evt.getParam(1);
			//check to make sure peer is not online already
			if(!isPeerOnline(peerNumber)) {
				addPeer(peerNumber);
			}
		} else if (evt.getType().equals("offline")){
			int peerNumber = evt.getParam(1);
			//check to make sure peer is online
			if(isPeerOnline(peerNumber)) {
				removePeer(peerNumber);
			}
		} else if(evt.getType().equals("connect")){
			int peer1 = evt.getParam(1);
			int peer2 = evt.getParam(2);
			//check peers are online and that they are not already connected
			//if peer is not online, come online
			if(!isPeerOnline(peer1)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer1);
				addPeer(peer1);
			}
			if(!isPeerOnline(peer2)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer2);
				addPeer(peer2);
			}
			if(!arePeersConnected(peer1, peer2)) {
				connectPeers(peer1, peer2);
				connectPeers(peer2, peer1);
			}
		} else if(evt.getType().equals("disconnect")){
			int peer1 = evt.getParam(1);
			int peer2 = evt.getParam(2);
			//check peers are online and that they are already connected
			//if peer is not online, come online
			if(!isPeerOnline(peer1)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer1);
				addPeer(peer1);
			}
			if(!isPeerOnline(peer2)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer2);
				addPeer(peer2);
			}
			if(arePeersConnected(peer1, peer2)) {
				disconnectPeers(peer1, peer2);
				disconnectPeers(peer2, peer1);
			}
		} else if(evt.getType().equals("publish")){		
			int peer = evt.getParam(1);
			int document = evt.getParam(2);
			//check peer is online, check peer has not already published document
			//if peer is not online, come online
			if(!isPeerOnline(peer)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer);
				addPeer(peer);
			}
			if(!isDocumentPublished(document)) {
				addDocument(document, peer);
			}
		} else if(evt.getType().equals("remove")){
			int peer = evt.getParam(1);
			int document = evt.getParam(2);
			//check peer is online, check peer has published document
			//if peer is not online, come online
			if(!isPeerOnline(peer)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer);
				addPeer(peer);
			}
			if(isDocumentPublished(document)) {
				removeDocument(document, peer);
			}
		} else if(evt.getType().equals("linkdocument")) {
			int doc1 = evt.getParam(1);
			int doc2 = evt.getParam(2);
			//check documents are both published if not, it is impossible to know who published the 
			//documents making adding them to the graph difficult
			if(isDocumentPublished(doc1) && isDocumentPublished(doc2)) {
				connectDocuments(doc1, doc2);
			}
		} else if(evt.getType().equals("delinkdocument")) {
			int doc1 = evt.getParam(1);
			int doc2 = evt.getParam(2);
			//check documents are both published if not, it is impossible to know who published the 
			//documents making adding them to the graph difficult
			if(isDocumentPublished(doc1) && isDocumentPublished(doc2)) {
				disconnectDocuments(doc1, doc2);
			}
		} else if(evt.getType().endsWith("query")) { //use ends with so 'un'-prefix (unquery etc.) will also satisfy
			int peer = evt.getParam(1);
			if(!isPeerOnline(peer)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer);
				addPeer(peer);
			}
		} else if(evt.getType().endsWith("queryhit")) {
			int peer = evt.getParam(1);
			int document = evt.getParam(2);
			if(!isPeerOnline(peer)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer);
				addPeer(peer);
			}
			if(!isDocumentPublished(document)) {
				addDocumentPublish(events,evt.getTime()-100,peer,document);
				addDocument(document, peer);
			}
			
		} else if(evt.getType().endsWith("queryreachespeer")) {
			int peer = evt.getParam(1);
			if(!isPeerOnline(peer)) {
				addPeerOnlineEvent(events,evt.getTime()-100,peer);
				addPeer(peer);
			}
		}
	}
	

	
	
	/**
	 * Helper method adds an "online" event into the list of LogEvents
	 * 
	 * @param events		 The list of LogEvents modifying the graph
	 * @param timeToAddEvent The time the event takes place
	 * @param peerNumber	 The peer to add the event for
	 */
	private static void addPeerOnlineEvent(List<LogEvent> events, long timeToAddEvent, int peerNumber) {
		try{
			//peer will for sure be online as long as "if(!isPeerOnline(peer))" is done before addPeerOnlineEvent
			LogEvent eventToAdd = new LogEvent(timeToAddEvent,"online",peerNumber,0,0);		
			addEventAtProperTime(eventToAdd, events);
			
		} catch(Exception e) {
			System.out.println("Error in adding peer online event");
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper method adds a "publish" event into the list of LogEvents
	 * 
	 * @param events		 The list of LogEvents modifying the graph
	 * @param timeToAddEvent The time the event takes place
	 * @param peerNumber	 The peer who publishes the document in the event
	 * @param documentNumber the Document to be published in the event
	 */
	private static void addDocumentPublish(List<LogEvent> events, long timeToAddEvent, int peerNumber, int documentNumber) {
		try {
			LogEvent eventToAdd = new LogEvent(timeToAddEvent,"publish",peerNumber,documentNumber,0);
			addEventAtProperTime(eventToAdd, events);
		} catch(Exception e) {
			System.out.println("Error in adding document publish event");
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper method places the passed event into the properly sorted position.
	 * 
	 * ensures the event is placed in the proper position in the list (times match up)
	 * @param eventToAdd The event to place into the list.
	 * @param events The list which the event will be added to
	 */
	private static void addEventAtProperTime(LogEvent eventToAdd, List<LogEvent>events) {
		synchronized(events) {
			if(events.isEmpty()) {
				events.add(0,eventToAdd); 
			}
			else {
				// Generate an iterator. Start just after the last element.
				ListIterator<LogEvent> li = events.listIterator(events.size());
				int index = events.size();
				// Iterate in reverse.
				while(li.hasPrevious()) {
					LogEvent evt = li.previous();
					if(evt.getTime()<eventToAdd.getTime() || index == 0) { //find the proper time to insert the event
						events.add(index,eventToAdd);
						break;
					}
					index--;
				}
			}
		}
	}
}