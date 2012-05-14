/*
 * File:         ReferencedNetworkGraph.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/08/2011
 * Last Changed: Date: 19/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph;

import java.util.List;

/**
 * Container for two P2P Network Graphs where one is a full graph containing of all vertices that ever appear on the graph, 
 * and the other references the full graph only adding vertices and edges provided they are in the full graph.
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 19/08/2011 
 */
public class ReferencedNetworkGraph {	
	
	private P2PNetworkGraph dynamicGraph;
	private P2PNetworkGraph fullGraph;
	
	
	public ReferencedNetworkGraph(P2PNetworkGraph fullGraph, P2PNetworkGraph dynamicGraph) {
		this.dynamicGraph = dynamicGraph;
		this.fullGraph = fullGraph;
	}
	
	public ReferencedNetworkGraph() {
		this(new P2PNetworkGraph(), new P2PNetworkGraph());
	}
	
	public ReferencedNetworkGraph(P2PNetworkGraph fullGraph) {
		this(fullGraph, new P2PNetworkGraph());
	}
	
	public P2PNetworkGraph getFullGraph() {
		return fullGraph;
	}
	
	public P2PNetworkGraph getDynamicGraph() {
		return dynamicGraph;
	}
	
	public void setfullGraph(P2PNetworkGraph fullGraph) {
		this.fullGraph = fullGraph;
	}
	
	public void setDynamicGraph(P2PNetworkGraph dynamicGraph) {
		this.dynamicGraph = dynamicGraph;
	}
	
	public void constructionEvent(LogEvent evt) {
		fullGraph.graphEvent(evt);
	}
	
	public void robustConstructionEvent(List<LogEvent> events, int currentIndex) {
		fullGraph.robustGraphEvent(events, currentIndex);
	}
	
	public void graphEvent(LogEvent evt, boolean forward) {
		dynamicGraph.graphEvent(evt, forward, fullGraph);
	}
	
}
