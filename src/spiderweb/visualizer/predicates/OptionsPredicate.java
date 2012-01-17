/*
 * File:         OptionsPredicate.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 19/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.predicates;

import org.apache.commons.collections15.Predicate;

import spiderweb.graph.DocumentVertex;
import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;
import spiderweb.graph.PeerVertex;
import spiderweb.graph.ReferencedNetworkGraph;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

/**
 * The Options Predicate will listen to anything that handles PredicateChangeEvents and show specified 
 * vertices depending on what options are passed into it.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 19/08/2011 
 */
public class OptionsPredicate implements Predicate<Context<Graph<P2PVertex, P2PConnection>, P2PVertex>>, PredicateChangeListener {

	protected ReferencedNetworkGraph graph;
	protected Class<? extends P2PVertex> exclude;
	
	protected boolean showHiddenVertices;
	protected boolean disregardTime;
	
	protected boolean cutoffPeers;
	protected int peerInDegreeCutoff;
	protected int peerOutDegreeCutoff;
	
	protected boolean cutoffDocuments;
	protected int documentInDegreeCutoff;
	protected int documentOutDegreeCutoff;
	
	/**
	 * The Options Predicate will listen to anything that handles PredicateChangeEvents and show specified 
	 * vertices depending on what options are passed into it and providing the vertices are in both the reference
	 * graph and the dynamic graph.
	 * @param graph The referenced graph which this predicate is operating on
	 */
	public OptionsPredicate(ReferencedNetworkGraph graph) {
		this.graph = graph;
		exclude = null;
		showHiddenVertices = false;
	}
	
	/**
	 * Set the Type of vertex to exclude from drawing.
	 * @param exclude The type of vertex class to exclude from drawing; required to extend <code>P2PVertex.class</code>
	 */
	public void setExcludeClass(Class<? extends P2PVertex> exclude) {
		this.exclude = exclude;
	}
	
	
	@Override
	public boolean evaluate(Context<Graph<P2PVertex, P2PConnection>, P2PVertex> context) {
		if(disregardTime) { // meaning show all vertices 
			return true;
		}
		if(!graph.getDynamicGraph().containsVertex(context.element)) { 
			return false; //the dynamic graph doesn't contain the vertex to show
		}
		if(context.element.isHidden()) { //if the vertex is hidden
			if(!showHiddenVertices) { //and the option to show hidden is off
				return false; //do not show vertex
			}
		}
		if((context.element.getClass().equals(exclude))) { 
			return false; //If this vertex is the same as the exclude type
		}
		//if this vertex is a peer and peers are being cutoff depending on their degree
		if(cutoffPeers && context.element.getClass().equals(PeerVertex.class)) { 
			int inDegree = graph.getDynamicGraph().getInEdges(context.element).size();
			int outDegree = graph.getDynamicGraph().getOutEdges(context.element).size();
			if(peerInDegreeCutoff > inDegree) {
				return false;
			}
			if(peerOutDegreeCutoff > outDegree) {
				return false;
			}
		}
		//if this vertex is a document and documents are being cutoff depending on their degree
		if(cutoffDocuments && context.element.getClass().equals(DocumentVertex.class)) {
			int inDegree = graph.getDynamicGraph().getInEdges(context.element).size();
			int outDegree = graph.getDynamicGraph().getOutEdges(context.element).size();
			if(peerInDegreeCutoff > inDegree) {
				return false;
			}
			if(peerOutDegreeCutoff > outDegree) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void changeOptions(PredicateChangeEvent pce) {
		showHiddenVertices = pce.getShowHiddenVertices();
		disregardTime = pce.getDisregardTime();
		
		cutoffPeers = pce.getCutoffPeers();
		peerInDegreeCutoff = pce.getPeerInDegreeCutoff();
		peerOutDegreeCutoff = pce.getPeerOutDegreeCutoff();
		
		cutoffDocuments = pce.getCutoffDocuments();
		documentInDegreeCutoff = pce.getDocumentInDegreeCutoff();
		documentOutDegreeCutoff = pce.getDocumentOutDegreeCutoff();
	}
}
