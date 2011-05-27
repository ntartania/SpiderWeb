package spiderweb;

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
