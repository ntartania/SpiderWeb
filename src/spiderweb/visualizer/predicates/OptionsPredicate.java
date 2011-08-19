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

import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

/**
 * The Options
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 19/08/2011 
 */
public class OptionsPredicate implements Predicate<Context<Graph<P2PVertex, P2PConnection>, P2PVertex>>, PredicateChangeListener {

	protected Graph<P2PVertex, P2PConnection> othergraph;
	protected Class<? extends P2PVertex> exclude;
	
	protected boolean showHiddenVertices;
	
	
	
	public OptionsPredicate(Graph<P2PVertex, P2PConnection> g) {
		othergraph = g;
		exclude = null;
		showHiddenVertices = false;
	}
	
	public void setExcludeClass(Class<? extends P2PVertex> exclude) {
		this.exclude = exclude;
	}
	
	
	@Override
	public boolean evaluate(Context<Graph<P2PVertex, P2PConnection>, P2PVertex> context) {
		if(!othergraph.containsVertex(context.element)) { 
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
		
		return true;
		
	}
}
