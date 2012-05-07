package spiderweb;

import org.apache.commons.collections15.Predicate;

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
		
		return (othergraph.containsVertex(context.element));
		
	}
	
}
