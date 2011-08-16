/*
 * File:         VertexIsInTheOtherGraphPredicate.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

import org.apache.commons.collections15.Predicate;

import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class VertexIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<P2PVertex, P2PConnection>, P2PVertex>> {

	protected Graph<P2PVertex, P2PConnection> othergraph;
	
	public VertexIsInTheOtherGraphPredicate(Graph<P2PVertex, P2PConnection> g){
		othergraph = g;
		
	}
	@Override
	public boolean evaluate(Context<Graph<P2PVertex, P2PConnection>, P2PVertex> context) {
		
		return (othergraph.containsVertex(context.element) && !context.element.isHidden());
		
	}
	
}
