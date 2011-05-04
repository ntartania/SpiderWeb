package spiderweb;

import java.awt.Shape;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

/**
 * An Edge Shape Transformer class that simply encapsulates the generic QuadCurve and Line transformers.
 * This transformer is used to set the shape of an edge in a graph rendering.
 * For a P2PNetwork, connections between peers will be rendered as curves (quadcurves) and connections between 
 * peers and the docs that the peer publishes are rendered as straight lines. Note that the stroke is 
 * determined by another rendering class, and so is the length.
 * @author alan
 *
 */
public class P2PEdgeShapeTransformer
		implements
		Transformer<Context<Graph<P2PVertex, P2PConnection>, P2PConnection>, Shape> {

	private AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> curvetransformer;
	private AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> segmenttransformer;
	
	public P2PEdgeShapeTransformer(){
		curvetransformer = new EdgeShape.QuadCurve<P2PVertex,P2PConnection>();
		segmenttransformer = new EdgeShape.Line<P2PVertex, P2PConnection>();
	}
	@Override
	public Shape transform(
			Context<Graph<P2PVertex, P2PConnection>, P2PConnection> context) {
		if (context.element.isP2P())
			return curvetransformer.transform(context); //a curve if this is between peers
		else
			return segmenttransformer.transform(context); // between peer and doc, a straight line
	}

}
