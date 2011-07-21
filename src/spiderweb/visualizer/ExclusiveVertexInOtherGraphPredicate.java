/*
 * File:         ExclusiveVertexInOtherGraphPredicate.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class ExclusiveVertexInOtherGraphPredicate extends VertexIsInTheOtherGraphPredicate {

	Class<? extends P2PVertex> exclude;
	
	public ExclusiveVertexInOtherGraphPredicate(Graph<P2PVertex, P2PConnection> g, Class<? extends P2PVertex> exclude) {
		super(g);
		this.exclude = exclude;
	}
	
	@Override
	public boolean evaluate(Context<Graph<P2PVertex, P2PConnection>, P2PVertex> context) {
		
		return (super.evaluate(context) && !(context.element.getClass().equals(exclude)));
		
	}

}
