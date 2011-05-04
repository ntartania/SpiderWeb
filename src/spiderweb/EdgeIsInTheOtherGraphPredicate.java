package spiderweb;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

/**
 * This class is a predicate, and as such the logical predicate implemented is the following :
 * the predicate is related to a particular graph, and evaluates if the given vertex exists within the associated graph.
 * This is used because the simulation contains two graphs, one partly visible, that maintains the full layout,
 * and one invisible, which simply stores the information of the graph state as the simulation proceeds.
 * 
 * @author alan
 *
 */
public class EdgeIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<P2PVertex, P2PConnection>, P2PConnection>> {

	private Graph<P2PVertex, P2PConnection> othergraph;
	
	public EdgeIsInTheOtherGraphPredicate(Graph<P2PVertex, P2PConnection> g){
		othergraph= g;
		
	}
	@Override
	public boolean evaluate(Context<Graph<P2PVertex, P2PConnection>, P2PConnection> context) {
		
		return (othergraph.containsEdge(context.element));
		
	}
	

}
