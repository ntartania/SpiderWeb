/*
 * File:         P2PConnection.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 29/08/2011 
 * Author:       Matthew Smith
 * 				 Alan Davoust
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph;

/**
 * P2PConnection represents the edge between two vertices on the graph.
 * The connection has different states which the transformer will draw 
 * the connection as different sizes and shapes to differentiate.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @author Alan Davoust
 * @version Date: 29/08/2011 
 */
public class P2PConnection {

	public static final int P2P = 0;
	public static final int P2DOC = 10;
	public static final int P2PDOC = 20;
	public static final int DOC2PDOC = 30;
	public static final int DOC2DOC = 40;
	public static final int QUERYING = 1;
	public static final int ANSWERING = 2;
	public static final int MATCHING_DOC = 11;
	private int mytypeandstate;
	
	private Integer key;
	private String edgeName;
	
	/**
	 * P2PConnection represents the edge between two vertices on the graph.
	 * The connection has different states which the transformer will draw 
	 * the connection as different sizes and shapes to differentiate.
	 * 
	 * @param type either <code>P2PConnection.P2P</code> (edge between two peers) or 
	 * 		<code>P2PConnection.P2DOC</code> (edge between a peer and a document)
	 * @param key the <code>Integer</code> key this edge is in the graph.
	 */
	public P2PConnection(int type, Integer key){
		mytypeandstate = type;
		this.key = key;
		edgeName = "edge "+key.toString();
	}
	
	/**
	 * Returns the key value of the edge.
	 * @return the <code>Integer</code> key value of the edge
	 */
	public Integer getKey(){
		return key;
	}
	
	/**
	 * is this edge a peer-to-peer edge 
	 * @return true for edges that relate peers, false for edges that relate peers to docs
	 */
	public int getType(){
		if (mytypeandstate <10) {
			return P2P;
		}
		else if (mytypeandstate >=10 && mytypeandstate <20) {
			return P2DOC;
		}
		else if (mytypeandstate >=20 && mytypeandstate <30) {
			return P2PDOC;
		}
		else {
			return DOC2PDOC;
		}
	}
	
	public boolean isP2P() {
		return (getType() == P2P);
	}
	
	public boolean isP2DOC() {
		return (getType() == P2DOC);
	}
	
	public boolean isP2PDOC() {
		return (getType() == P2PDOC);
	}
	
	public boolean isDOC2PDOC() {
		return (getType() == DOC2PDOC);
	}
	
	/** 
	 * change state to "query"
	 */ 
	public void query(){
		if (mytypeandstate==P2P)
			mytypeandstate=QUERYING;
	}
	
	/**
	 * If the Edge is in a querying state, turn it back to a normal P2P State.
	 */
	public void backToNormal() {
		if (isQuerying())
			mytypeandstate=P2P;
		
	}
	
	/**
	 * return whether or not the edge is in a query state.
	 * @return <code>true</code> if the edge is in a query state
	 */
	public boolean isQuerying() {
		return (mytypeandstate==QUERYING);
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof P2PConnection)
			return (key.equals(((P2PConnection)other).getKey()));
			else 
		return false; 
	}

	@Override
	public int hashCode(){
		return key.hashCode();	
	}
	
	@Override
	public String toString(){
		return edgeName;
	}
	
	

}
