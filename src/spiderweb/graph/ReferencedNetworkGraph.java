package spiderweb.graph;

import java.util.List;

public class ReferencedNetworkGraph {	
	
	private P2PNetworkGraph dynamicGraph;
	private P2PNetworkGraph referenceGraph;
	
	
	public ReferencedNetworkGraph(P2PNetworkGraph referenceGraph, P2PNetworkGraph dynamicGraph) {
		this.dynamicGraph = dynamicGraph;
		this.referenceGraph = referenceGraph;
	}
	
	public ReferencedNetworkGraph() {
		this(new P2PNetworkGraph(), new P2PNetworkGraph());
	}
	
	public ReferencedNetworkGraph(P2PNetworkGraph referenceGraph) {
		this(referenceGraph, new P2PNetworkGraph());
	}
	
	public P2PNetworkGraph getReferenceGraph() {
		return referenceGraph;
	}
	
	public P2PNetworkGraph getDynamicGraph() {
		return dynamicGraph;
	}
	
	public void setReferenceGraph(P2PNetworkGraph referenceGraph) {
		this.referenceGraph = referenceGraph;
	}
	
	public void setDynamicGraph(P2PNetworkGraph dynamicGraph) {
		this.dynamicGraph = dynamicGraph;
	}
	
	public void constructionEvent(LogEvent evt) {
		referenceGraph.graphEvent(evt);
	}
	
	public void robustConstructionEvent(List<LogEvent> events, int currentIndex) {
		referenceGraph.robustGraphEvent(events, currentIndex);
	}
	
	public void graphEvent(LogEvent evt, boolean forward) {
		dynamicGraph.graphEvent(evt, forward, referenceGraph);
	}
	
}
