/*
 * File:         P2PEdgeShapeTransformer.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.transformers;

import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PVertex;
import spiderweb.visualizer.EdgeShapeType;

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

	private AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> P2PEdgeShape;
	private AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> P2DocEdgeShape;
	private AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> Doc2PDocEdgeShape;
	private AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> P2PDocEdgeShape;
	private final AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> defaultEdgeShape;
	
	/**
	 * 
	 * @param P2PEdgeShape		Peer to Peer Edge Shape.
	 * @param P2DocEdgeShape	Peer to Document Edge Shape.
	 * @param Doc2PDocEdgeShape	Document to PeerDocument Edge Shape.
	 * @param P2PDocEdgeShape	Peer to PeerDocument Edge Shape.
	 */
	public P2PEdgeShapeTransformer(EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape){
		this.P2PEdgeShape = shapeChooser(P2PEdgeShape);
		this.P2DocEdgeShape = shapeChooser(P2DocEdgeShape);
		this.Doc2PDocEdgeShape = shapeChooser(Doc2PDocEdgeShape);
		this.P2PDocEdgeShape = shapeChooser(P2PDocEdgeShape);
		
		defaultEdgeShape = new EdgeShape.Box<P2PVertex, P2PConnection>();
	}
	@Override
	public Shape transform(
			Context<Graph<P2PVertex, P2PConnection>, P2PConnection> context) {
		if (context.element.isP2P()) {
			return P2PEdgeShape.transform(context); //a curve if this is between peers
		}
		else if (context.element.isP2DOC()){
			return P2DocEdgeShape.transform(context); // between peer and doc, a straight line
		}
		else if (context.element.isDOC2PDOC()){

			return Doc2PDocEdgeShape.transform(context); // between peer and doc, a straight line
		}
		else if (context.element.isP2PDOC()){

			return P2PDocEdgeShape.transform(context); // between peer and doc, a straight line
		}
		return defaultEdgeShape.transform(context);
	}
	
	private AbstractEdgeShapeTransformer<P2PVertex, P2PConnection> shapeChooser(EdgeShapeType chosenShape) {
		switch(chosenShape) {
		
		case BENT_LINE:
			return new EdgeShape.BentLine<P2PVertex, P2PConnection>();
		case BOX:
			return new EdgeShape.Box<P2PVertex, P2PConnection>();
		case CUBIC_CURVE:
			return new EdgeShape.CubicCurve<P2PVertex, P2PConnection>();
		case LINE:
			return new EdgeShape.Line<P2PVertex, P2PConnection>();
		case LOOP:
			return new EdgeShape.Loop<P2PVertex, P2PConnection>();
		case ORTHOGONAL:
			return new EdgeShape.Orthogonal<P2PVertex, P2PConnection>();
		case QUAD_CURVE:
			return new EdgeShape.QuadCurve<P2PVertex, P2PConnection>();
		case SIMPLE_LOOP:
			return new EdgeShape.SimpleLoop<P2PVertex, P2PConnection>();
		case WEDGE:
			return new EdgeShape.Wedge<P2PVertex, P2PConnection>(3);
    		
		}
		return new EdgeShape.Line<P2PVertex, P2PConnection>();
	}

}
