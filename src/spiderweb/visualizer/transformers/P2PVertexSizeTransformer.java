/*
 * File:         P2PVertexSizeFunction.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.transformers;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

import spiderweb.graph.PeerDocumentVertex;
import spiderweb.graph.PeerVertex;
/**
 * Size function for the vertices in a graph representing a P2P network.
 * The size of the shapes representing the nodes are given in the constructor.
 * This class has three attributes, that indicate the size of vertices representing peers, 
 * the size of vertices representing documents stored by the peers, and a parameter that indicates 
 * the limit between integers (vertex labels) representing documents and integers representing peers   
 * @author adavoust
 */
public class P2PVertexSizeTransformer<V, E> implements Transformer<V,Integer>{

	//private static final int MIN_SIZE = 15;
	//private static final int MAX_SIZE = 50;
	
	private int my_doc_size;
	private int my_peer_doc_size;
	private int my_peer_size;
	
	protected Graph<V, E> graph;
	
	private boolean scale = false;
	
	/**
	 * Constructor
	 * @param ds document vertex size
	 * @param ps Peer Size
	 * @param pds peer document size
	 */
	public P2PVertexSizeTransformer(int ds, int ps, int pds, Graph<V, E> graph){
		
		my_doc_size = ds;
		my_peer_size = ps;
		my_peer_doc_size = pds;
		
		this.graph = graph;
	}
	
	@Override
	public Integer transform(V vertexID) {
		if (vertexID instanceof PeerVertex) {
			if(scale) {
				int numConnections = graph.getOutEdges(vertexID).size();
				return new Integer(my_peer_size+(numConnections*2));
			}
			return new Integer(my_peer_size);
		}
		else if (vertexID instanceof PeerDocumentVertex) {
			return new Integer(my_peer_doc_size);
		}
		else {
			if(scale) {
				int numConnections = graph.getInEdges(vertexID).size();
				return new Integer(my_doc_size+(numConnections));
			}
			return new Integer(my_doc_size);
		}
	}
	
	public void setScaling(boolean scale) {
    	this.scale = scale;
    }	    
}
