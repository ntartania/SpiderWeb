package spiderweb;

public class P2PConnection implements Comparable<P2PConnection> {

	public static final int P2P = 0;
	public static final int P2DOC = 10;
	public static final int P2PDOC = 20;
	public static final int DOC2PDOC = 30;
	public static final int QUERYING = 1;
	public static final int ANSWERING = 2;
	public static final int MATCHING_DOC = 11;
	private int mytypeandstate;
	//private P2PVertex peer;
	private Integer key;
	
	//not sure how useful this will be
	public static P2PConnection connectPeers(Integer p1, Integer p2, int count){
		return new P2PConnection (P2P, new Integer(count));
	}
	
	/** constructor
	 * 
	 * @param type either P2P (edge between two peers) or P2DOC (edge between a peer and a document)
	 * @param key
	 */
	public P2PConnection(int type, Integer key){
		mytypeandstate = type;
		this.key = key;
	}
	
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
	
	/** change state to "query"*/ 
	public void query(){
		if (mytypeandstate==P2P)
			mytypeandstate=QUERYING;
	}
	
	/**
	 * change from whatever it was to just being a doc
	 */
	public void normalState(){
		if (isP2P())
			mytypeandstate=P2P;
		else
			mytypeandstate=P2DOC;
		
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
	public int compareTo(P2PConnection other) {
		if(other instanceof P2PConnection)
			return (key.compareTo(((P2PConnection)other).getKey()));
		else 
		return 0; // there's a problem anyway : can only compare two P2PConnection objects
	}

	public String toString(){
		return "edge "+key.toString();
	}
	public boolean isQuerying() {
		
		return (mytypeandstate ==QUERYING);
	}

	public void backToNormal() {
		if (isQuerying())
			mytypeandstate=P2P;
		
	}

}
